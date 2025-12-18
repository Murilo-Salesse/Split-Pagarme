export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',

  // IMPORTANTE: As filiais agora são carregadas do backend via API
  // As secret keys estão protegidas em variáveis de ambiente no servidor
  // Use FilialService.listFiliais() para obter a lista de filiais
  // Use FilialService.getSecretKey(filialId) para obter a secretKey quando necessário

  // Placeholder para manter compatibilidade durante transição
  filiais: {
    brauna: {
      nome: 'Braúna',
      secretKey: '', // Será carregado do backend
      publicKey: 'pk_Qa82VbjIBBfMVePO',
      recebedores: [] as any[],
    },
    minasGerais: {
      nome: 'Minas Gerais',
      secretKey: '', // Será carregado do backend
      publicKey: 'pk_8yPoXxVf7UpRGjex',
      recebedores: [] as any[],
    },
  },
};

