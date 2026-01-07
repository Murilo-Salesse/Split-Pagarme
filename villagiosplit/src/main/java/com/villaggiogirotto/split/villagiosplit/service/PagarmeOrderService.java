package com.villaggiogirotto.split.villagiosplit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.villaggiogirotto.split.villagiosplit.controller.requests.CreateOrderRequest;
import com.villaggiogirotto.split.villagiosplit.dto.*;
import com.villaggiogirotto.split.villagiosplit.dto.*;
import com.villaggiogirotto.split.villagiosplit.config.FiliaisConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.*;

@Service
public class PagarmeOrderService {

    @Value("${pagarme.base-url}")
    private String baseUrl;

    private final FiliaisConfig filiaisConfig;

    public PagarmeOrderService(FiliaisConfig filiaisConfig) {
        this.filiaisConfig = filiaisConfig;
    }

    public Mono<JsonNode> createOrder(CreateOrderRequest req) {
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
                // flat validation is harder without request total here, maybe skip strict validation or calc it
            }
        }

        WebClient webClient = createWebClient(secretKey);
        Map<String, Object> payload = buildOrderPayload(req);

        return webClient.post()
                .uri("/orders")
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
        // Pagar.me exige que a secretKey seja codificada em Base64 com ":" no final
        String credentials = secretKey + ":";
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Basic " + encodedCredentials)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    private Map<String, Object> buildOrderPayload(CreateOrderRequest req) {
        Map<String, Object> root = new HashMap<>();

        // Code (opcional - identificador no seu sistema)
        if (req.getCode() != null && !req.getCode().isEmpty()) {
            root.put("code", req.getCode());
        }

        // Items (obrigatório)
        root.put("items", buildItems(req.getItems(), req.getAmount()));

        // Customer (obrigatório se customer_id não for informado)
        if (req.getCustomer() != null) {
            root.put("customer", buildCustomer(req.getCustomer()));
        } else if (req.getCustomerId() != null && !req.getCustomerId().isEmpty()) {
            root.put("customer_id", req.getCustomerId());
        } else {
            throw new IllegalArgumentException("Customer ou customer_id deve ser informado");
        }

        // Payments (obrigatório)
        root.put("payments", buildPayments(req));

        // Split (opcional)
        if (req.getSplit() != null && !req.getSplit().isEmpty()) {
            root.put("split", buildSplit(req.getSplit()));
        }

        // Closed (default: true)
        root.put("closed", req.getClosed() != null ? req.getClosed() : true);

        // Shipping (opcional)
        if (req.getShipping() != null) {
            root.put("shipping", buildShipping(req.getShipping()));
        }

        // Metadata (opcional)
        if (req.getMetadata() != null) {
            root.put("metadata", req.getMetadata());
        }

        return root;
    }

    private List<Map<String, Object>> buildItems(List<CartItemDTO> items, Integer totalAmount) {
        List<Map<String, Object>> itemsList = new ArrayList<>();

        if (items != null && !items.isEmpty()) {
            for (CartItemDTO item : items) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("amount", item.getAmount());
                itemMap.put("description", item.getDescription() != null ? item.getDescription() : item.getName());
                itemMap.put("quantity", item.getDefaultQuantity() != null ? item.getDefaultQuantity() : 1);
                itemMap.put("code", item.getCode() != null ? item.getCode() : UUID.randomUUID().toString());
                itemsList.add(itemMap);
            }
        } else if (totalAmount != null) {
            // Fallback: item único
            Map<String, Object> defaultItem = new HashMap<>();
            defaultItem.put("amount", totalAmount);
            defaultItem.put("description", "Pagamento");
            defaultItem.put("quantity", 1);
            defaultItem.put("code", "item-1");
            itemsList.add(defaultItem);
        } else {
            throw new IllegalArgumentException("Items ou amount deve ser informado");
        }

        return itemsList;
    }

    private Map<String, Object> buildCustomer(CustomerDTO customer) {
        Map<String, Object> customerMap = new HashMap<>();

        customerMap.put("name", customer.getName());

        if (customer.getEmail() != null) {
            customerMap.put("email", customer.getEmail());
        }

        if (customer.getDocument() != null) {
            customerMap.put("document", customer.getDocument());
            customerMap.put("type", customer.getType() != null ? customer.getType() : "individual");
            customerMap.put("document_type", customer.getDocumentType() != null ? customer.getDocumentType() : "CPF");
        }

        if (customer.getCode() != null) {
            customerMap.put("code", customer.getCode());
        }

        if (customer.getGender() != null) {
            customerMap.put("gender", customer.getGender());
        }

        if (customer.getBirthdate() != null) {
            customerMap.put("birthdate", customer.getBirthdate());
        }

        // Address
        if (customer.getAddress() != null) {
            customerMap.put("address", buildAddress(customer.getAddress()));
        }

        // Phones
        if (customer.getPhones() != null) {
            customerMap.put("phones", customer.getPhones());
        }

        if (customer.getMetadata() != null) {
            customerMap.put("metadata", customer.getMetadata());
        }

        return customerMap;
    }

    private Map<String, Object> buildAddress(AddressDTO address) {
        Map<String, Object> addressMap = new HashMap<>();

        if (address.getCountry() != null) {
            addressMap.put("country", address.getCountry());
        }
        if (address.getState() != null) {
            addressMap.put("state", address.getState());
        }
        if (address.getCity() != null) {
            addressMap.put("city", address.getCity());
        }
        if (address.getZipCode() != null) {
            addressMap.put("zip_code", address.getZipCode());
        }
        if (address.getLine1() != null) {
            addressMap.put("line_1", address.getLine1());
        }
        if (address.getLine2() != null) {
            addressMap.put("line_2", address.getLine2());
        }

        return addressMap;
    }

    private List<Map<String, Object>> buildPayments(CreateOrderRequest req) {
        List<Map<String, Object>> payments = new ArrayList<>();
        Map<String, Object> payment = new HashMap<>();

        payment.put("payment_method", req.getPaymentMethod());

        switch (req.getPaymentMethod().toLowerCase()) {
            case "credit_card":
                payment.put("credit_card", buildCreditCard(req.getCreditCard()));
                break;
            case "pix":
                payment.put("pix", buildPix(req.getPix()));
                break;
            case "boleto":
                payment.put("boleto", buildBoleto(req.getBoleto()));
                break;
            case "debit_card":
                payment.put("debit_card", buildDebitCard(req.getDebitCard()));
                break;
            default:
                throw new IllegalArgumentException("Método de pagamento inválido: " + req.getPaymentMethod());
        }

        // Split no nível do pagamento (se houver)
        if (req.getSplit() != null && !req.getSplit().isEmpty()) {
            payment.put("split", buildSplit(req.getSplit()));
        }

        payments.add(payment);
        return payments;
    }

    private Map<String, Object> buildCreditCard(CreditCardDTO creditCard) {
        if (creditCard == null) {
            throw new IllegalArgumentException("Dados do cartão de crédito são obrigatórios");
        }

        Map<String, Object> ccMap = new HashMap<>();

        ccMap.put("operation_type", creditCard.getOperationType() != null ? creditCard.getOperationType() : "auth_and_capture");

        // Installments
        if (creditCard.getInstallments() != null) {
            ccMap.put("installments", creditCard.getInstallments());
        }

        // Statement descriptor
        if (creditCard.getStatementDescriptor() != null) {
            ccMap.put("statement_descriptor", creditCard.getStatementDescriptor());
        }

        // Card - pode ser card_id, card_token, ou dados do cartão
        if (creditCard.getCardId() != null) {
            ccMap.put("card_id", creditCard.getCardId());
        } else if (creditCard.getCardToken() != null) {
            ccMap.put("card_token", creditCard.getCardToken());
        } else {
            // Dados completos do cartão
            Map<String, Object> card = new HashMap<>();
            card.put("number", creditCard.getNumber());
            card.put("holder_name", creditCard.getHolderName());
            card.put("exp_month", creditCard.getExpMonth());
            card.put("exp_year", creditCard.getExpYear());
            card.put("cvv", creditCard.getCvv());

            if (creditCard.getBillingAddress() != null) {
                card.put("billing_address", buildAddress(creditCard.getBillingAddress()));
            }

            ccMap.put("card", card);
        }

        return ccMap;
    }

    private Map<String, Object> buildPix(PixDTO pix) {
        Map<String, Object> pixMap = new HashMap<>();

        if (pix != null && pix.getExpiresIn() != null) {
            pixMap.put("expires_in", pix.getExpiresIn());
        } else {
            // Default: 24 horas
            pixMap.put("expires_in", 86400);
        }

        return pixMap;
    }

    private Map<String, Object> buildBoleto(BoletoDTO boleto) {
        Map<String, Object> boletoMap = new HashMap<>();

        if (boleto != null) {
            if (boleto.getInstructions() != null) {
                boletoMap.put("instructions", boleto.getInstructions());
            }
            if (boleto.getDueAt() != null) {
                boletoMap.put("due_at", boleto.getDueAt());
            }
        }

        return boletoMap;
    }

    private Map<String, Object> buildDebitCard(DebitCardDTO debitCard) {
        if (debitCard == null) {
            throw new IllegalArgumentException("Dados do cartão de débito são obrigatórios");
        }

        Map<String, Object> dcMap = new HashMap<>();

        // Statement descriptor
        if (debitCard.getStatementDescriptor() != null) {
            dcMap.put("statement_descriptor", debitCard.getStatementDescriptor());
        }

        // Card
        Map<String, Object> card = new HashMap<>();
        card.put("number", debitCard.getNumber());
        card.put("holder_name", debitCard.getHolderName());
        card.put("exp_month", debitCard.getExpMonth());
        card.put("exp_year", debitCard.getExpYear());
        card.put("cvv", debitCard.getCvv());

        if (debitCard.getBillingAddress() != null) {
            card.put("billing_address", buildAddress(debitCard.getBillingAddress()));
        }

        dcMap.put("card", card);

        return dcMap;
    }

    private List<Map<String, Object>> buildSplit(List<SplitInputDTO> splitList) {
        List<Map<String, Object>> splits = new ArrayList<>();

        for (SplitInputDTO split : splitList) {
            Map<String, Object> splitMap = new HashMap<>();

            splitMap.put("amount", split.getAmount());
            splitMap.put("type", split.getType() != null ? split.getType() : "percentage");
            splitMap.put("recipient_id", split.getRecipientId());

            // Options
            Map<String, Object> options = new HashMap<>();
            boolean isLiable = split.getLiable() != null && split.getLiable();
            options.put("liable", isLiable);
            options.put("charge_processing_fee", isLiable);
            options.put("charge_remainder_fee", isLiable);

            splitMap.put("options", options);
            splits.add(splitMap);
        }

        return splits;
    }

    private Map<String, Object> buildShipping(ShippingDTO shipping) {
        Map<String, Object> shippingMap = new HashMap<>();

        if (shipping.getAmount() != null) {
            shippingMap.put("amount", shipping.getAmount());
        }
        if (shipping.getDescription() != null) {
            shippingMap.put("description", shipping.getDescription());
        }
        if (shipping.getRecipientName() != null) {
            shippingMap.put("recipient_name", shipping.getRecipientName());
        }
        if (shipping.getRecipientPhone() != null) {
            shippingMap.put("recipient_phone", shipping.getRecipientPhone());
        }
        if (shipping.getAddress() != null) {
            shippingMap.put("address", buildAddress(shipping.getAddress()));
        }

        return shippingMap;
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