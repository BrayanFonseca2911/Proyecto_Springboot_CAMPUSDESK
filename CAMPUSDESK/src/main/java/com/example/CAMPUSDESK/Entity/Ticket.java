package com.example.CAMPUSDESK.Entity;

import com.example.CAMPUSDESK.Enums.Category;
import com.example.CAMPUSDESK.Enums.Priority;
import com.example.CAMPUSDESK.Enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Entidad Ticket / Incidencia (RF-03, RF-04). */
@Entity
@Table(name = "ticket")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"solicitante", "tecnicoAsignado", "comentarios", "historial"})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus estado;

    /** Solicitante (FK obligatoria). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ticket_solicitante"))
    private User solicitante;

    /** Técnico asignado (opcional hasta que el ADMIN lo asigne). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_asignado_id",
            foreignKey = @ForeignKey(name = "fk_ticket_tecnico"))
    private User tecnicoAsignado;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comentarios = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<History> historial = new ArrayList<>();
}
