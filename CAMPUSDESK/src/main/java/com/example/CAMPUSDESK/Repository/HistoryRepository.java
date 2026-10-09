package com.example.CAMPUSDESK.Repository;

import com.example.CAMPUSDESK.Entity.History;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Long> {

    /** Historial de un ticket en orden cronológico (RF-06, solo lectura). */
    List<History> findByTicketIdOrderByFechaHoraAsc(Long ticketId);

    List<History> findByTicketId(Long ticketId, Sort sort);
}
