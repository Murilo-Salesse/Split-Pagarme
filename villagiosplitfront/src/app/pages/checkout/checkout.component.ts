import { Component, OnInit } from '@angular/core';
import {
  CartItem,
  CreatePaymentLinkRequest,
  CreateOrderRequest,
  SplitRule,
  Customer,
  PaymentResponse,
} from '../../models/payment';
import { CheckoutService } from '../../services/checkout.service';
import { CustomerService, CustomerData } from '../../services/customer.service';
import { FilialService, Filial, Recebedor } from '../../services/filial.service';

@Component({
  selector: 'app-checkout',
  standalone: false,
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css',
})
export class CheckoutComponent implements OnInit {
  amountInReais = 0;
  amountDisplay = '';
  installments = 6;
  isLoading = false;

  filialSelecionada: string = '';
  filiais: { [key: string]: Filial } = {};
  recebedoresDisponiveis: Recebedor[] = [];
  currentSecretKey: string = '';
  isLoadingFiliais: boolean = false;
  isLoadingSecretKey: boolean = false;

  // Método de pagamento selecionado
  paymentMethod: 'payment_link' | 'pix' | 'boleto' | 'credit_card' =
    'payment_link';

  items: CartItem[] = [
    {
      name: 'Pagamento da compra',
      description: 'Pagamento da compra',
      amount: 0,
      defaultQuantity: 1,
    },
  ];

  split: SplitRule[] = [];
  splitType: 'percentage' | 'flat' = 'percentage';

  // Resultados
  checkoutUrl: string | null = null;
  pixQrCode: string | null = null;
  pixQrCodeUrl: string | null = null;
  boletoUrl: string | null = null;
  boletoBarcode: string | null = null;
  boletoPdf: string | null = null;
  transactionStatus: string | null = null;

  // Dados do cliente (necessário para Orders API)
  customerData: Customer = {
    name: '',
    email: '',
    document: '',
    documentType: 'CPF',
    type: 'individual',
  };

  // Clientes cadastrados
  customers: CustomerData[] = [];
  selectedCustomerId: string = '';
  useExistingCustomer: boolean = false;
  isLoadingCustomers: boolean = false;

  constructor(
    private checkoutService: CheckoutService,
    private customerService: CustomerService,
    private filialService: FilialService
  ) {}

  ngOnInit() {
    this.loadFiliais();
  }

  loadFiliais() {
    this.isLoadingFiliais = true;
    this.filialService.listFiliais().subscribe({
      next: (res) => {
        if (res.success && res.filiais) {
          this.filiais = res.filiais;
        }
        this.isLoadingFiliais = false;
      },
      error: (err) => {
        this.isLoadingFiliais = false;
      },
    });
  }

  onFilialChange() {
    this.split = [];
    this.clearResults();
    this.customers = [];
    this.selectedCustomerId = '';

    const filial = this.filiais[this.filialSelecionada];
    if (filial) {
      this.recebedoresDisponiveis = filial.recebedores || [];
      // Carregar clientes da filial selecionada (sem secretKey)
      this.loadCustomers();
    } else {
      this.recebedoresDisponiveis = [];
    }
  }

  loadCustomers() {
    if (!this.filialSelecionada) return;

    this.isLoadingCustomers = true;
    // Agora passa o ID da filial
    this.customerService.listCustomers(this.filialSelecionada, { size: 50 }).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.customers = res.data.data || [];
        }
        this.isLoadingCustomers = false;
      },
      error: (err) => {
        this.isLoadingCustomers = false;
      },
    });
  }

  getFilialConfig(): Filial | null {
    return this.filiais[this.filialSelecionada] || null;
  }

  updateAmounts() {
    const cleanValue = String(this.amountInReais)
      .replace(/\./g, '')
      .replace(',', '.');

    const valueInReais = parseFloat(cleanValue) || 0;
    this.items[0].amount = Math.round(valueInReais * 100);
  }

  onAmountInput(event: any) {
    let value = event.target.value;
    value = value.replace(/[^\d.,]/g, '');
    const numericValue = value.replace(',', '.');
    this.amountInReais = parseFloat(numericValue) || 0;
    this.updateAmounts();
  }

  addSplit() {
    this.split.push({
      recipientId: '',
      amount: 0,
      amountDisplay: '',
      type: this.splitType,
      liable: false,
    });
  }

  onSplitTypeChange() {
    // Reset all split amounts and update type when global type changes
    this.split.forEach(s => {
      s.amount = 0;
      s.amountDisplay = '';
      s.type = this.splitType;
    });
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value / 100);
  }

  onSplitAmountInput(split: SplitRule, event: any) {
    // Remove tudo que não é número
    let rawValue = event.target.value.replace(/\D/g, '');
    
    // Converte para número (já em centavos)
    const centavos = parseInt(rawValue, 10) || 0;
    split.amount = centavos;
    
    // Formata para exibição (ex: 486 -> "4,86")
    if (centavos === 0) {
      split.amountDisplay = '';
    } else {
      const reais = centavos / 100;
      split.amountDisplay = reais.toLocaleString('pt-BR', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      });
    }
    
    // Atualiza o valor no input
    event.target.value = split.amountDisplay;
  }

  onRecipientChange(split: SplitRule) {
    const recebedor = this.recebedoresDisponiveis.find(
      (r) => r.id === split.recipientId
    );
    if (recebedor) {
      split.liable = recebedor.liable;
    }
  }

  clearResults() {
    this.checkoutUrl = null;
    this.pixQrCode = null;
    this.pixQrCodeUrl = null;
    this.boletoUrl = null;
    this.boletoBarcode = null;
    this.boletoPdf = null;
    this.transactionStatus = null;
  }

  validateForm(): boolean {
    if (!this.filialSelecionada) {
      alert('Por favor, selecione uma filial!');
      return false;
    }

    if (this.split.length === 0) {
      alert('Por favor, adicione pelo menos um split!');
      return false;
    }

    // Valida soma do split
    const totalSplit = this.split.reduce((sum, s) => sum + s.amount, 0);
    
    if (this.splitType === 'percentage') {
      if (totalSplit !== 100) {
        alert(`A soma do split deve ser 100%. Atual: ${totalSplit}%`);
        return false;
      }
    } else {
      // flat type - soma deve ser igual ao valor total em centavos
      const totalAmountCentavos = Math.round(this.amountInReais * 100);
      if (totalSplit !== totalAmountCentavos) {
        const totalSplitReais = this.formatCurrency(totalSplit);
        const expectedReais = this.formatCurrency(totalAmountCentavos);
        alert(`A soma do split (${totalSplitReais}) deve ser igual ao valor total (${expectedReais})`);
        return false;
      }
    }

    // Valida dados do cliente para Orders API
    if (this.paymentMethod !== 'payment_link') {
      if (this.useExistingCustomer) {
        if (!this.selectedCustomerId) {
          alert('Por favor, selecione um cliente cadastrado!');
          return false;
        }
      } else {
        if (!this.customerData.name || !this.customerData.email) {
          alert('Por favor, preencha os dados do cliente!');
          return false;
        }
      }
    }

    return true;
  }

  submit() {
    if (!this.validateForm()) {
      return;
    }

    this.updateAmounts();
    this.isLoading = true;
    this.clearResults();

    // Escolhe o método correto baseado na seleção
    switch (this.paymentMethod) {
      case 'payment_link':
        this.submitPaymentLink();
        break;
      case 'pix':
        this.submitPixOrder();
        break;
      case 'boleto':
        this.submitBoletoOrder();
        break;
      case 'credit_card':
        this.submitCreditCardOrder();
        break;
    }
  }

  private submitPaymentLink() {
    const body: CreatePaymentLinkRequest = {
      filialId: this.filialSelecionada,
      amount: Math.round(this.amountInReais * 100),
      installments: this.installments,
      items: this.items,
      split: this.split,
    };

    this.checkoutService
      .createPaymentLink(body)
      .subscribe({
        next: (res: PaymentResponse) => {
          this.checkoutUrl = res.checkout_url || null;
          this.isLoading = false;
        },
        error: (err) => {
          alert('Erro ao criar Payment Link. Tente novamente.');
          this.isLoading = false;
        },
      });
  }

  private submitPixOrder() {
    const body: CreateOrderRequest = {
      filialId: this.filialSelecionada,
      code: `ORDER-${Date.now()}`,
      items: this.items,
      paymentMethod: 'pix',
      pix: {
        expiresIn: 3600, // 1 hora
      },
      split: this.split,
    };

    // Usar customerId se cliente existente estiver selecionado
    if (this.useExistingCustomer && this.selectedCustomerId) {
      body.customerId = this.selectedCustomerId;
    } else {
      body.customer = this.customerData;
    }

    this.checkoutService
      .createPixOrder(body)
      .subscribe({
        next: (res: PaymentResponse) => {
          this.pixQrCode = res.pix_qr_code || null;
          this.pixQrCodeUrl = res.pix_qr_code_url || null;
          this.transactionStatus = res.status || 'pending';
          this.isLoading = false;
        },
        error: (err) => {
          alert('Erro ao criar pedido PIX. Tente novamente.');
          this.isLoading = false;
        },
      });
  }

  private submitBoletoOrder() {
    const dueDate = new Date();
    dueDate.setDate(dueDate.getDate() + 3); // Vence em 3 dias

    const body: CreateOrderRequest = {
      filialId: this.filialSelecionada,
      code: `ORDER-${Date.now()}`,
      items: this.items,
      paymentMethod: 'boleto',
      boleto: {
        instructions: 'Não receber após o vencimento',
        dueAt: dueDate.toISOString().split('T')[0],
      },
      split: this.split,
    };

    // Usar customerId se cliente existente estiver selecionado
    if (this.useExistingCustomer && this.selectedCustomerId) {
      body.customerId = this.selectedCustomerId;
    } else {
      body.customer = this.customerData;
    }

    this.checkoutService
      .createBoletoOrder(body)
      .subscribe({
        next: (res: PaymentResponse) => {
          this.boletoUrl = res.boleto_url || null;
          this.boletoBarcode = res.boleto_barcode || null;
          this.boletoPdf = res.boleto_pdf || null;
          this.transactionStatus = res.status || 'pending';
          this.isLoading = false;
        },
        error: (err) => {
          alert('Erro ao criar boleto. Tente novamente.');
          this.isLoading = false;
        },
      });
  }

  private submitCreditCardOrder() {
    // ATENÇÃO: Em produção, você deve tokenizar o cartão primeiro!
    // Este exemplo usa card_id (cartão já salvo)

    const body: CreateOrderRequest = {
      filialId: this.filialSelecionada,
      code: `ORDER-${Date.now()}`,
      items: this.items,
      paymentMethod: 'credit_card',
      creditCard: {
        // Opção 1: Usar token (RECOMENDADO)
        // cardToken: 'token_gerado_no_frontend',

        // Opção 2: Usar cartão salvo
        cardId: 'card_xyz123',

        installments: this.installments,
        operationType: 'auth_and_capture',
      },
      split: this.split,
    };

    // Usar customerId se cliente existente estiver selecionado
    if (this.useExistingCustomer && this.selectedCustomerId) {
      body.customerId = this.selectedCustomerId;
    } else {
      body.customer = this.customerData;
    }

    this.checkoutService
      .createCreditCardOrder(body)
      .subscribe({
        next: (res: PaymentResponse) => {
          this.transactionStatus = res.status || 'processing';
          this.isLoading = false;

          if (res.status === 'paid') {
            alert('💳 Pagamento aprovado!');
          } else {
            alert('⏳ Pagamento em processamento...');
          }
        },
        error: (err) => {
          alert('Erro ao processar cartão. Tente novamente.');
          this.isLoading = false;
        },
      });
  }

  getFilialNome(): string {
    const config = this.getFilialConfig();
    return config ? config.nome : '';
  }

  resetForm() {
    this.filialSelecionada = '';
    this.amountInReais = 0;
    this.installments = 6;
    this.items = [
      {
        name: 'Pagamento da compra',
        description: 'Pagamento da compra',
        amount: 0,
        defaultQuantity: 1,
      },
    ];
    this.split = [];
    this.splitType = 'percentage';
    this.paymentMethod = 'payment_link';
    this.useExistingCustomer = false;
    this.selectedCustomerId = '';
    this.customerData = {
      name: '',
      email: '',
      document: '',
      documentType: 'CPF',
      type: 'individual',
    };
    this.recebedoresDisponiveis = [];

    this.clearResults();
  }

  // Helpers para exibir resultados
  hasPaymentLink(): boolean {
    return !!this.checkoutUrl;
  }

  hasPixResult(): boolean {
    return !!this.pixQrCode || !!this.pixQrCodeUrl;
  }

  hasBoletoResult(): boolean {
    return !!this.boletoUrl || !!this.boletoBarcode;
  }

  hasCardResult(): boolean {
    return !!this.transactionStatus && this.paymentMethod === 'credit_card';
  }
}
