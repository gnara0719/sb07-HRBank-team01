package com.codeit_team01.sb07_hrbank_team01.employee.controller;

import com.codeit_team01.sb07_hrbank_team01.common.doc.EmployeeControllerDocs;
import com.codeit_team01.sb07_hrbank_team01.employee.dto.request.*;
import com.codeit_team01.sb07_hrbank_team01.employee.dto.response.EmployeeDistributionResponseDto;
import com.codeit_team01.sb07_hrbank_team01.employee.dto.response.EmployeePageResponseDto;
import com.codeit_team01.sb07_hrbank_team01.employee.dto.response.EmployeeResponseDto;
import com.codeit_team01.sb07_hrbank_team01.employee.dto.response.EmployeeTrendResponseDto;
import com.codeit_team01.sb07_hrbank_team01.employee.entity.EmployeeStatus;
import com.codeit_team01.sb07_hrbank_team01.employee.service.EmployeeService;
import com.codeit_team01.sb07_hrbank_team01.file.dto.FileCreateRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/employees")
public class EmployeeController implements EmployeeControllerDocs {
    private final EmployeeService employeeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EmployeeResponseDto createEmployee(
            @Valid @RequestPart("employee") EmployeeCreateRequestDto employee,
            @RequestPart(value = "profile", required = false) MultipartFile profile) {
        FileCreateRequestDto profileDto = null;
        if(profile != null) {
            profileDto = FileCreateRequestDto.from(profile);
        }
        return employeeService.createEmployee(employee, profileDto);
    }

    @GetMapping(value = "/{id}")
    public EmployeeResponseDto getEmployee(@PathVariable("id") Long id) {
        return employeeService.getEmployee(id);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EmployeeResponseDto updateEmployee(
            @PathVariable("id") Long id,
            @Valid @RequestPart("employee") EmployeeUpdateRequestDto employee,
            @RequestPart(value = "profile", required = false) MultipartFile profile) {
        FileCreateRequestDto profileDto = null;
        if(profile != null) {
            profileDto = FileCreateRequestDto.from(profile);
        }
        return employeeService.updateEmployee(employee, profileDto, id);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployee(@PathVariable("id") Long id) {
        employeeService.deleteEmployee(id);
    }

    @GetMapping
    public EmployeePageResponseDto getEmployees(
            @RequestParam(required = false) String nameOrEmail,
            @RequestParam(required = false) String employeeNumber,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String position,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hireDateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hireDateTo,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) Long idAfter,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        EmployeeSearchConditionDto condition = new EmployeeSearchConditionDto(
                nameOrEmail,
                departmentName,
                position,
                employeeNumber,
                hireDateFrom,
                hireDateTo,
                status
        );

        EmployeeSortCondition sortCondition = toSortCondition(sortField);
        EmployeeSortDirection direction = toSortDirection(sortDirection);

        EmployeeSearchPageRequestDto pageRequestDto = new EmployeeSearchPageRequestDto(
                condition,
                sortCondition,
                idAfter,
                size,
                direction,
                cursor

        );
        return employeeService.getEmployeesByPageSearch(pageRequestDto);
    }

    @GetMapping(value = "/stats/trend")
    public List<EmployeeTrendResponseDto> getEmployeesTrend(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "month") String unit
    ) {
        return employeeService.getEmployeeTrend(from, to, unit);
    }


    @GetMapping(value = "/stats/distribution")
    public List<EmployeeDistributionResponseDto> getEmployeesDistribution(
            @RequestParam(required = false, defaultValue = "department") String groupBy,
            @RequestParam(required = false) EmployeeStatus status
    ) {
        return employeeService.getEmployeeDistribution(groupBy, status);
    }

    @GetMapping(value = "/count")
    public long  getEmployeeCount(
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return employeeService.getEmployeeCount(status, fromDate, toDate);
    }

    private EmployeeSortCondition toSortCondition(String sortField) {
        return switch (sortField) {
            case "name" -> EmployeeSortCondition.NAME;
            case "hireDate" -> EmployeeSortCondition.HIRE_DATE;
            case "employeeNumber" -> EmployeeSortCondition.EMPLOYEE_NUMBER;
            default -> throw new IllegalArgumentException("지원하지 않는 정렬입니다." + sortField);
        };
    }

    private EmployeeSortDirection toSortDirection(String sortDirection) {
        return switch (sortDirection) {
            case "asc" -> EmployeeSortDirection.ASC;
            case "desc" -> EmployeeSortDirection.DESC;
            default -> throw new IllegalArgumentException("지원하지 않는 방향입니다." + sortDirection);
        };
    }
}
