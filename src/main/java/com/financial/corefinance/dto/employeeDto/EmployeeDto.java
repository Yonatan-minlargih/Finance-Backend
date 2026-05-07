package com.financial.corefinance.dto.employeeDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {

    private UUID id;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String department;
    private String position;
    private String costCenter;
    private String managerId;
    private String tenantId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate terminationDate;
    
    private String status; // ACTIVE, INACTIVE, TERMINATED
    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT
    private BigDecimal salary;
    private String currency;
    private List<String> roles;
    private List<String> permissions;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime updatedAt;
    
    private String createdBy;
    private String updatedBy;
}
