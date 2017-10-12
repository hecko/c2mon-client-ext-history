package cern.c2mon.client.ext.history.supervision;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Manuel Bouzas Reguera
 */
public interface SupervisionEventRepository extends JpaRepository<ServerSupervisionEvent, Long>{
    List<ServerSupervisionEvent> findByIdAndEventTimeBetween(Long id, Date from, Date to);
}
