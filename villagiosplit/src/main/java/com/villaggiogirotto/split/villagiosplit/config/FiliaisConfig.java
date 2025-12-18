package com.villaggiogirotto.split.villagiosplit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "filiais")
public class FiliaisConfig {
    private FilialConfig brauna;
    private FilialConfig minasGerais;

    @Data
    public static class FilialConfig {
        private String nome;
        private String secretKey;
        private String publicKey;
        private List<RecebedorConfig> recebedores;
    }

    @Data
    public static class RecebedorConfig {
        private String id;
        private String nome;
        private boolean liable;
    }
}
