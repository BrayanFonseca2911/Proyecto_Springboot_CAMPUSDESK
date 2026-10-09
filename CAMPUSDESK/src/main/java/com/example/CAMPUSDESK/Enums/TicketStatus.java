package com.example.CAMPUSDESK.Enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Estados del ticket y flujo estricto del ciclo de vida (RF-04):
 * ABIERTA -> ASIGNADA -> EN_PROCESO -> RESUELTA -> CERRADA
 */
public enum TicketStatus {
    ABIERTA,
    ASIGNADA,
    EN_PROCESO,
    RESUELTA,
    CERRADA;

    /** Mapa de transiciones válidas: solo se permite avanzar un paso. */
    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
            ABIERTA,   EnumSet.of(ASIGNADA),
            ASIGNADA,  EnumSet.of(EN_PROCESO),
            EN_PROCESO,EnumSet.of(RESUELTA),
            RESUELTA,  EnumSet.of(CERRADA),
            CERRADA,   EnumSet.noneOf(TicketStatus.class)
    );

    public boolean canTransitionTo(TicketStatus next) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(TicketStatus.class)).contains(next);
    }

    /** Un ticket cerrado es inmutable (RF-04 regla 6). */
    public boolean isClosed() {
        return this == CERRADA;
    }
}
