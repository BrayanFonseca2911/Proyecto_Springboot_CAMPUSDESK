package com.example.CAMPUSDESK.Service;

import com.example.CAMPUSDESK.Dto.Response.DashboardMetricsDTO;
import com.example.CAMPUSDESK.Security.UserPrincipal;

public interface DashboardService {
    /** ADMIN: métricas globales. USER/TECHNICIAN: métricas de su alcance (RF-07). */
    DashboardMetricsDTO metrics(UserPrincipal me);
}
