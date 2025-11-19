import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class CheckoutService {
  private api = 'https://split-pagarme.onrender.com';

  constructor(private http: HttpClient) {}

  createOrder(data: any) {
    return this.http.post(this.api, data);
  }
}
