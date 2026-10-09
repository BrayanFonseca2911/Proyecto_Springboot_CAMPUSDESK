package com.example.CAMPUSDESK.Entity;

import com.example.CAMPUSDESK.Enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** Historial inmutable de cambios de estado de un ticket (RF-06). */
@Entity
@Table(name = "ticket_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"ticket", "usuarioResponsable"})
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, foreignKey = @ForeignKey(name = "fk_history_ticket"))
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 20)
    private TicketStatus estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private TicketStatus estadoNuevo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_responsable_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_history_responsable"))
    private User usuarioResponsable;

    @CreationTimestamp
    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;
}
