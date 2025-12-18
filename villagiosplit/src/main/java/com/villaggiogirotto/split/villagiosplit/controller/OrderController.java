package com.villaggiogirotto.split.villagiosplit.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.villaggiogirotto.split.villagiosplit.controller.requests.CreateOrderRequest;
import com.villaggiogirotto.split.villagiosplit.service.PagarmeOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final PagarmeOrderService orderService;

    public OrderController(PagarmeOrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Cria um pedido com split usando a API Orders do Pagar.me
     *
     * Métodos de pagamento suportados:
     * - credit_card: Cartão de crédito
     * - pix: PIX
     * - boleto: Boleto bancário
     * - debit_card: Cartão de débito
     */
    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> createOrder(@RequestBody CreateOrderRequest req) {
        return orderService.createOrder(req)
                .map(orderResponse -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("order", orderResponse);

                    // Extrair informações importantes baseado no método de pagamento
                    JsonNode charges = orderResponse.path("charges");
                    if (charges.isArray() && charges.size() > 0) {
                        JsonNode charge = charges.get(0);
                        JsonNode lastTransaction = charge.path("last_transaction");

                        String paymentMethod = lastTransaction.path("transaction_type").asText();

                        switch (paymentMethod) {
                            case "pix":
                                response.put("pix_qr_code", lastTransaction.path("qr_code").asText());
                                response.put("pix_qr_code_url", lastTransaction.path("qr_code_url").asText());
                                break;
                            case "boleto":
                                response.put("boleto_url", lastTransaction.path("url").asText());
                                response.put("boleto_barcode", lastTransaction.path("barcode").asText());
                                response.put("boleto_pdf", lastTransaction.path("pdf").asText());
                                break;
                            case "credit_card":
                            case "debit_card":
                                response.put("transaction_id", lastTransaction.path("id").asText());
                                response.put("status", charge.path("status").asText());
                                break;
                        }
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

    /**
     * Exemplo de endpoint para pagamento com PIX
     */
    @PostMapping("/pix")
    public Mono<ResponseEntity<Map<String, Object>>> createPixOrder(@RequestBody CreateOrderRequest req) {
        req.setPaymentMethod("pix");
        return createOrder(req);
    }

    /**
     * Exemplo de endpoint para pagamento com Boleto
     */
    @PostMapping("/boleto")
    public Mono<ResponseEntity<Map<String, Object>>> createBoletoOrder(@RequestBody CreateOrderRequest req) {
        req.setPaymentMethod("boleto");
        return createOrder(req);
    }

    /**
     * Exemplo de endpoint para pagamento com Cartão de Crédito
     */
    @PostMapping("/credit-card")
    public Mono<ResponseEntity<Map<String, Object>>> createCreditCardOrder(@RequestBody CreateOrderRequest req) {
        req.setPaymentMethod("credit_card");
        return createOrder(req);
    }
}