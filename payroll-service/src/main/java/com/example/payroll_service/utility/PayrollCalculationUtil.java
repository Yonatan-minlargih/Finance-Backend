package com.example.payroll_service.utility;

import com.example.payroll_service.enums.CalculationMethod;
import com.example.payroll_service.model.EmployeeSalaryComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@Slf4j
public class PayrollCalculationUtil {

    private static final int DECIMAL_SCALE = 2;

    public BigDecimal calculateGrossSalary(BigDecimal basicSalary, BigDecimal overtime, BigDecimal bonus, List<EmployeeSalaryComponent> earnings) {
        BigDecimal gross = basicSalary != null ? basicSalary : BigDecimal.ZERO;
        gross = gross.add(overtime != null ? overtime : BigDecimal.ZERO);
        gross = gross.add(bonus != null ? bonus : BigDecimal.ZERO);

        for (EmployeeSalaryComponent component : earnings) {
            if (Boolean.TRUE.equals(component.getIsActive()) && component.getType() != null && Boolean.TRUE.equals(component.getType().getIsEarning())) {
                gross = gross.add(calculateComponentValue(component, gross));
            }
        }

        return gross.setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateNetSalary(BigDecimal grossSalary, List<EmployeeSalaryComponent> deductions, BigDecimal loanDeduction) {
        BigDecimal net = grossSalary != null ? grossSalary : BigDecimal.ZERO;

        for (EmployeeSalaryComponent component : deductions) {
            if (Boolean.TRUE.equals(component.getIsActive()) && component.getType() != null && Boolean.FALSE.equals(component.getType().getIsEarning())) {
                net = net.subtract(calculateComponentValue(component, grossSalary));
            }
        }

        if (loanDeduction != null) {
            net = net.subtract(loanDeduction);
        }

        return net.setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateComponentValue(EmployeeSalaryComponent component, BigDecimal baseAmount) {
        if (component.getType() == null || component.getAmount() == null) {
            return BigDecimal.ZERO;
        }

        if (component.getType().getCalculationMethod() == CalculationMethod.PERCENTAGE) {
            return applyPercentage(baseAmount, component.getAmount());
        } else {
            return applyFixedAmount(component.getAmount());
        }
    }

    public BigDecimal applyPercentage(BigDecimal baseAmount, BigDecimal percentage) {
        if (baseAmount == null || percentage == null) {
            return BigDecimal.ZERO;
        }
        return baseAmount.multiply(percentage)
                .divide(BigDecimal.valueOf(100), DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal applyFixedAmount(BigDecimal amount) {
        return amount != null ? amount.setScale(DECIMAL_SCALE, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal calculateOvertime(BigDecimal hourlyRate, BigDecimal overtimeHours) {
        if (hourlyRate == null || overtimeHours == null) {
            return BigDecimal.ZERO;
        }
        return hourlyRate.multiply(overtimeHours)
                .multiply(BigDecimal.valueOf(1.5))
                .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateLoanInstallment(BigDecimal loanAmount, BigDecimal interestRate, int numberOfInstallments) {
        if (loanAmount == null || numberOfInstallments <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalAmount = loanAmount;
        if (interestRate != null && interestRate.compareTo(BigDecimal.ZERO) > 0) {
            totalAmount = loanAmount.multiply(BigDecimal.ONE.add(interestRate.divide(BigDecimal.valueOf(100))));
        }

        return totalAmount.divide(BigDecimal.valueOf(numberOfInstallments), DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalEarnings(List<EmployeeSalaryComponent> components, BigDecimal baseSalary) {
        BigDecimal total = baseSalary != null ? baseSalary : BigDecimal.ZERO;

        for (EmployeeSalaryComponent component : components) {
            if (Boolean.TRUE.equals(component.getIsActive()) && component.getType() != null && Boolean.TRUE.equals(component.getType().getIsEarning())) {
                total = total.add(calculateComponentValue(component, baseSalary));
            }
        }

        return total.setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalDeductions(List<EmployeeSalaryComponent> components, BigDecimal baseSalary) {
        BigDecimal total = BigDecimal.ZERO;

        for (EmployeeSalaryComponent component : components) {
            if (Boolean.TRUE.equals(component.getIsActive()) && component.getType() != null && Boolean.FALSE.equals(component.getType().getIsEarning())) {
                total = total.add(calculateComponentValue(component, baseSalary));
            }
        }

        return total.setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);
    }
}
