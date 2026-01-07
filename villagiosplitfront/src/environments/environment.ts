export const environment = {
  production: false,
  apiUrl: 'https://split-pagarme.onrender.com',

  // IMPORTANTE: As filiais agora são carregadas do backend via API
  // As secret keys estão protegidas em variáveis de ambiente no servidor
  // Use FilialService.listFiliais() para obter a lista de filiais
  // Use FilialService.getSecretKey(filialId) para obter a secretKey quando necessário

  // Placeholder para manter compatibilidade durante transição
  filiais: {
    brauna: {
      nome: 'Braúna',
      publicKey: 'pk_Qa82VbjIBBfMVePO',
      recebedores: [] as any[],
    },
    minasGerais: {
      nome: 'Minas Gerais',
      publicKey: 'pk_8yPoXxVf7UpRGjex',
      recebedores: [] as any[],
    },
  },
};

