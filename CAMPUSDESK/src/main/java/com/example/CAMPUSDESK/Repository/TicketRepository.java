package com.example.CAMPUSDESK.Repository;

import com.example.CAMPUSDESK.Entity.Ticket;
import com.example.CAMPUSDESK.Enums.Category;
import com.example.CAMPUSDESK.Enums.Priority;
import com.example.CAMPUSDESK.Enums.TicketStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /** Tickets del solicitante autenticado (USER), con ordenamiento. */
    List<Ticket> findBySolicitanteId(Long solicitanteId, Sort sort);

    /** Tickets asignados al técnico autenticado (TECHNICIAN). */
    List<Ticket> findByTecnicoAsignadoId(Long tecnicoId, Sort sort);

    // ---------- Filtros combinables por scope (RF-08) ----------

    @Query("""
            SELECT t FROM Ticket t
            WHERE (:estado IS NULL OR t.estado = :estado)
              AND (:prioridad IS NULL OR t.prioridad = :prioridad)
              AND (:categoria IS NULL OR t.categoria = :categoria)
            """)
    List<Ticket> filterAll(@Param("estado") TicketStatus estado,
                           @Param("prioridad") Priority prioridad,
                           @Param("categoria") Category categoria,
                           Sort sort);

    @Query("""
            SELECT t FROM Ticket t
            WHERE t.solicitante.id = :solicitanteId
              AND (:estado IS NULL OR t.estado = :estado)
              AND (:prioridad IS NULL OR t.prioridad = :prioridad)
              AND (:categoria IS NULL OR t.categoria = :categoria)
            """)
    List<Ticket> filterBySolicitante(@Param("solicitanteId") Long solicitanteId,
                                     @Param("estado") TicketStatus estado,
                                     @Param("prioridad") Priority prioridad,
                                     @Param("categoria") Category categoria,
                                     Sort sort);

    @Query("""
            SELECT t FROM Ticket t
            WHERE t.tecnicoAsignado.id = :tecnicoId
              AND (:estado IS NULL OR t.estado = :estado)
              AND (:prioridad IS NULL OR t.prioridad = :prioridad)
              AND (:categoria IS NULL OR t.categoria = :categoria)
            """)
    List<Ticket> filterByTecnico(@Param("tecnicoId") Long tecnicoId,
                                 @Param("estado") TicketStatus estado,
                                 @Param("prioridad") Priority prioridad,
                                 @Param("categoria") Category categoria,
                                 Sort sort);

    // ---------- Métricas del dashboard en PostgreSQL (RF-07) ----------

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.solicitante.id = :userId OR t.tecnicoAsignado.id = :userId")
    long countScoped(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(t) FROM Ticket t
            WHERE (:userId IS NULL OR t.solicitante.id = :userId OR t.tecnicoAsignado.id = :userId)
              AND t.estado = :estado
            """)
    long countByEstadoScoped(@Param("userId") Long userId, @Param("estado") TicketStatus estado);
}
