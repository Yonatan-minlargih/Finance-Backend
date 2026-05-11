package com.example.cost_service.controller;

import com.example.cost_service.dto.request.WithholdingTaxRuleRequest;
import com.example.cost_service.dto.response.WithholdingTaxRuleResponse;
import com.example.cost_service.service.WithholdingTaxRuleService;
import com.example.cost_service.utility.PermissionEvaluator;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/withholding-tax-rule/{tenant-id}")
@RequiredArgsConstructor
@Tag(name = "Withholding Tax Rule")
public class WithholdingTaxRuleController {

    private final WithholdingTaxRuleService withholdingTaxRuleService;
    private final PermissionEvaluator permissionEvaluator;

    @PostMapping("/add")
    public ResponseEntity<?> addWithholdingTaxRule(
            @PathVariable("tenant-id") UUID tenantId,
            @RequestBody WithholdingTaxRuleRequest request) {

        permissionEvaluator.addWithholdingTaxRulePermission(tenantId);

        WithholdingTaxRuleResponse response = withholdingTaxRuleService.addWithholdingTaxRule(tenantId, request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllWithholdingTaxRules(
            @PathVariable("tenant-id") UUID tenantId) {

        permissionEvaluator.getAllWithholdingTaxRulesPermission(tenantId);

        List<WithholdingTaxRuleResponse> withholdingTaxRules = withholdingTaxRuleService.getAllWithholdingTaxRules(tenantId);
        return ResponseEntity.ok(withholdingTaxRules);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getWithholdingTaxRuleById(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID withholdingTaxRuleId) {

        permissionEvaluator.getWithholdingTaxRuleByIdPermission(tenantId, withholdingTaxRuleId);

        return ResponseEntity.ok(withholdingTaxRuleService.getWithholdingTaxRuleById(tenantId, withholdingTaxRuleId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateWithholdingTaxRule(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID withholdingTaxRuleId,
            @RequestBody WithholdingTaxRuleRequest request) {

        permissionEvaluator.updateWithholdingTaxRulePermission(tenantId);

        return ResponseEntity.ok(withholdingTaxRuleService.updateWithholdingTaxRule(tenantId, withholdingTaxRuleId, request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteWithholdingTaxRule(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID withholdingTaxRuleId) {

        permissionEvaluator.deleteWithholdingTaxRulePermission(tenantId);

        withholdingTaxRuleService.deleteWithholdingTaxRule(tenantId, withholdingTaxRuleId);
        return ResponseEntity.ok("Deleted successfully");
    }
}
