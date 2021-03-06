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

import java.sql.Timestamp;

import cern.c2mon.client.ext.history.common.HistorySupervisionEvent;
import cern.c2mon.client.ext.history.common.id.SupervisionEventId;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

/**
 * This class implement the {@link SupervisionEvent} and is used when the event
 * is retrieved from the history
 *
 * @author vdeila
 *
 */
public class HistorySupervisionEventImpl implements HistorySupervisionEvent {

  /** The id of the supervision event */
  private final SupervisionEventId id;

  /** one state from the <code>SupervisionStatus</code> enumeration */
  private final SupervisionStatus status;
  /** time of the event */
  private final Timestamp eventTime;
  /** Free text for describing this event */
  private final String message;

  /** <code>true</code> if the value is an initial value */
  private boolean initialValue = false;

  /**
   * Constructor.
   *
   * @param entity
   *          The entity for which this supervision event is created
   * @param entityId
   *          The id of the entity
   * @param status
   *          one state from the <code>SupervisionStatus</code> enumeration
   * @param eventTime
   *          time of the event
   * @param message
   *          Free text for describing this event
   */
  public HistorySupervisionEventImpl(final SupervisionEntity entity, final Long entityId, final SupervisionStatus status, final Timestamp eventTime,
      final String message) {
    this(new SupervisionEventId(entity, entityId), status, eventTime, message);
  }

  /**
   * Constructor.
   *
   * @param identity
   *          The identify of the supervision event
   * @param status
   *          one state from the <code>SupervisionStatus</code> enumeration
   * @param eventTime
   *          time of the event
   * @param message
   *          Free text for describing this event
   */
  public HistorySupervisionEventImpl(final SupervisionEventId identity, final SupervisionStatus status, final Timestamp eventTime,
      final String message) {
    this.id = identity;
    this.status = status;
    this.eventTime = eventTime;
    this.message = message;
  }

  /**
   * @return the entity
   */
  @Override
  public SupervisionEntity getEntity() {
    return this.id.getEntity();
  }

  /**
   * @return the entityId
   */
  @Override
  public Long getEntityId() {
    return this.id.getEntityId();
  }

  /**
   * @return the status
   */
  @Override
  public SupervisionStatus getStatus() {
    return status;
  }

  /**
   * @return the eventTime
   */
  @Override
  public Timestamp getEventTime() {
    return eventTime;
  }

  /**
   * @return the message
   */
  @Override
  public String getMessage() {
    return message;
  }


  /**
   *
   * @return a clone of this
   */
  @Override
  public SupervisionEvent clone() {
    final HistorySupervisionEventImpl clone = new HistorySupervisionEventImpl(id.getEntity(), id.getEntityId(), status, eventTime, message);
    clone.initialValue = initialValue;
    return clone;
  }

  /**
   * @return the time of when this update should execute
   */
  @Override
  public Timestamp getExecutionTimestamp() {
    return getEventTime();
  }

  @Override
  public SupervisionEventId getUpdateId() {
    return this.id;
  }

  /**
   *
   * @return <code>true</code> if the value is an initial value
   */
  @Override
  public boolean isInitialValue() {
    return initialValue;
  }

  /**
   * @param initialValue <code>true</code> if the value is an initial value
   */
  public void setInitialValue(final boolean initialValue) {
    this.initialValue = initialValue;
  }

  @Override
  public String toString() {
    return String.format("HistorySupervisionEventImpl [eventTime=%s, id=%s, status=%s]", eventTime, id, status);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((eventTime == null) ? 0 : eventTime.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((message == null) ? 0 : message.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof SupervisionEvent))
      return false;
    SupervisionEvent other = (SupervisionEvent) obj;
    if (eventTime == null) {
      if (other.getEventTime() != null)
        return false;
    }
    else if (!eventTime.equals(other.getEventTime()))
      return false;
    if (id == null || id.getEntityId() == null) {
      if (other.getEntityId() != null)
        return false;
    }
    else if (!id.getEntityId().equals(other.getEntityId()))
      return false;
    if (id.getEntity() == null) {
      if (other.getEntity() != null)
        return false;
    }
    else if (!id.getEntity().equals(other.getEntity()))
      return false;
    if (message == null) {
      if (other.getMessage() != null)
        return false;
    }
    else if (!message.equals(other.getMessage()))
      return false;
    if (status == null) {
      if (other.getStatus() != null)
        return false;
    }
    else if (!status.equals(other.getStatus()))
      return false;
    return true;
  }

  @Override
  public String getName() {
    return null;
  }



}
