package com.example.CAMPUSDESK.Controller;

import com.example.CAMPUSDESK.Dto.Response.DashboardMetricsDTO;
import com.example.CAMPUSDESK.Security.UserPrincipal;
import com.example.CAMPUSDESK.Service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Panel de indicadores (RF-07). */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboard", description = "Métricas del sistema calculadas en PostgreSQL")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Totales por estado: ADMIN global, USER/TECHNICIAN en su alcance")
    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsDTO> metrics(@AuthenticationPrincipal UserPrincipal me) {
        return ResponseEntity.ok(dashboardService.metrics(me));
    }
}
