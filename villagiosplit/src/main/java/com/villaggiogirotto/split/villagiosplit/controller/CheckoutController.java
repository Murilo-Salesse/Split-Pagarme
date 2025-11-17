package com.villaggiogirotto.split.villagiosplit.controller;

import com.villaggiogirotto.split.villagiosplit.controller.requests.CreatePaymentRequest;
import com.villaggiogirotto.split.villagiosplit.service.PagarmeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("")
public class CheckoutController {

    private final PagarmeService pagarmeService;

    public CheckoutController(PagarmeService pagarmeService) {
        this.pagarmeService = pagarmeService;
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, String>>> create(@RequestBody CreatePaymentRequest req) {
        return pagarmeService.createCheckout(req)
                .map(url -> ResponseEntity.ok(Map.of("checkout_url", url)))
                .onErrorResume(ex -> {
                    ex.printStackTrace();
                    return Mono.just(ResponseEntity.badRequest().body(Map.of("error", ex.getMessage())));
                });
    }
}
