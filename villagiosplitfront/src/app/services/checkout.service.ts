// checkout.service.ts
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class CheckoutService {
  // Altere conforme necessário
  // private api = 'https://split-pagarme.onrender.com';
  private api = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  /**
   * ✅ MÉTODO ORIGINAL - Mantido para compatibilidade
   * Cria um Payment Link (método que você já usa)
   */
  createOrder(data: any, secretKey: string): Observable<any> {
    const payload = {
      ...data,
      secretKey: secretKey,
    };

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    // Endpoint original: POST /
    return this.http.post(this.api + '/', payload, { headers });
  }

  /**
   * 🆕 NOVO - Cria um Payment Link (mesmo que createOrder, mas com nome mais claro)
   * Use este se quiser deixar o código mais explícito
   */
  createPaymentLink(data: any, secretKey: string): Observable<any> {
    const payload = {
      ...data,
      secretKey: secretKey,
    };

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    return this.http.post(this.api + '/', payload, { headers });
  }

  /**
   * 🆕 NOVO - Cria um Order com PIX
   * Retorna QR Code para pagamento instantâneo
   */
  createPixOrder(data: any, secretKey: string): Observable<any> {
    const payload = {
      ...data,
      secretKey: secretKey,
      paymentMethod: 'pix',
    };

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    return this.http.post(this.api + '/orders/pix', payload, { headers });
  }

  /**
   * 🆕 NOVO - Cria um Order com Boleto
   * Retorna código de barras e PDF do boleto
   */
  createBoletoOrder(data: any, secretKey: string): Observable<any> {
    const payload = {
      ...data,
      secretKey: secretKey,
      paymentMethod: 'boleto',
    };

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    return this.http.post(this.api + '/orders/boleto', payload, { headers });
  }

  /**
   * 🆕 NOVO - Cria um Order com Cartão de Crédito
   * ⚠️ ATENÇÃO: Requer tokenização do cartão no frontend!
   */
  createCreditCardOrder(data: any, secretKey: string): Observable<any> {
    const payload = {
      ...data,
      secretKey: secretKey,
      paymentMethod: 'credit_card',
    };

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    return this.http.post(this.api + '/orders/credit-card', payload, {
      headers,
    });
  }

  /**
   * 🆕 NOVO - Cria um Order genérico
   * Permite especificar qualquer método de pagamento manualmente
   */
  createGenericOrder(data: any, secretKey: string): Observable<any> {
    const payload = {
      ...data,
      secretKey: secretKey,
    };

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    return this.http.post(this.api + '/orders', payload, { headers });
  }

  /**
   * 🆕 NOVO - Opcional: Buscar status de um pedido
   * Útil para verificar se o pagamento foi confirmado
   */
  getOrderStatus(orderId: string, secretKey: string): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: 'Basic ' + secretKey,
    });

    return this.http.get(this.api + '/orders/' + orderId, { headers });
  }
}
