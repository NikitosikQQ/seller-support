package ru.seller_support.assignment.service.employee;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.controller.dto.request.employee.EmployeeDto;
import ru.seller_support.assignment.domain.enums.Workplace;
import ru.seller_support.assignment.service.UserService;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeValidationService {

    private final UserService userService;

    public void validateEmployees(List<EmployeeDto> employees) {
        if (employees.size() > 1) {
            employees.forEach(emp -> {
                if (!emp.getWorkplace().equalsIgnoreCase(Workplace.PILA1.getValue()) &&
                        !emp.getWorkplace().equalsIgnoreCase(Workplace.PILA2.getValue())) {
                    throw new ValidationException("Только распиловщики могут работать совместно");
                }
            });
        }

        if (employees.size() == 1 &&
                (employees.getFirst().getWorkplace().equalsIgnoreCase(Workplace.PILA1.getValue())
                        || employees.getFirst().getWorkplace().equalsIgnoreCase(Workplace.PILA2.getValue()))) {
            throw new ValidationException("Распиловщик1 или распиловщик2 могут работать только совместно, а не по одному");
        }

        var usernamesEmployeesMap = employees.stream()
                .collect(Collectors.toMap(EmployeeDto::getUsername, Function.identity()));
        var existingUsers = userService.findUsersByUsernames(usernamesEmployeesMap.keySet());
        if (existingUsers.size() != employees.size()) {
            throw new ValidationException("Некоторые работники не найдены в системе seller-supp");
        }
        existingUsers.forEach(user -> {
            var employee = usernamesEmployeesMap.get(user.getUsername());
            var workplace = employee.getWorkplace();
            if (!user.getWorkplaces().contains(Workplace.fromValue(workplace))) {
                throw new ValidationException(String.format("Работник %s не может работать как %s, "
                                + "необходимо пополнить его список доступных рабочих мест в seller-supp",
                        user.getUsername(), workplace));
            }
        });

    }
}
