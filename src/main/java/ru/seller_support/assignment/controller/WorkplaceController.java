package ru.seller_support.assignment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.seller_support.assignment.controller.dto.request.workplace.WorkplaceChangeRequest;
import ru.seller_support.assignment.controller.dto.request.workplace.WorkplaceMaterialRatesChangeBody;
import ru.seller_support.assignment.controller.dto.response.WorkplaceDto;
import ru.seller_support.assignment.domain.enums.Workplace;
import ru.seller_support.assignment.service.WorkplaceService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workplaces")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class WorkplaceController {

    private final WorkplaceService workplaceService;

    @GetMapping(path = "/names")
    public List<String> getAllWorkplaceNames() {
        return Arrays.stream(Workplace.values())
                .map(Workplace::getValue)
                .sorted()
                .toList();
    }

    @GetMapping
    public List<WorkplaceDto> getAllWorkplacesWithRates() {
        var workplaces = workplaceService.findAll();
        return workplaces.stream()
                .map(workplace -> WorkplaceDto.builder()
                        .id(workplace.getId())
                        .workplace(workplace.getWorkplace().getValue())
                        .rate(workplace.getRate())
                        .build())
                .sorted(Comparator.comparing(WorkplaceDto::getWorkplace))
                .toList();

    }

    @PostMapping
    public void changeWorkplaceRate(@RequestBody @Valid WorkplaceChangeRequest request) {
        workplaceService.changeRate(request);
    }

    @GetMapping(path = "/coefficient")
    public Map<String, BigDecimal> getCoefficientsByWorkplace(@RequestParam("workplace") String workplace) {
        return workplaceService.findCoefficientPerMaterialsByWorkplace(Workplace.fromValue(workplace));
    }

    @PostMapping(path = "/coefficient")
    public void changeCoefficients(@RequestBody @Valid List<WorkplaceMaterialRatesChangeBody> request) {
        request.forEach(workplaceService::saveCoefficientRatePerMaterial);
    }
}
