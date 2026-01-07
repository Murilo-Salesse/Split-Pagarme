package com.villaggiogirotto.split.villagiosplit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.villaggiogirotto.split.villagiosplit.controller.requests.CreatePaymentRequest;
import com.villaggiogirotto.split.villagiosplit.dto.CartItemDTO;

import com.villaggiogirotto.split.villagiosplit.dto.SplitInputDTO;
import com.villaggiogirotto.split.villagiosplit.config.FiliaisConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Serviço para criar Payment Links via API Pagar.me
 * 
 * Endpoint: POST https://api.pagar.me/core/v5/paymentlinks
 * 
 * Payment Links permitem criar links de checkout onde o cliente
 * preenche seus próprios dados. Diferente da API de Orders,
 * não requer dados do cliente previamente.
 * 
 * IMPORTANTE: Split de pagamento só funciona para credit_card!
 */
@Service
public class PagarmePaymentLinkService {

    @Value("${pagarme.base-url}")
    private String baseUrl;

    private final FiliaisConfig filiaisConfig;

    public PagarmePaymentLinkService(FiliaisConfig filiaisConfig) {
        this.filiaisConfig = filiaisConfig;
    }

    /**
     * Cria um Payment Link com configuração de split
     *
     * @param req Requisição com dados do link
     * @return Resposta da API Pagar.me contendo checkout_url
     */
    public Mono<JsonNode> createPaymentLink(CreatePaymentRequest req) {
        if (req.getFilialId() == null || req.getFilialId().isEmpty()) {
            return Mono.error(new IllegalArgumentException("ID da filial é obrigatório"));
        }

        String secretKey = getSecretKeyByFilialId(req.getFilialId());
        if (secretKey == null) {
            return Mono.error(new IllegalArgumentException("Filial não encontrada ou sem chave configurada: " + req.getFilialId()));
        }

        // Validação do split
        if (req.getSplit() != null && !req.getSplit().isEmpty()) {
            // Verificar se todos os splits têm o mesmo tipo
            String splitType = req.getSplit().get(0).getType();
            if (splitType == null) splitType = "percentage";
            
            int totalAmount = req.getSplit().stream()
                    .mapToInt(SplitInputDTO::getAmount)
                    .sum();

            if ("percentage".equals(splitType)) {
                if (totalAmount != 100) {
                    return Mono.error(new IllegalArgumentException(
                            "A soma dos percentuais do split deve ser 100%. Atual: " + totalAmount + "%"
                    ));
                }
            } else {
                // flat: soma deve ser igual ao valor total
                int expectedTotal = calculateTotalAmount(req);
                if (totalAmount != expectedTotal) {
                    return Mono.error(new IllegalArgumentException(
                            "A soma dos valores do split (" + totalAmount + ") deve ser igual ao valor total (" + expectedTotal + ")"
                    ));
                }
            }
        }



        WebClient webClient = createWebClient(secretKey);
        Map<String, Object> payload = buildPaymentLinkPayload(req);

        return webClient.post()
                .uri("/paymentlinks")
                .bodyValue(payload)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException(
                                        "Erro na API Pagar.me [" + response.statusCode() + "]: " + errorBody
                                )))
                )
                .bodyToMono(JsonNode.class);
    }

    private WebClient createWebClient(String secretKey) {
        String credentials = secretKey + ":";
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Basic " + encodedCredentials)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * Constrói o payload seguindo o formato recomendado pelo suporte Pagar.me
     */
    private Map<String, Object> buildPaymentLinkPayload(CreatePaymentRequest req) {
        Map<String, Object> root = new HashMap<>();

        // Configurações básicas
        root.put("is_building", false);
        root.put("type", "order");

        // Payment Settings
        root.put("payment_settings", buildPaymentSettings(req));

        // Cart Settings (items)
        root.put("cart_settings", buildCartSettings(req));

        // Split Settings (regras de divisão)
        if (req.getSplit() != null && !req.getSplit().isEmpty()) {
            root.put("split_settings", buildSplitSettings(req.getSplit()));
        }

        return root;
    }

    private Map<String, Object> buildPaymentSettings(CreatePaymentRequest req) {
        Map<String, Object> paymentSettings = new HashMap<>();

        // Métodos de pagamento aceitos
        paymentSettings.put("accepted_payment_methods", Arrays.asList("credit_card", "pix"));

        // Statement descriptor (descrição na fatura)
        String descriptor = req.getStatementDescriptor();
        if (descriptor == null || descriptor.isEmpty()) {
            descriptor = "Pagamento";
        }
        // Limitar a 13 caracteres conforme documentação
        if (descriptor.length() > 13) {
            descriptor = descriptor.substring(0, 13);
        }
        paymentSettings.put("statement_descriptor", descriptor);

        // Credit Card Settings
        paymentSettings.put("credit_card_settings", buildCreditCardSettings(req));

        // PIX Settings
        Map<String, Object> pixSettings = new HashMap<>();
        pixSettings.put("expires_in", 3600); // 1 hora em segundos
        paymentSettings.put("pix_settings", pixSettings);

        return paymentSettings;
    }

    private Map<String, Object> buildCreditCardSettings(CreatePaymentRequest req) {
        Map<String, Object> ccSettings = new HashMap<>();

        ccSettings.put("operation_type", "auth_and_capture");

        // Configuração de parcelamento
        Map<String, Object> installmentsSetup = new HashMap<>();
        installmentsSetup.put("interest_type", "simple");
        
        int maxInstallments = req.getInstallments() != null ? req.getInstallments() : 12;
        installmentsSetup.put("max_installments", maxInstallments);
        installmentsSetup.put("free_installments", Math.min(3, maxInstallments)); // Até 3x sem juros
        
        // Amount é obrigatório para installments_setup
        int amount = calculateTotalAmount(req);
        installmentsSetup.put("amount", amount);
        installmentsSetup.put("interest_rate", 1);

        ccSettings.put("installments_setup", installmentsSetup);

        return ccSettings;
    }

    private Map<String, Object> buildCartSettings(CreatePaymentRequest req) {
        Map<String, Object> cartSettings = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();

        if (req.getItems() != null && !req.getItems().isEmpty()) {
            for (CartItemDTO item : req.getItems()) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("name", item.getName() != null ? item.getName() : "Item");
                itemMap.put("amount", item.getAmount());
                itemMap.put("default_quantity", item.getDefaultQuantity() != null ? item.getDefaultQuantity() : 1);
                items.add(itemMap);
            }
        } else if (req.getAmount() != null) {
            // Fallback: item único
            Map<String, Object> defaultItem = new HashMap<>();
            defaultItem.put("name", "Pagamento");
            defaultItem.put("amount", req.getAmount());
            defaultItem.put("default_quantity", 1);
            items.add(defaultItem);
        }

        cartSettings.put("items", items);
        return cartSettings;
    }

    private Map<String, Object> buildSplitSettings(List<SplitInputDTO> splitList) {
        Map<String, Object> splitSettings = new HashMap<>();
        List<Map<String, Object>> rules = new ArrayList<>();

        for (SplitInputDTO split : splitList) {
            Map<String, Object> rule = new HashMap<>();

            // Usa o type do split (percentage ou flat)
            String splitType = split.getType() != null ? split.getType() : "percentage";
            rule.put("type", splitType);
            rule.put("amount", split.getAmount());
            rule.put("recipient_id", split.getRecipientId());

            // Options
            Map<String, Object> options = new HashMap<>();
            boolean isLiable = split.getLiable() != null && split.getLiable();
            options.put("liable", isLiable);
            options.put("charge_processing_fee", isLiable);
            options.put("charge_remainder_fee", isLiable);

            rule.put("options", options);
            rules.add(rule);
        }

        splitSettings.put("rules", rules);
        return splitSettings;
    }

    private int calculateTotalAmount(CreatePaymentRequest req) {
        if (req.getAmount() != null) {
            return req.getAmount();
        }

        if (req.getItems() != null && !req.getItems().isEmpty()) {
            return req.getItems().stream()
                    .mapToInt(item -> {
                        int amount = item.getAmount() != null ? item.getAmount() : 0;
                        int qty = item.getDefaultQuantity() != null ? item.getDefaultQuantity() : 1;
                        return amount * qty;
                    })
                    .sum();
        }

        return 0;
    }
    private String getSecretKeyByFilialId(String filialId) {
        FiliaisConfig.FilialConfig filial = null;
        switch (filialId.toLowerCase()) {
            case "brauna":
                filial = filiaisConfig.getBrauna();
                break;
            case "minasgerais":
            case "minas-gerais":
                filial = filiaisConfig.getMinasGerais();
                break;
        }
        return (filial != null) ? filial.getSecretKey() : null;
    }
}
