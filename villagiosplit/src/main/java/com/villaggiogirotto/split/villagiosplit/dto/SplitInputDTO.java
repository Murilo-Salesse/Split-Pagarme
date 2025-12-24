package com.villaggiogirotto.split.villagiosplit.dto;

import lombok.Data;

/**
 * DTO para configuração de Split de Pagamentos
 *
 * O split permite dividir o pagamento entre múltiplos recebedores
 * conforme regras percentuais definidas.
 *
 * Exemplo de uso:
 * - Marketplace: Dividir entre vendedor (90%) e plataforma (10%)
 * - Franquias: Dividir entre franqueado (80%) e franqueador (20%)
 * - Parcerias: Dividir entre parceiros conforme acordo
 */
@Data
public class SplitInputDTO {

    /**
     * ID do recebedor cadastrado na Pagar.me
     *
     * Formato: rp_XXXXX ou re_XXXXX
     * Exemplo: "rp_9bV3QoSVOuv24Oj8"
     *
     * Para obter recipient_id:
     * 1. Acesse o Dashboard da Pagar.me
     * 2. Vá em "Recebedores"
     * 3. Copie o ID do recebedor
     *
     * Ou crie via API: POST /recipients
     */
    private String recipientId;

    /**
     * Valor do split (percentual ou valor fixo em centavos)
     *
     * Para type='percentage': Valor entre 0 e 100
     * Para type='flat': Valor em centavos
     *
     * Exemplos:
     * - 90 (percentage) = 90% do valor total
     * - 50000 (flat) = R$ 500,00 em centavos
     */
    private Integer amount;

    /**
     * Tipo de divisão: percentage ou flat
     *
     * - percentage: Divide por porcentagem (soma deve ser 100%)
     * - flat: Divide por valor fixo em centavos (soma deve ser igual ao total)
     *
     * Default: percentage
     */
    private String type = "percentage";

    /**
     * Define se este recebedor é responsável (liable) pelo pagamento
     *
     * - true: Recebedor paga as taxas de processamento
     * - false: Taxas são divididas proporcionalmente
     *
     * Normalmente:
     * - O recebedor principal (maior percentual) deve ser liable = true
     * - Recebedores secundários devem ser liable = false
     *
     * Quando liable = true:
     * - charge_processing_fee = true (paga taxa de processamento)
     * - charge_remainder_fee = true (paga taxa de sobras)
     *
     * Quando liable = false:
     * - charge_processing_fee = false
     * - charge_remainder_fee = false
     *
     * Exemplo:
     * Venda de R$ 100,00 com taxa de 3,99%
     *
     * Split 90/10 com vendedor liable:
     * - Vendedor (90%, liable=true): Recebe R$ 86,01 (90% - taxa total)
     * - Plataforma (10%, liable=false): Recebe R$ 10,00 (10% sem descontos)
     *
     * Split 90/10 com plataforma liable:
     * - Vendedor (90%, liable=false): Recebe R$ 90,00 (90% sem descontos)
     * - Plataforma (10%, liable=true): Recebe R$ 6,01 (10% - taxa total)
     */
    private Boolean liable;

    /**
     * Construtor vazio (necessário para Lombok)
     */
    public SplitInputDTO() {
    }

    /**
     * Construtor completo para facilitar criação em testes
     */
    public SplitInputDTO(String recipientId, Integer amount, Boolean liable) {
        this.recipientId = recipientId;
        this.amount = amount;
        this.liable = liable;
        this.type = "percentage";
    }

    /**
     * Construtor completo com type
     */
    public SplitInputDTO(String recipientId, Integer amount, String type, Boolean liable) {
        this.recipientId = recipientId;
        this.amount = amount;
        this.type = type != null ? type : "percentage";
        this.liable = liable;
    }

    /**
     * Validação básica dos dados
     *
     * @throws IllegalArgumentException se os dados forem inválidos
     */
    public void validate() {
        if (recipientId == null || recipientId.trim().isEmpty()) {
            throw new IllegalArgumentException("recipientId é obrigatório");
        }

        if (!recipientId.startsWith("rp_") && !recipientId.startsWith("re_")) {
            throw new IllegalArgumentException(
                    "recipientId deve começar com 'rp_' ou 're_'. Recebido: " + recipientId
            );
        }

        if (amount == null) {
            throw new IllegalArgumentException("amount é obrigatório");
        }

        // Validação de type
        String splitType = this.type != null ? this.type : "percentage";
        if (!splitType.equals("percentage") && !splitType.equals("flat")) {
            throw new IllegalArgumentException(
                    "type deve ser 'percentage' ou 'flat'. Recebido: " + splitType
            );
        }

        // Validação de amount baseada no type
        if (splitType.equals("percentage")) {
            if (amount <= 0 || amount > 100) {
                throw new IllegalArgumentException(
                        "amount deve estar entre 1 e 100 para type 'percentage'. Recebido: " + amount
                );
            }
        } else {
            // flat: deve ser positivo
            if (amount <= 0) {
                throw new IllegalArgumentException(
                        "amount deve ser maior que 0 para type 'flat'. Recebido: " + amount
                );
            }
        }

        if (liable == null) {
            throw new IllegalArgumentException("liable é obrigatório");
        }
    }

    @Override
    public String toString() {
        String unit = "percentage".equals(type) ? "%" : " centavos";
        return String.format(
                "SplitInputDTO{recipientId='%s', amount=%d%s, type='%s', liable=%s}",
                recipientId, amount, unit, type, liable
        );
    }
}