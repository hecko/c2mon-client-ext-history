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
package cern.c2mon.client.ext.history.updates;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import lombok.Data;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import cern.c2mon.client.ext.history.common.HistoryTagValueUpdate;
import cern.c2mon.client.ext.history.common.id.TagValueUpdateId;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.common.datatag.DataTagQuality;

/**
 * Implementation of the {@link TagValueUpdate}
 *
 * @author vdeila
 *
 */
@Root(name="HistoryTag")
@Data
public class HistoryTagValueUpdateImpl implements HistoryTagValueUpdate {

  /** the updateId */
  private final TagValueUpdateId updateId;

  /** the DataTagQuality object for this data tag. */
  @Element(required = false)
  private final DataTagQuality dataTagQuality;

  /** the tag value */
  @Element(required = false)
  private Object value;

  /**
   * This timestamp indicates when the tag value event was generated by the
   * source.
   */
  @Element(required = false)
  private final Timestamp sourceTimestamp;

  /**
   * This timestamp indicates when the tag value passed the server. Please
   * notice that the source timestamp and server timestamp might be the same in
   * case that the value change event was generated by the server itself.
   */
  @Element(required = false)
  private final Timestamp serverTimestamp;

  /** The time the record were put into the database */
  private final Timestamp logTimestamp;

  /** the tag value description */
  @Element(required = false)
  private final String description;

  /**
   * The collection of registered alarms or an empty list, if no alarm is
   * defined
   */
  private final Collection<AlarmValue> alarms;

  /** the current mode of the tag. */
  @Element(required = false)
  private final TagMode mode;

  /**
   * <code>true</code>, if the tag value is currently simulated and not
   * corresponding to a live event.
   */
  private final boolean isSimulated;

  /** The data type of the value */
  private String valueClassName;

  /** The daq time */
  private Timestamp daqTimestamp;

  /** <code>true</code> if the value is an initial value */
  private boolean initialValue = false;

  public HistoryTagValueUpdateImpl(final Long tagId) {
    this(tagId, null, null, null, null, null, null, null, null, null);
  }

  /**
   *
   * @param tagId
   *          the tag identifier
   * @param dataTagQuality
   *          the DataTagQuality object for this data tag.
   * @param value
   *          the tag value
   * @param sourceTimestamp
   *          This timestamp indicates when the tag value event was generated by
   *          the source.
   * @param serverTimestamp
   *          This timestamp indicates when the tag value passed the server.
   *          Please notice that the source timestamp and server timestamp might
   *          be the same in case that the value change event was generated by
   *          the server itself.
   * @param logTimestamp
   *          The time the record were put into the database
   * @param description
   *          the tag value description
   * @param alarms
   *          The collection of registered alarms or an empty list, if no alarm
   *          is defined
   * @param mode
   *          the current mode of the tag.
   */
  public HistoryTagValueUpdateImpl(final Long tagId, final DataTagQuality dataTagQuality, final Object value,
      final Timestamp sourceTimestamp, final Timestamp daqTimestamp,
      final Timestamp serverTimestamp, final Timestamp logTimestamp,
      final String description, final AlarmValue[] alarms, final TagMode mode) {
    this.updateId = new TagValueUpdateId(tagId);
    this.dataTagQuality = dataTagQuality;
    this.value = value;
    this.sourceTimestamp = sourceTimestamp;
    this.daqTimestamp = daqTimestamp;
    this.serverTimestamp = serverTimestamp;
    this.logTimestamp = logTimestamp;
    this.description = description;
    this.mode = mode;
    this.isSimulated = false;
    this.alarms = new ArrayList<>();
    if (alarms != null) {
      this.alarms.addAll(Arrays.asList(alarms));
    }
  }

  /**
   * @param tagId
   *          the tag identifier
   * @param dataTagQuality
   *          the DataTagQuality object for this data tag.
   * @param value
   *          the tag value
   * @param sourceTimestamp
   *          This timestamp indicates when the tag value event was generated by
   *          the source.
   * @param serverTimestamp
   *          This timestamp indicates when the tag value passed the server.
   *          Please notice that the source timestamp and server timestamp might
   *          be the same in case that the value change event was generated by
   *          the server itself.
   * @param logTimestamp
   *          The time the record were put into the database
   * @param description
   *          the tag value description
   * @param mode
   *          the current mode of the tag.
   */
  public HistoryTagValueUpdateImpl(final Long tagId, final DataTagQuality dataTagQuality, final Object value,
      final Timestamp sourceTimestamp, final Timestamp daqTimestamp,
      final Timestamp serverTimestamp, final Timestamp logTimestamp, final String description, final TagMode mode) {
    this(tagId, dataTagQuality, value, sourceTimestamp, daqTimestamp, serverTimestamp, logTimestamp, description, null, mode);
  }

  /**
   *
   * @param tagValueUpdate the value to copy
   */
  public HistoryTagValueUpdateImpl(final TagValueUpdate tagValueUpdate) {
    this(
        tagValueUpdate.getId(),
        tagValueUpdate.getDataTagQuality(),
        tagValueUpdate.getValue(),
        tagValueUpdate.getSourceTimestamp(),
        tagValueUpdate.getDaqTimestamp(),
        tagValueUpdate.getServerTimestamp(),
        null,
        tagValueUpdate.getDescription(),
        tagValueUpdate.getAlarms().toArray(new AlarmValue[0]),
        tagValueUpdate.getMode());

    if (tagValueUpdate instanceof HistoryTagValueUpdateImpl) {
      HistoryTagValueUpdateImpl historyTagValueUpdate = (HistoryTagValueUpdateImpl) tagValueUpdate;
      this.valueClassName = historyTagValueUpdate.getValueClassName();
      this.initialValue = historyTagValueUpdate.isInitialValue();
    }
    else if (tagValueUpdate.getValue() != null){
      this.valueClassName = tagValueUpdate.getValue().getClass().getName();
    }
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public Long getId() {
    return this.updateId.getTagId();
  }


  @Override
  public boolean isSimulated() {
    return this.isSimulated;
  }

  /**
   * @return the string representation of the object
   */
  @Override
  public String toString() {
    final String logTimestamp;
    final String serverTimestamp;

    if (getLogTimestamp() != null) {
      logTimestamp = getLogTimestamp().toString();
    }
    else {
      logTimestamp = "";
    }

    if (getServerTimestamp() != null) {
      serverTimestamp = getServerTimestamp().toString();
    }
    else {
      serverTimestamp = "";
    }

    return String.format("Id: %d, Logdate: %s, ServerTime: %s", getId(), logTimestamp, serverTimestamp);
  }

  /**
   *
   * @return the time of when this update should execute
   */
  @Override
  public Timestamp getExecutionTimestamp() {
    return getServerTimestamp();
  }

  @Override
  public String getValueDescription() {
    return this.description;
  }

  /**
   * Creates a XML representation of this class by making use of
   * the simpleframework XML library.
   * @return The XML representation of this class
   * @see #fromXml(String)
   */
  public String getXml() {
      Serializer serializer = new Persister(new AnnotationStrategy());
      StringWriter fw = null;
      String result = null;

      try {
          fw = new StringWriter();
          serializer.write(this, fw);
          result = fw.toString();
      } catch (Exception e) {
          e.printStackTrace();
      } finally {
          if (fw != null) {
              try {
                  fw.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      }
      return result;
  }
}
