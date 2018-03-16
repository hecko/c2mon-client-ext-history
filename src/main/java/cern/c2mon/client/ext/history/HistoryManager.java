/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.client.ext.history;

import java.util.*;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.cache.BasicCacheHandler;
import cern.c2mon.client.core.jms.ConnectionListener;
import cern.c2mon.client.core.jms.SupervisionListener;
import cern.c2mon.client.core.listener.TagSubscriptionListener;
import cern.c2mon.client.core.service.AdvancedTagService;
import cern.c2mon.client.core.service.CoreSupervisionService;
import cern.c2mon.client.core.tag.TagController;
import cern.c2mon.client.ext.history.common.*;
import cern.c2mon.client.ext.history.common.exception.HistoryPlayerNotActiveException;
import cern.c2mon.client.ext.history.common.tag.HistoryTagManager;
import cern.c2mon.client.ext.history.data.HistoryLoadingManagerImpl;
import cern.c2mon.client.ext.history.dbaccess.HistorySessionFactory;
import cern.c2mon.client.ext.history.playback.HistoryPlayerCoreAccess;
import cern.c2mon.client.ext.history.playback.HistoryPlayerImpl;
import cern.c2mon.client.ext.history.util.KeyForValuesMap;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;

@Service
@Slf4j
public class HistoryManager implements C2monHistoryManager, TagSubscriptionListener {

  /** Reference to the <code>TagManager</code> singleton */
  private final AdvancedTagService tagService;

  /** Reference to the <code>ClientDataTagCache</code> */
  private final BasicCacheHandler cache;

  /** Reference to the <code>C2monSupervisionManager</code> */
  private final CoreSupervisionService coreSupervisionService;

  /** Reference to the {@link HistoryTagManager} */
  private final HistoryTagManager historyTagManager;

  /** the history player */
  private HistoryPlayerCoreAccess historyPlayer = null;

  /** Keeps track of which tag id belongs to which {@link SupervisionListener} */
  private final KeyForValuesMap<Long, SupervisionListener> tagToSupervisionListener = new KeyForValuesMap<>();

  /** A connection listener checking if the connection to the JMS is lost. */
  private ConnectionListener jmsConnectionListener = null;

  private HistorySessionFactory historySessionFactory;

  @Autowired
  protected HistoryManager(final AdvancedTagService tagService, final BasicCacheHandler pCache,
      final CoreSupervisionService supervisionService, final HistoryTagManager historyTagManager,
                           @Qualifier("historyFactory") HistorySessionFactory historySessionFactory) {

    this.tagService = tagService;
    this.cache = pCache;
    this.coreSupervisionService = supervisionService;
    this.historyTagManager = historyTagManager;
    this.historySessionFactory = historySessionFactory;
  }

  /**
   * Inner method to initialize the STL database connection.
   */
  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    tagService.addTagSubscriptionListener(this);
  }

  @Override
  public void startHistoryPlayerMode(final HistoryProvider provider, final Timespan timespan) {
    if (!coreSupervisionService.isServerConnectionWorking()) {
      throw new RuntimeException("Cannot go into history mode," +
      		" because the connection to the server is down.");
    }

    cache.setHistoryMode(true);

    synchronized (cache.getHistoryModeSyncLock()) {
      if (cache.isHistoryModeEnabled()) {
        if (jmsConnectionListener == null) {
          jmsConnectionListener = new ConnectionEvents();
          coreSupervisionService.addConnectionListener(jmsConnectionListener);
        }

        if (historyPlayer == null) {
          historyPlayer = new HistoryPlayerImpl();
        }

        // Configures the history player with the given provider and time span
        historyPlayer.configure(provider, timespan);

        // Activating the history player
        historyPlayer.activateHistoryPlayer();
        try {

          // Subscribes all the current subscribed tags
          subscribeTagsToHistory(cache.getAllTagControllers());
        }
        catch (RuntimeException e) {
          historyPlayer.deactivateHistoryPlayer();
          cache.setHistoryMode(false);
          throw e;
        }
      }
    }
  }

  @Override
  public void stopHistoryPlayerMode() {
    if (cache.isHistoryModeEnabled()) {
      if (historyPlayer != null) {
        historyPlayer.stopLoading();
      }

      synchronized (cache.getHistoryModeSyncLock()) {
        if (historyPlayer != null) {
          historyPlayer.deactivateHistoryPlayer();
        }
      }
      cache.setHistoryMode(false);
    }
  }

  @Override
  public void onNewTagSubscriptions(final Set<Long> tagIds) {
    if (tagIds != null && tagIds.size() > 0) {
      synchronized (cache.getHistoryModeSyncLock()) {
        if (cache.isHistoryModeEnabled()
            && this.historyPlayer != null
            && this.historyPlayer.isHistoryPlayerActive()) {
          subscribeTagsToHistory(tagIds);
        }
      }
    }
  }

  @Override
  public void onUnsubscribe(final Set<Long> tagIds) {
    if (tagIds != null && tagIds.size() > 0) {
      unsubscribeTagsFromHistory(tagIds);
    }
  }

  /**
   * Subscribes the tags to the history player.<br/>
   * <br/>
   * The caller must hold the {@link BasicCacheHandler#getHistoryModeSyncLock()}
   * when calling this method
   *
   * @param tagIds
   *          The tags to subscribes
   */
  private void subscribeTagsToHistory(final Set<Long> tagIds) {
    subscribeTagsToHistory(cache.getTagControllers(tagIds));
  }

  /**
   * Subscribes the tags to the history player.<br/>
   * <br/>
   * The caller must hold the {@link BasicCacheHandler#getHistoryModeSyncLock()}
   * when calling this method
   *
   * @param tagController
   *          The tags to subscribes
   */
  private void subscribeTagsToHistory(final Collection<TagController> tagControllers) {
    if (cache.isHistoryModeEnabled()
        && this.historyPlayer != null
        && this.historyPlayer.isHistoryPlayerActive()) {

      // Registers all the tag update listeners and supervision listeners
      for (final TagController tagController : tagControllers) {

        Tag realtimeValue = tagController.getTagImpl().clone();

        tagController.clean();

        // Registers to tag updates
        this.historyPlayer.registerTagUpdateListener(
            tagController,
            realtimeValue.getId(),
            realtimeValue);

        // Tracks the listener, used when for later when unsubscribing
        tagToSupervisionListener.add(realtimeValue.getId(), tagController);

        // Register to supervision events
        this.historyPlayer.registerSupervisionListener(
            SupervisionEntity.PROCESS,
            tagController,
            realtimeValue.getProcessIds());

        this.historyPlayer.registerSupervisionListener(
            SupervisionEntity.EQUIPMENT,
            tagController,
            realtimeValue.getEquipmentIds());

//          For the day the SupervisionEntity.SUBEQUIPMENT also comes
//          this.historyPlayer.registerSupervisionListener(
//              SupervisionEntity.SUBEQUIPMENT,
//              (SupervisionListener) clientDataTag,
//              clientDataTag.getSubEquipmentIds());
      }

      new Thread("History-Player-Begin-Loading-Thread") {
        @Override
        public void run() {
          // Begins the loading process
          historyPlayer.beginLoading();
        }
      }.start();
    }
  }

  /**
   * Unsubscribes the tags to the history player
   *
   * @param tagIds
   *          The tags to unsubscribe
   */
  private void unsubscribeTagsFromHistory(final Set<Long> tagIds) {
    synchronized (cache.getHistoryModeSyncLock()) {
      if (cache.isHistoryModeEnabled()
          && this.historyPlayer != null
          && this.historyPlayer.isHistoryPlayerActive()) {

        this.historyPlayer.unregisterTags(tagIds);

        // Unregistering the supervision listeners
        for (final Long tagId : tagIds) {
          for (final SupervisionListener listener : tagToSupervisionListener.getValues(tagId)) {
            this.historyPlayer.unregisterSupervisionListener(SupervisionEntity.PROCESS, listener);
            this.historyPlayer.unregisterSupervisionListener(SupervisionEntity.EQUIPMENT, listener);
            this.historyPlayer.unregisterSupervisionListener(SupervisionEntity.SUBEQUIPMENT, listener);
            tagToSupervisionListener.removeValue(listener);
          }
        }
      }
    }

  }

  @Override
  public HistoryPlayer getHistoryPlayer() throws HistoryPlayerNotActiveException {
    if (isHistoryModeEnabled()) {
      return historyPlayer;
    }
    else {
      throw new HistoryPlayerNotActiveException("The history player is not active, and can therefore not be retrieved");
    }
  }

  @Override
  public HistoryPlayerEvents getHistoryPlayerEvents() {
    synchronized (cache.getHistoryModeSyncLock()) {
      if (this.historyPlayer == null) {
        this.historyPlayer = new HistoryPlayerImpl();
      }
      return this.historyPlayer;
    }
  }

  @Override
  public HistoryProviderFactory getHistoryProviderFactory() {
    return new HistoryProviderFactoryImpl(new ClientDataTagRequester(), historySessionFactory);
  }

  @Override
  public boolean isHistoryModeEnabled() {
    return cache.isHistoryModeEnabled();
  }

  class ConnectionEvents implements ConnectionListener {
    @Override
    public void onConnection() {

    }

    @Override
    public void onDisconnection() {
      if (isHistoryModeEnabled()) {
        stopHistoryPlayerMode();
        log.info("The history mode is stopped, because the connection to the server is lost.");
      }
    }
  }

  @Override
  public HistoryLoadingManager createHistoryLoadingManager(final HistoryProvider historyProvider, final Collection<Long> tagIds) {
    final HistoryLoadingManager manager = new HistoryLoadingManagerImpl(historyProvider);
    final Collection<Tag> cdtValues = this.tagService.get(tagIds);
    final List<Tag> cdts = new ArrayList<>();
    for (final Tag cdtValue : cdtValues) {
      if (cdtValue instanceof Tag) {
        cdts.add(cdtValue);
      }
      else {
        throw new RuntimeException(String.format("The '%s' must be of type '%s'", Tag.class.getName(), Tag.class.getName()));
      }
    }
    manager.addClientDataTagsForLoading(cdts);
    return manager;
  }

  /** Used by the {@link HistoryProvider} to get access to ClientDataTagValues */
  class ClientDataTagRequester implements ClientDataTagRequestCallback {
    @Override
    public Tag getClientDataTagValue(final long tagId) {
      final Collection<Tag> tagValues = tagService.get(Arrays.asList(tagId));
      if (tagValues == null || tagValues.size() == 0) {
        throw new RuntimeException("Cannot get the client data tag value for the tag id " + tagId);
      }
      return tagValues.iterator().next();
    }
  }

  @Override
  public HistoryTagManager getHistoryTagManager() {
    return this.historyTagManager;
  }

}
