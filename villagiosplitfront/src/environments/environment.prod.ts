export const environment = {
  production: true,
  apiUrl: 'https://split-pagarme.onrender.com',

  filiais: {
    brauna: {
      nome: 'Braúna',
      secretKey: 'sk_df1ed3c001cd4e7e8d82f04e2a4de05a',
      publicKey: 'pk_Qa82VbjIBBfMVePO',
      recebedores: [
        {
          id: 're_cm1v0c5ou3d8z0l9tdsmogd9l',
          nome: 'Villaggio Girotto (Principal)',
          liable: true,
        },
        {
          id: 're_brauna_andreia',
          nome: 'Andreia',
          liable: false,
        },
        {
          id: 're_brauna_fabiana',
          nome: 'Fabiana Massagista',
          liable: false,
        },
      ],
    },
    minasGerais: {
      nome: 'Minas Gerais',
      secretKey: 'sk_6d2cf72a6e6649909893102dbf73d87d',
      publicKey: 'pk_8yPoXxVf7UpRGjex',
      recebedores: [
        {
          id: 'rp_9bV3QoSVOuv24Oj8',
          nome: 'Villaggio Girotto (Principal)',
          liable: true,
        },
        {
          id: 're_cmhxjk1ii0ts00l9twu3xb7cy',
          nome: 'Pai/Mãe',
          liable: false,
        },
        {
          id: 're_cmienxadq7hm90l9tna50nwho',
          nome: 'Bruna',
          liable: false,
        },
      ],
    },
  },
};
