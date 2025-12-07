package com.codeit_team01.sb07_hrbank_team01.employee.service;

import com.codeit_team01.sb07_hrbank_team01.department.entity.Department;
import com.codeit_team01.sb07_hrbank_team01.department.repository.DepartmentRepository;
import com.codeit_team01.sb07_hrbank_team01.employee.dto.request.EmployeeCreateRequestDto;
import com.codeit_team01.sb07_hrbank_team01.employee.dto.request.EmployeeSearchConditionDto;
import com.codeit_team01.sb07_hrbank_team01.employee.dto.request.EmployeeSearchPageRequestDto;
import com.codeit_team01.sb07_hrbank_team01.employee.dto.request.EmployeeUpdateRequestDto;
import com.codeit_team01.sb07_hrbank_team01.employee.dto.response.EmployeeDistributionResponseDto;
import com.codeit_team01.sb07_hrbank_team01.employee.dto.response.EmployeePageResponseDto;
import com.codeit_team01.sb07_hrbank_team01.employee.dto.response.EmployeeResponseDto;
import com.codeit_team01.sb07_hrbank_team01.employee.dto.response.EmployeeTrendResponseDto;
import com.codeit_team01.sb07_hrbank_team01.employee.entity.Employee;
import com.codeit_team01.sb07_hrbank_team01.employee.entity.EmployeeStatus;
import com.codeit_team01.sb07_hrbank_team01.employee.mapper.EmployeeMapper;
import com.codeit_team01.sb07_hrbank_team01.employee.repository.EmployeeRepository;
import com.codeit_team01.sb07_hrbank_team01.file.dto.FileCreateRequestDto;
import com.codeit_team01.sb07_hrbank_team01.file.dto.FileResponseDto;
import com.codeit_team01.sb07_hrbank_team01.file.entity.MetaFile;
import com.codeit_team01.sb07_hrbank_team01.file.repository.MetaFileRepository;
import com.codeit_team01.sb07_hrbank_team01.file.service.MetaFileService;
import com.codeit_team01.sb07_hrbank_team01.history.dto.requestDto.HistoryEmployeeCopyDto;
import com.codeit_team01.sb07_hrbank_team01.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final DepartmentRepository departmentRepository;
    private final MetaFileRepository metaFileRepository;
    private final MetaFileService metaFileService;
    private final HistoryService historyService; //test

    @Override
    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeCreateRequestDto employeeCreateRequestDto,
                                              FileCreateRequestDto fileCreateRequestDto) {
        Objects.requireNonNull(employeeCreateRequestDto, "요청이 null일 수 없습니다.");

        if (employeeRepository.existsByEmailIgnoreCase(employeeCreateRequestDto.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Department department = departmentRepository.findById(employeeCreateRequestDto.departmentId())
                .orElseThrow(() -> new NoSuchElementException("부서를 찾을 수 없습니다."));

        MetaFile profile = null;
        if (fileCreateRequestDto != null) {
            FileResponseDto file = metaFileService.createFile(fileCreateRequestDto);
            profile = metaFileRepository.getReferenceById(file.id());
        }

        Instant hireDate = employeeCreateRequestDto.hireDate()
                .atStartOfDay(ZoneId.systemDefault()).toInstant();

        Employee newEmployee = Employee.builder()
                .name(employeeCreateRequestDto.name())
                .email(employeeCreateRequestDto.email())
                .jobPosition(employeeCreateRequestDto.position())
                .department(department)
                .profile(profile)
                .hireDate(hireDate)
                .build();

        Employee save = employeeRepository.save(newEmployee);

        int year = employeeCreateRequestDto.hireDate().getYear();
        String employeeNo = String.format(
                "%s-%d_%06d",
                department.getName(),
                year,
                save.getId()
        );

        save.updateEmployeeNo(employeeNo);

        historyService.createHistory(save,employeeCreateRequestDto.memo());//test
        return employeeMapper.toDto(save);
    }

    @Override
    @Transactional
    public EmployeeResponseDto updateEmployee(EmployeeUpdateRequestDto employeeUpdateRequestDto,
                                              FileCreateRequestDto fileCreateRequestDto,
                                              Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("일치하는 사원이 없습니다."));

        HistoryEmployeeCopyDto beforeEmployee = HistoryEmployeeCopyDto.from(employee);//test

        if (!employee.getEmail().equalsIgnoreCase(employeeUpdateRequestDto.email()) &&
                employeeRepository.existsByEmailIgnoreCaseAndIdNot(
                        employeeUpdateRequestDto.email(), employee.getId())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Department department = departmentRepository.findById(employeeUpdateRequestDto.departmentId())
                .orElseThrow(() -> new NoSuchElementException("일치하는 부서가 없습니다."));

        MetaFile newProfile = employee.getProfile();
        if (fileCreateRequestDto != null) {
            FileResponseDto fileDto = metaFileService.createFile(fileCreateRequestDto);
            newProfile = metaFileRepository.getReferenceById(fileDto.id());
        }

        Instant hireDate = employeeUpdateRequestDto.hireDate()
                .atStartOfDay(ZoneId.systemDefault()).toInstant();

        employee.updateInfo(
                employeeUpdateRequestDto.name(),
                employeeUpdateRequestDto.email(),
                employeeUpdateRequestDto.position(),
                department,
                hireDate,
                newProfile
        );

        if (employeeUpdateRequestDto.status() != null) {
            employee.changeStatus(employeeUpdateRequestDto.status());
        }
        historyService.updateHistory(beforeEmployee, employee,employeeUpdateRequestDto.memo());//test
        return employeeMapper.toDto(employee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("직원을 찾을 수 없습니다."));
        historyService.deleteHistory(employee,null);//test
        employeeRepository.delete(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployee(Long id) {
        Employee employee = employeeRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new NoSuchElementException("직원을 찾을 수 없습니다."));
        return employeeMapper.toDto(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDto> getEmployeesBySearch(EmployeeSearchConditionDto employeeSearchConditionDto) {
        List<Employee> employees = employeeRepository.search(employeeSearchConditionDto);
        return employees.stream()
                .map(employee -> employeeMapper.toDto(employee))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePageResponseDto getEmployeesByPageSearch(EmployeeSearchPageRequestDto employeeSearchPageRequestDto) {
        int pageSize = employeeSearchPageRequestDto.size();
        EmployeeSearchPageRequestDto pageRequestDto = new EmployeeSearchPageRequestDto(
                employeeSearchPageRequestDto.employeeSearchConditionDto(),
                employeeSearchPageRequestDto.sortField(),
                employeeSearchPageRequestDto.idAfter(),
                pageSize + 1,
                employeeSearchPageRequestDto.sortDirection(),
                employeeSearchPageRequestDto.cursor()
        );
        List<Employee> employees = employeeRepository.searchPage(pageRequestDto);

        boolean hasNext = employees.size() > pageSize;
        if(hasNext) {
            employees = employees.subList(0, pageSize);
        }

        List<EmployeeResponseDto> content = employees.stream()
                .map(employee -> employeeMapper.toDto(employee))
                .toList();

        Long nextIdAfter = employees.isEmpty()
                ? null : employees.get(employees.size() - 1).getId();

        String nextCursor = nextIdAfter != null ? String.valueOf(nextIdAfter) : null;

        long totalElements = employeeRepository
                .countBySearchCondition(employeeSearchPageRequestDto.employeeSearchConditionDto());


        return new EmployeePageResponseDto(
                content,
                nextCursor,
                nextIdAfter,
                pageSize,
                totalElements,
                hasNext
        );
    }

    @Override
    public List<EmployeeTrendResponseDto> getEmployeeTrend(LocalDate from, LocalDate to, String unit) {
        LocalDate now = LocalDate.now();
        String unitValue = (unit == null || unit.isEmpty()) ? "month" : unit.toLowerCase();

        LocalDate fromDate;
        LocalDate toDate;

        if(from == null && to == null) {
            toDate = now;
            fromDate = switch (unitValue) {
                case "day" -> toDate.minusDays(11);
                case "week" -> toDate.minusWeeks(11);
                case "month" -> toDate.minusMonths(11);
                case "quarter" -> toDate.minusMonths(3L * 11);
                case "year" -> toDate.minusYears(11);
                default -> throw new IllegalArgumentException("지원하는 날짜가 아닙니다.");
            };
        } else if (from == null) {
            toDate = to;
            fromDate = switch (unitValue) {
                case "day" -> toDate.minusDays(11);
                case "week" -> toDate.minusWeeks(11);
                case "month" -> toDate.minusMonths(11);
                case "quarter" -> toDate.minusMonths(3L * 11);
                case "year" -> toDate.minusYears(11);
                default -> throw new IllegalArgumentException("지원하는 날짜가 아닙니다.");
            };
        } else if(to == null) {
            fromDate = from;
            toDate = now;
        } else {
            fromDate = from;
            toDate = to;
        }

        final String finalUnit = unitValue;
        final LocalDate finalFrom = fromDate;
        final LocalDate finalTo = toDate;

        EmployeeSearchConditionDto condition = new EmployeeSearchConditionDto(
                null,
                null,
                null,
                null,
                fromDate,
                toDate,
                null
        );
        List<Employee> employees = employeeRepository.search(condition);

        Map<LocalDate, Long> grouped = employees.stream()
                .map(employee -> toUnitDate(employee.getHireDate(), finalUnit))
                .filter(obj -> Objects.nonNull(obj))
                .filter(d -> !d.isBefore(finalFrom) && !d.isAfter(finalTo))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()
                ));

        List<LocalDate> localDates = buildUnitDates(fromDate, toDate, unitValue);
        List<EmployeeTrendResponseDto> result = new ArrayList<>();
        long prevCount = -1;
        for (LocalDate localDate : localDates) {
            long count = grouped.getOrDefault(localDate, 0L);

            long charge = 0;
            double chargeRate = 0.0;
            if (prevCount >= 0) {
                charge = count - prevCount;
                if (prevCount > 0) {
                    chargeRate = (charge * 100.0) / prevCount;
                }
            }

            result.add(new EmployeeTrendResponseDto(
                    localDate,
                    count,
                    charge,
                    chargeRate
            ));
            prevCount = count;
        }
        return result;
    }

    private LocalDate toUnitDate(Instant hireDate, String unit) {
        if(hireDate == null) {
            return null;
        }
        LocalDate date = hireDate.atZone(ZoneId.systemDefault()).toLocalDate();
        return switch (unit) {
            case "day" -> date;
            case "week" -> date.with(DayOfWeek.MONDAY);
            case "month" -> date.withDayOfMonth(1);
            case "quarter" -> LocalDate.of(
                    date.getYear(),
                    ((date.getMonthValue() - 1) / 3) * 3 + 1,
                    1);
            case "year" -> LocalDate.of(date.getYear(), 1, 1);
            default -> throw new IllegalArgumentException("지원하지 않는 날짜입니다.");
        };
    }


    private List<LocalDate> buildUnitDates(LocalDate from, LocalDate to, String unit) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate cursor = normalizeFrom(from, unit);

        while (!cursor.isAfter(to)) {
            dates.add(cursor);
            cursor = moveNext(cursor, unit);
        }
        return dates;
    }

    private LocalDate normalizeFrom(LocalDate from, String unit) {
        return switch (unit) {
            case "day" -> from;
            case "week" -> from.with(DayOfWeek.MONDAY);
            case "month" -> from.withDayOfMonth(1);
            case "quarter" -> {
                int m = from.getMonthValue();
                int startMonth = ((m - 1) / 3) * 3 + 1;
                yield LocalDate.of(from.getYear(), startMonth, 1);
            }
            case "year" -> LocalDate.of(from.getYear(), 1, 1);
            default -> throw new IllegalArgumentException("지원하지 않는 unit 입니다: " + unit);
        };
    }

    private LocalDate moveNext(LocalDate date, String unit) {
        return switch (unit) {
            case "day" -> date.plusDays(1);
            case "week" -> date.plusWeeks(1);
            case "month" -> date.plusMonths(1);
            case "quarter" -> date.plusMonths(3);
            case "year" -> date.plusYears(1);
            default -> throw new IllegalArgumentException("지원하지 않는 unit 입니다: " + unit);
        };
    }

    @Override
    public List<EmployeeDistributionResponseDto> getEmployeeDistribution(String groupBy, EmployeeStatus status) {
        EmployeeStatus employeeStatus = status != null ?
                status : EmployeeStatus.ACTIVE;

        return employeeRepository.findDistribution(groupBy, employeeStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public long getEmployeeCount(EmployeeStatus status, LocalDate fromDate, LocalDate toDate) {
        LocalDate now = LocalDate.now();
        LocalDate from = fromDate;
        LocalDate to = toDate;

        if (from != null && to == null) {
            to = now;
        }

        EmployeeSearchConditionDto employeeSearchConditionDto = new EmployeeSearchConditionDto(
                null,
                null,
                null,
                null,
                from,
                to,
                status
        );
        return employeeRepository.countBySearchCondition(employeeSearchConditionDto);
    }
}
