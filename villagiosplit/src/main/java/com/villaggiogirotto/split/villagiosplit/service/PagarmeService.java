package com.villaggiogirotto.split.villagiosplit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.villaggiogirotto.split.villagiosplit.controller.requests.CreatePaymentRequest;
import com.villaggiogirotto.split.villagiosplit.dto.CartItemDTO;
import com.villaggiogirotto.split.villagiosplit.dto.SplitInputDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class PagarmeService {

    private final WebClient pagarmeWebClient;

    private static final int PIX_EXPIRES_IN_MINUTES = 50 * 24 * 60; // 50 dias -> 72000
    private static final int BOLETO_DUE_IN_DAYS = 50;

    public PagarmeService(WebClient pagarmeWebClient) {
        this.pagarmeWebClient = pagarmeWebClient;
    }

    public Mono<String> createCheckout(CreatePaymentRequest req) {

        Map<String, Object> payload = buildPayload(req);

        return pagarmeWebClient.post()
                .uri("/paymentlinks")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> {
                    // extrai URL (campo URL na resposta)
                    JsonNode urlNode = json.get("url");
                    if(urlNode != null && !urlNode.isNull()) {
                        return urlNode.asText();
                    } else {
                        throw new RuntimeException("checkout url not present in pagar.me response: " + json.toString());
                    }
                });
    }

    private Map<String, Object> buildPayload(CreatePaymentRequest req) {
        Map<String, Object> root = new HashMap<>();
        root.put("type", "order");

        //payments settings
        Map<String, Object> paymentSettings = new HashMap<>();
        paymentSettings.put("accepted_payment_methods", Arrays.asList("pix", "boleto", "credit_card"));

        Map<String, Object> pixSettings = new HashMap<>();
        pixSettings.put("expires_in", PIX_EXPIRES_IN_MINUTES);
        paymentSettings.put("pix_settings", pixSettings);

        Map<String, Object> billetSettings = new HashMap<>();
        billetSettings.put("due_in", BOLETO_DUE_IN_DAYS);
        paymentSettings.put("boleto_settings", billetSettings);

        Map<String, Object> ccSettings = new HashMap<>();
        ccSettings.put("operation_type", "auth_and_capture");

        Map<String, Object> installmentsSetup = new HashMap<>();
        // max installments fixed to 6 in setup; but respect front's installments for allowed choices
        installmentsSetup.put("max_installments", 6);
        installmentsSetup.put("amount", req.getAmount() != null ? req.getAmount() : 0);
        installmentsSetup.put("interest_type", "simple");
        installmentsSetup.put("interest_rate", 0);

        // make all installments "free" up to max desired
        installmentsSetup.put("free_installments", 6);
        ccSettings.put("installments_setup", installmentsSetup);

        paymentSettings.put("credit_card_settings", ccSettings);

        root.put("payment_settings", paymentSettings);

        // cart_settings
        Map<String, Object> cartSettings = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();

        if (req.getItems() != null && !req.getItems().isEmpty()) {
            for (CartItemDTO it: req.getItems()) {
                Map<String, Object> m = new HashMap<>();
                m.put("name", it.getName() != null ? it.getName() : "Item");
                m.put("description", it.getDescription() != null ? it.getDescription() : "");
                m.put("amount", it.getAmount());
                m.put("default_quantity", it.getDefaultQuantity() != null ? it.getDefaultQuantity() : 1);
                items.add(m);
            }
        } else {
            // fallback: single item equal to total
            Map<String, Object> m = new HashMap<>();
            m.put("name", "Pagamento");
            m.put("description", "Pagamento");
            m.put("amount", req.getAmount());
            m.put("default_quantity", 1);
            items.add(m);
        }
        cartSettings.put("items", items);
        root.put("cart_settings", cartSettings);

        // split_settings (optional)
        if (req.getSplit() != null && !req.getSplit().isEmpty()) {
            Map<String, Object> splitSettings = new HashMap<>();
            List<Map<String, Object>> rules = new ArrayList<>();

            for (SplitInputDTO s : req.getSplit()) {
                Map<String, Object> rule = new HashMap<>();
                rule.put("amount", s.getAmount()); // percentual
                rule.put("type", "percentage");
                rule.put("recipient_id", s.getRecipientId());
                Map<String, Object> options = new HashMap<>();
                options.put("liable", s.getLiable() != null ? s.getLiable() : false);
                // by your rules: if liable true -> charge_processing_fee true; else false
                options.put("charge_processing_fee", s.getLiable() != null && s.getLiable());
                // you can decide whether to include charge_remainder_fee only on liable
                options.put("charge_remainder_fee", s.getLiable() != null && s.getLiable());
                rule.put("options", options);
                rules.add(rule);
            }
            splitSettings.put("rules", rules);
            root.put("split_settings", splitSettings);
        }

        return root;
    }
}
