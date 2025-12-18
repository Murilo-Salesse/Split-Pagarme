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
     * Percentual que este recebedor receberá
     *
     * Valor entre 0 e 100
     * A soma de todos os splits DEVE ser exatamente 100
     *
     * Exemplos:
     * - 90 = 90% do valor total
     * - 10 = 10% do valor total
     * - 33.33 = 33.33% do valor total (aceita decimais)
     */
    private Integer amount;

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

        if (amount <= 0 || amount > 100) {
            throw new IllegalArgumentException(
                    "amount deve estar entre 1 e 100. Recebido: " + amount
            );
        }

        if (liable == null) {
            throw new IllegalArgumentException("liable é obrigatório");
        }
    }

    @Override
    public String toString() {
        return String.format(
                "SplitInputDTO{recipientId='%s', amount=%d%%, liable=%s}",
                recipientId, amount, liable
        );
    }
}