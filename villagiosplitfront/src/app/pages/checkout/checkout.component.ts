import { Component } from '@angular/core';
import {
  CartItem,
  CreatePaymentRequest,
  SplitRule,
} from '../../models/payment';
import { CheckoutService } from '../../services/checkout.service';

@Component({
  selector: 'app-checkout',
  standalone: false,
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css',
})
export class CheckoutComponent {
  amountInReais = 0;
  installments = 6;
  isLoading = false;

  items: CartItem[] = [
    {
      name: 'Pagamento da compra',
      description: 'Pagamento da compra',
      amount: 0,
      defaultQuantity: 1,
    },
  ];

  split: SplitRule[] = [];
  checkoutUrl: string | null = null;

  constructor(private checkoutService: CheckoutService) {}

  updateAmounts() {
    this.items[0].amount = Math.round(this.amountInReais * 100);
  }

  addSplit() {
    this.split.push({
      recipientId: '',
      amount: 0,
      liable: false,
    });
  }

  onRecipientChange(split: SplitRule) {
    split.liable = split.recipientId === 're_cmhzfjou7007t0l9t76n27ab3';
  }

  submit() {
    this.updateAmounts();
    this.isLoading = true;

    const body: CreatePaymentRequest = {
      amount: Math.round(this.amountInReais * 100),
      installments: this.installments,
      items: this.items,
      split: this.split,
    };

    this.checkoutService.createOrder(body).subscribe({
      next: (res) => {
        console.log(res);
        this.checkoutUrl = (res as any).checkout_url;
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
      },
    });
  }
}
