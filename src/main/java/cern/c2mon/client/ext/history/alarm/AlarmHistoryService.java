/******************************************************************************
 * Copyright (C) 2010-2017 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.client.ext.history.alarm;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * This service allows querying {@link Alarm} history from the c2mon history database.
 *
 * @author Justin Lewis Salmon, Matthias Braeger
 */
public interface AlarmHistoryService extends JpaRepository<Alarm, Long>{
  /**
   * Find all historical alarm records for the given time span and the given alarm id
   * @param id alarm id
   * @param startTime start time to search for an alarm entry
   * @param endTime end time to search for an alarm entry
   * @param pageable The requested page
   * @return The requested page
   * @see #findAllDistinctByIdAndTimestampBetweenOrderByTimestamp(Long, LocalDateTime, LocalDateTime)
   */
  Page<Alarm> findAllDistinctByIdAndTimestampBetweenOrderByTimestamp(Long id, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

  /**
   * Find all historical alarm records for the given time span and the given alarm id
   * @param id alarm id
   * @param startTime start time to search for an alarm entry
   * @param endTime end time to search for an alarm entry
   * @return The resulting list
   * @see #findAllDistinctByIdAndTimestampBetweenOrderByTimestamp(Long, LocalDateTime, LocalDateTime, Pageable)
   */
  List<Alarm> findAllDistinctByIdAndTimestampBetweenOrderByTimestamp(Long id, LocalDateTime startTime, LocalDateTime endTime);

  /**
   * Find all historical alarm records for the given time span and the given alarm id
   * @param faultFamily The name of the fault family to which the alarm belongs to
   * @param faultMember The fault member name within the given fault family to which the alarm belongs to
   * @param faultCode The fault code id which identifies the alarm within the given fault member
   * @param startTime start time to search for an alarm entry
   * @param endTime end time to search for an alarm entry
   * @param pageable The requested page
   * @return The requested page
   */
  @Query("SELECT DISTINCT a FROM Alarm a WHERE "
           + "a.faultFamily = :faultFamily AND "
           + "a.faultMember = :faultMember AND "
           + "a.faultCode = :faultCode AND "
           + "a.timestamp BETWEEN :startTime AND :endTime "
           + "ORDER BY a.timestamp ASC")
  Page<Alarm> findAllDistinctInTimeSpanOrderByTimestamp(@Param("faultFamily") String faultFamily,
                                                        @Param("faultMember") String faultMember,
                                                        @Param("faultCode") int faultCode,
                                                        @Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime,
                                                        Pageable pageable);

  /**
   * Find all historical alarm records for the given time span and the given alarm id
   * @param faultFamily The name of the fault family to which the alarm belongs to
   * @param faultMember The fault member name within the given fault family to which the alarm belongs to
   * @param faultCode The fault code id which identifies the alarm within the given fault member
   * @param startTime start time to search for an alarm entry
   * @param endTime end time to search for an alarm entry
   * @param pageable The requested page
   * @return The requested page
   */
  @Query("SELECT DISTINCT a FROM Alarm a WHERE "
           + "a.faultFamily = :faultFamily AND "
           + "a.faultMember = :faultMember AND "
           + "a.faultCode = :faultCode AND "
           + "a.timestamp BETWEEN :startTime AND :endTime "
           + "ORDER BY a.timestamp ASC")
  List<Alarm> findAllDistinctInTimeSpanOrderByTimestamp(@Param("faultFamily") String faultFamily,
                                                        @Param("faultMember") String faultMember,
                                                        @Param("faultCode") int faultCode,
                                                        @Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);


  /**
   * Find all historical alarm records for the given time span
   * @param id alarm id
   * @param startTime start time to search for an alarm entry
   * @param pageable The requested page
   * @return The requested page
   */
  Page<Alarm> findAllDistinctByTimestampBetweenOrderByTimestamp(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

  /**
   * Find all historical alarm records for the given time span
   * @param id alarm id
   * @param startTime start time to search for an alarm entry
   * @param endTime end time to search for an alarm entry
   * @return The resulting list
   */
  List<Alarm> findAllDistinctByTimestampBetweenOrderByTimestamp(LocalDateTime startTime, LocalDateTime endTime);

  /**
   * Returns the last N records for a given alarm id
   * @param id alarm id
   * @param pageable Use e.g. <code>new PageResult(0, 100)</code> to retrieve the last 100 historical records for the given alarm
   * @return The page of requested alarms
   */
  Page<Alarm> findAllDistinctByIdOrderByTimestampDesc(Long id, Pageable pageable);

  /**
   * Returns the last N records for a given alarm, which has to be specified by the unique triplet: faultFamily, faultMember, faultCode
   * @param faultFamily The name of the fault family to which the alarm belongs to
   * @param faultMember The fault member name within the given fault family to which the alarm belongs to
   * @param faultCode The fault code id which identifies the alarm within the given fault member
   * @param pageable Use e.g. <code>new PageResult(0, 100)</code> to retrieve the last 100 historical records for the given alarm
   * @return The page of requested alarms
   * @see #findAllDistinctByIdOrderByTimestampDesc(Long, Pageable)
   */
  Page<Alarm> findAllDistinctByFaultFamilyAndFaultMemberAndFaultCodeOrderByTimestampDesc(String faultFamily, String faultMember, int faultCode, Pageable pageable);
}
