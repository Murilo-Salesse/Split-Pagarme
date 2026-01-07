// customer.service.ts
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Customer } from '../models/payment';

export interface CreateCustomerRequest {
  filialId: string;
  name: string;
  email?: string;
  document?: string;
  documentType?: 'CPF' | 'CNPJ' | 'PASSPORT';
  type?: 'individual' | 'company';
  code?: string;
  gender?: 'male' | 'female';
  birthdate?: string;
  address?: {
    country?: string;
    state?: string;
    city?: string;
    zipCode?: string;
    line1?: string;
    line2?: string;
  };
  phones?: {
    home_phone?: {
      country_code: string;
      area_code: string;
      number: string;
    };
    mobile_phone?: {
      country_code: string;
      area_code: string;
      number: string;
    };
  };
}

export interface CustomerListResponse {
  success: boolean;
  data?: {
    data: CustomerData[];
    paging?: {
      total: number;
      previous?: string;
      next?: string;
    };
  };
  error?: string;
}

export interface CustomerData {
  id: string;
  name: string;
  email: string;
  document?: string;
  document_type?: string;
  type?: string;
  code?: string;
  created_at?: string;
}

export interface CreateCustomerResponse {
  success: boolean;
  customer?: CustomerData;
  error?: string;
}

@Injectable({
  providedIn: 'root',
})
export class CustomerService {
  private api = 'https://split-pagarme.onrender.com';

  constructor(private http: HttpClient) {}

  /**
   * Cria um cliente na API Pagar.me
   */
  createCustomer(
    data: CreateCustomerRequest
  ): Observable<CreateCustomerResponse> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    return this.http.post<CreateCustomerResponse>(
      `${this.api}/customers`,
      data,
      { headers }
    );
  }

  /**
   * Lista clientes da API Pagar.me
   */
  listCustomers(
    filialId: string,
    filters?: {
      name?: string;
      document?: string;
      email?: string;
      page?: number;
      size?: number;
    }
  ): Observable<CustomerListResponse> {
    let params = new HttpParams();

    if (filters?.name) params = params.set('name', filters.name);
    if (filters?.document) params = params.set('document', filters.document);
    if (filters?.email) params = params.set('email', filters.email);
    if (filters?.page) params = params.set('page', filters.page.toString());
    if (filters?.size) params = params.set('size', filters.size.toString());

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    // Adiciona filialId aos params
    params = params.set('filialId', filialId);

    return this.http.get<CustomerListResponse>(`${this.api}/customers`, {
      headers,
      params,
    });
  }
}
