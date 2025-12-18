package com.villaggiogirotto.split.villagiosplit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.villaggiogirotto.split.villagiosplit.controller.requests.CreateCustomerRequest;
import com.villaggiogirotto.split.villagiosplit.controller.requests.ListCustomersRequest;
import com.villaggiogirotto.split.villagiosplit.dto.AddressDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class PagarmeCustomerService {

    @Value("${pagarme.base-url}")
    private String baseUrl;

    /**
     * Cria um cliente na API Pagar.me
     * POST https://api.pagar.me/core/v5/customers
     */
    public Mono<JsonNode> createCustomer(CreateCustomerRequest req) {
        if (req.getSecretKey() == null || req.getSecretKey().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Secret Key é obrigatória"));
        }

        if (req.getName() == null || req.getName().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Nome do cliente é obrigatório"));
        }

        WebClient webClient = createWebClient(req.getSecretKey());
        Map<String, Object> payload = buildCustomerPayload(req);

        return webClient.post()
                .uri("/customers")
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

    /**
     * Lista clientes da API Pagar.me
     * GET https://api.pagar.me/core/v5/customers
     */
    public Mono<JsonNode> listCustomers(ListCustomersRequest req) {
        if (req.getSecretKey() == null || req.getSecretKey().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Secret Key é obrigatória"));
        }

        WebClient webClient = createWebClient(req.getSecretKey());
        
        // Construir query params
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/customers");
        
        if (req.getName() != null && !req.getName().isEmpty()) {
            uriBuilder.queryParam("name", req.getName());
        }
        if (req.getDocument() != null && !req.getDocument().isEmpty()) {
            uriBuilder.queryParam("document", req.getDocument());
        }
        if (req.getEmail() != null && !req.getEmail().isEmpty()) {
            uriBuilder.queryParam("email", req.getEmail());
        }
        if (req.getGender() != null && !req.getGender().isEmpty()) {
            uriBuilder.queryParam("gender", req.getGender());
        }
        if (req.getCode() != null && !req.getCode().isEmpty()) {
            uriBuilder.queryParam("code", req.getCode());
        }
        if (req.getPage() != null) {
            uriBuilder.queryParam("page", req.getPage());
        }
        if (req.getSize() != null) {
            uriBuilder.queryParam("size", req.getSize());
        }

        String uri = uriBuilder.build().toUriString();

        return webClient.get()
                .uri(uri)
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

    /**
     * Atualiza um cliente na API Pagar.me
     * PUT https://api.pagar.me/core/v5/customers/{customer_id}
     */
    public Mono<JsonNode> updateCustomer(String customerId, CreateCustomerRequest req) {
        if (req.getSecretKey() == null || req.getSecretKey().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Secret Key é obrigatória"));
        }

        if (customerId == null || customerId.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Customer ID é obrigatório"));
        }

        WebClient webClient = createWebClient(req.getSecretKey());
        Map<String, Object> payload = buildCustomerPayload(req);

        return webClient.put()
                .uri("/customers/" + customerId)
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

    private Map<String, Object> buildCustomerPayload(CreateCustomerRequest req) {
        Map<String, Object> payload = new HashMap<>();

        payload.put("name", req.getName());

        if (req.getEmail() != null && !req.getEmail().isEmpty()) {
            payload.put("email", req.getEmail());
        }
        if (req.getDocument() != null && !req.getDocument().isEmpty()) {
            // Remove pontos, traços e espaços do documento (CPF/CNPJ)
            String cleanDocument = req.getDocument().replaceAll("[^0-9]", "");
            payload.put("document", cleanDocument);
            payload.put("document_type", req.getDocumentType() != null ? req.getDocumentType() : "CPF");
            payload.put("type", req.getType() != null ? req.getType() : "individual");
        }
        if (req.getCode() != null && !req.getCode().isEmpty()) {
            payload.put("code", req.getCode());
        }
        if (req.getGender() != null && !req.getGender().isEmpty()) {
            payload.put("gender", req.getGender());
        }
        if (req.getBirthdate() != null && !req.getBirthdate().isEmpty()) {
            payload.put("birthdate", req.getBirthdate());
        }
        if (req.getAddress() != null) {
            payload.put("address", buildAddress(req.getAddress()));
        }
        if (req.getPhones() != null) {
            payload.put("phones", req.getPhones());
        }
        if (req.getMetadata() != null) {
            payload.put("metadata", req.getMetadata());
        }

        return payload;
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
}
