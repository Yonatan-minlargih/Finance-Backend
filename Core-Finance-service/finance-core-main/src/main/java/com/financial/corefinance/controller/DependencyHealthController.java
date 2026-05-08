package com.financial.corefinance.controller;

import com.financial.corefinance.service.FinanceDependencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integration/dependencies")
@RequiredArgsConstructor
@Tag(name = "Dependency Health", description = "Health checks for finance domain dependencies")
public class DependencyHealthController {

    private final FinanceDependencyService financeDependencyService;

    @GetMapping("/health")
    @PreAuthorize("hasRole('SYSTEM') or hasRole('INTEGRATION_SERVICE') or hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Check downstream finance services health")
    public ResponseEntity<Map<String, String>> checkDependenciesHealth() {
        return ResponseEntity.ok(financeDependencyService.dependenciesHealth());
    }
}
