package com.example.CAMPUSDESK.Dto.Response;

/** Métricas del dashboard calculadas en PostgreSQL (RF-07). */
public record DashboardMetricsDTO(
        long totalTickets,
        long abiertas,
        long asignadas,
        long enProceso,
        long resueltas,
        long cerradas
) {}
