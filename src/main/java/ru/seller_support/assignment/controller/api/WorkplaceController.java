package ru.seller_support.assignment.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.seller_support.assignment.controller.dto.request.workplace.WorkplaceChangeRequest;
import ru.seller_support.assignment.controller.dto.request.workplace.WorkplaceMaterialRatesChangeBody;
import ru.seller_support.assignment.controller.dto.request.workplace.WorkplaceMaterialRatesDeleteBody;
import ru.seller_support.assignment.controller.dto.request.workplace.WorkplaceMaterialRatesSaveBody;
import ru.seller_support.assignment.controller.dto.response.MaterialCoefficientResponseDto;
import ru.seller_support.assignment.controller.dto.response.WorkplaceDto;
import ru.seller_support.assignment.domain.enums.Workplace;
import ru.seller_support.assignment.service.WorkplaceService;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

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

    @GetMapping(path = "/coefficients")
    public List<MaterialCoefficientResponseDto> getCoefficientsByWorkplace(@RequestParam("workplace") String workplace) {
        return workplaceService.findCoefficientPerMaterialsByWorkplace(Workplace.fromValue(workplace));
    }

    @PostMapping(path = "/coefficients")
    public UUID saveCoefficients(@RequestBody @Valid WorkplaceMaterialRatesSaveBody request) {
        return workplaceService.saveCoefficientRatePerMaterial(request);
    }

    @PatchMapping(path = "/coefficients")
    public void changeCoefficients(@RequestBody @Valid List<WorkplaceMaterialRatesChangeBody> request) {
       workplaceService.changeCoefficientRatePerMaterial(request);
    }

    @DeleteMapping(path = "/coefficients")
    public void deleteCoefficients(@RequestBody @Valid List<WorkplaceMaterialRatesDeleteBody> request) {
        request.forEach(workplaceService::deleteCoefficientRates);
    }
}
