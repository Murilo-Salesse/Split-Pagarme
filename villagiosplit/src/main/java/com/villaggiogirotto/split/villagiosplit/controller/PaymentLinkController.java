package com.villaggiogirotto.split.villagiosplit.controller;

import com.villaggiogirotto.split.villagiosplit.controller.requests.CreatePaymentRequest;
import com.villaggiogirotto.split.villagiosplit.service.PagarmePaymentLinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller para criação de Payment Links
 * 
 * Endpoint principal: POST /
 * 
 * Gera um link de checkout da Pagar.me onde o cliente
 * preenche seus dados e realiza o pagamento.
 */
@RestController
@RequestMapping("/")
public class PaymentLinkController {

    private final PagarmePaymentLinkService paymentLinkService;

    public PaymentLinkController(PagarmePaymentLinkService paymentLinkService) {
        this.paymentLinkService = paymentLinkService;
    }

    /**
     * Cria um Payment Link com configuração de split
     *
     * O link retornado pode ser enviado ao cliente para que ele
     * preencha seus dados de pagamento (nome, email, cartão, etc).
     *
     * @param req Dados do pagamento (valor, itens, split)
     * @return URL de checkout e dados do link criado
     */
    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> createPaymentLink(@RequestBody CreatePaymentRequest req) {
        return paymentLinkService.createPaymentLink(req)
                .map(linkResponse -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("payment_link", linkResponse);

                    // Extrair URL de checkout da resposta
                    String checkoutUrl = linkResponse.path("url").asText(null);
                    if (checkoutUrl != null && !checkoutUrl.isEmpty()) {
                        response.put("checkout_url", checkoutUrl);
                    }

                    // Extrair ID do link
                    String linkId = linkResponse.path("id").asText(null);
                    if (linkId != null) {
                        response.put("link_id", linkId);
                    }

                    // Extrair short_url se disponível
                    String shortUrl = linkResponse.path("short_url").asText(null);
                    if (shortUrl != null && !shortUrl.isEmpty()) {
                        response.put("short_url", shortUrl);
                    }

                    return ResponseEntity.ok(response);
                })
                .onErrorResume(ex -> {
                    ex.printStackTrace();
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("error", ex.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                });
    }
}
