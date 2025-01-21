package ru.seller_support.assignment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.service.MarketplaceProcessor;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final MarketplaceProcessor marketplaceProcessor;

    @GetMapping("/posting")
    public List<PostingInfoModel> test(@RequestParam("from") String from,
                                       @RequestParam("to") String to) {
        return marketplaceProcessor.getNewPostings(from, to);
    }

}
