// payment.model.ts

export interface CartItem {
  name: string;
  description: string;
  amount: number;
  defaultQuantity: number;
  code?: string; // Opcional: código do item no seu sistema
}

export interface SplitRule {
  recipientId: string;
  amount: number;
  amountDisplay?: string; // For flat mode - stores the text input
  type: 'percentage' | 'flat';
  liable: boolean;
}

export interface Address {
  country: string;
  state: string;
  city: string;
  zipCode: string;
  line1: string;
  line2?: string;
}

export interface Customer {
  name: string;
  email: string;
  document: string;
  documentType: 'CPF' | 'CNPJ' | 'PASSPORT';
  type: 'individual' | 'company';
  address?: Address;
  code?: string;
  gender?: 'male' | 'female';
  birthdate?: string;
  phones?: any;
  metadata?: Record<string, string>;
}

export interface CreditCard {
  cardToken?: string;
  cardId?: string;
  installments: number;
  operationType?: 'auth_only' | 'auth_and_capture';
  statementDescriptor?: string;
  billingAddress?: Address;
  // Para testes apenas - NUNCA use em produção!
  number?: string;
  holderName?: string;
  expMonth?: number;
  expYear?: number;
  cvv?: string;
}

export interface Pix {
  expiresIn?: number; // Em segundos (default: 86400 = 24h)
}

export interface Boleto {
  instructions?: string;
  dueAt?: string; // Formato: YYYY-MM-DD
}

export interface Shipping {
  amount: number;
  description: string;
  recipientName: string;
  recipientPhone: string;
  address: Address;
}

// Request para Payment Links (endpoint atual)
export interface CreatePaymentLinkRequest {
  filialId: string;
  amount: number;
  installments: number;
  items: CartItem[];
  split: SplitRule[];
}

// Request para Orders API (novo endpoint)
export interface CreateOrderRequest {
  filialId: string;
  code?: string;
  amount?: number; // Fallback se não enviar items

  // Customer (obrigatório)
  customerId?: string;
  customer?: Customer;

  // Items (obrigatório)
  items: CartItem[];

  // Payment (obrigatório)
  paymentMethod: 'credit_card' | 'pix' | 'boleto' | 'debit_card';
  creditCard?: CreditCard;
  pix?: Pix;
  boleto?: Boleto;

  // Split (opcional)
  split?: SplitRule[];

  // Optional
  closed?: boolean;
  shipping?: Shipping;
  metadata?: Record<string, string>;
}

// Response padrão
export interface PaymentResponse {
  success: boolean;
  checkout_url?: string; // Para Payment Links
  order?: any; // Para Orders API
  pix_qr_code?: string;
  pix_qr_code_url?: string;
  boleto_url?: string;
  boleto_barcode?: string;
  boleto_pdf?: string;
  transaction_id?: string;
  status?: string;
  error?: string;
}
