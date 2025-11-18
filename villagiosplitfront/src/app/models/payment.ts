export interface CartItem {
  name: string;
  description: string;
  amount: number;
  defaultQuantity: number;
}

export interface SplitRule {
  recipientId: string;
  amount: number;
  liable: boolean;
}

export interface CreatePaymentRequest {
  amount: number;
  installments: number;
  items: CartItem[];
  split: SplitRule[];
}
