package com.villaggiogirotto.split.villagiosplit.controller;

import com.villaggiogirotto.split.villagiosplit.controller.requests.CreateCustomerRequest;
import com.villaggiogirotto.split.villagiosplit.controller.requests.ListCustomersRequest;
import com.villaggiogirotto.split.villagiosplit.service.PagarmeCustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final PagarmeCustomerService customerService;

    public CustomerController(PagarmeCustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Cria um cliente na API Pagar.me
     * POST /customers
     */
    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> createCustomer(@RequestBody CreateCustomerRequest req) {
        return customerService.createCustomer(req)
                .map(customerResponse -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("customer", customerResponse);
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
     * Lista clientes da API Pagar.me
     * GET /customers
     */
    @GetMapping

    public Mono<ResponseEntity<Map<String, Object>>> listCustomers(
            @RequestParam String filialId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String document,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        ListCustomersRequest req = new ListCustomersRequest();
        req.setFilialId(filialId);
        req.setName(name);
        req.setDocument(document);
        req.setEmail(email);
        req.setGender(gender);
        req.setCode(code);
        req.setPage(page);
        req.setSize(size);

        return customerService.listCustomers(req)
                .map(customersResponse -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", customersResponse);
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
     * Atualiza um cliente na API Pagar.me
     * PUT /customers/{customerId}
     */
    @PutMapping("/{customerId}")
    public Mono<ResponseEntity<Map<String, Object>>> updateCustomer(
            @PathVariable String customerId,
            @RequestBody CreateCustomerRequest req
    ) {
        return customerService.updateCustomer(customerId, req)
                .map(customerResponse -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("customer", customerResponse);
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
