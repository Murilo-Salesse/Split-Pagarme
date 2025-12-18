// filial.service.ts
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Recebedor {
  id: string;
  nome: string;
  liable: boolean;
}

export interface Filial {
  nome: string;
  publicKey: string;
  recebedores: Recebedor[];
  secretKey?: string; // Only loaded when needed
}

export interface FiliaisResponse {
  success: boolean;
  filiais?: {
    [key: string]: Filial;
  };
  error?: string;
}

export interface SecretKeyResponse {
  success: boolean;
  secretKey?: string;
  error?: string;
}

@Injectable({
  providedIn: 'root',
})
export class FilialService {
  private api = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /**
   * Lista todas as filiais disponíveis (sem secretKey)
   */
  listFiliais(): Observable<FiliaisResponse> {
    return this.http.get<FiliaisResponse>(`${this.api}/filiais`);
  }

  /**
   * Obtém a secretKey de uma filial específica
   */
  getSecretKey(filialId: string): Observable<SecretKeyResponse> {
    return this.http.get<SecretKeyResponse>(`${this.api}/filiais/${filialId}/secret`);
  }
}
