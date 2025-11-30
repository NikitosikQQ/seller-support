package ru.seller_support.assignment.controller.callback.ozon;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.seller_support.assignment.controller.dto.request.callback.ozon.OzonCallbackRequest;
import ru.seller_support.assignment.controller.dto.request.callback.ozon.OzonCallbackResponse;
import ru.seller_support.assignment.facade.OzonCallbackFacade;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ozon")
public class OzonCallbackController {

    private final OzonCallbackFacade facade;

    @PostMapping(path = "/callback")
    public ResponseEntity<OzonCallbackResponse> processCallback(@RequestBody OzonCallbackRequest request) {
        var result = facade.process(request);
        return ResponseEntity.ok(result);
    }
}
