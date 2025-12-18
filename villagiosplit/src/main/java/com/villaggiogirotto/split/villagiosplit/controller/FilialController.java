package com.villaggiogirotto.split.villagiosplit.controller;

import com.villaggiogirotto.split.villagiosplit.config.FiliaisConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/filiais")
public class FilialController {

    private final FiliaisConfig filiaisConfig;

    public FilialController(FiliaisConfig filiaisConfig) {
        this.filiaisConfig = filiaisConfig;
    }

    /**
     * Lista todas as filiais disponíveis (sem expor secretKey)
     * GET /filiais
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listFiliais() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> filiais = new HashMap<>();

        if (filiaisConfig.getBrauna() != null) {
            filiais.put("brauna", buildFilialResponse(filiaisConfig.getBrauna()));
        }
        if (filiaisConfig.getMinasGerais() != null) {
            filiais.put("minasGerais", buildFilialResponse(filiaisConfig.getMinasGerais()));
        }

        response.put("success", true);
        response.put("filiais", filiais);
        return ResponseEntity.ok(response);
    }

    /**
     * Retorna a secretKey de uma filial específica
     * GET /filiais/{filialId}/secret
     * 
     * Nota: Em produção, este endpoint deveria ter autenticação/autorização adequada
     */
    @GetMapping("/{filialId}/secret")
    public ResponseEntity<Map<String, Object>> getFilialSecret(@PathVariable String filialId) {
        FiliaisConfig.FilialConfig filial = getFilialById(filialId);

        if (filial == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Filial não encontrada: " + filialId);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (filial.getSecretKey() == null || filial.getSecretKey().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Secret key não configurada para filial: " + filialId);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("secretKey", filial.getSecretKey());
        return ResponseEntity.ok(response);
    }

    private FiliaisConfig.FilialConfig getFilialById(String filialId) {
        switch (filialId.toLowerCase()) {
            case "brauna":
                return filiaisConfig.getBrauna();
            case "minasgerais":
            case "minas-gerais":
                return filiaisConfig.getMinasGerais();
            default:
                return null;
        }
    }

    private Map<String, Object> buildFilialResponse(FiliaisConfig.FilialConfig filial) {
        Map<String, Object> filialMap = new HashMap<>();
        filialMap.put("nome", filial.getNome());
        filialMap.put("publicKey", filial.getPublicKey());

        if (filial.getRecebedores() != null) {
            List<Map<String, Object>> recebedores = filial.getRecebedores().stream()
                    .map(r -> {
                        Map<String, Object> recebedorMap = new HashMap<>();
                        recebedorMap.put("id", r.getId());
                        recebedorMap.put("nome", r.getNome());
                        recebedorMap.put("liable", r.isLiable());
                        return recebedorMap;
                    })
                    .collect(Collectors.toList());
            filialMap.put("recebedores", recebedores);
        }

        return filialMap;
    }
}
