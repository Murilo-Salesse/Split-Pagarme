import { Component, OnInit } from '@angular/core';
import {
  CustomerService,
  CreateCustomerRequest,
  CustomerData,
} from '../../services/customer.service';
import { FilialService, Filial } from '../../services/filial.service';

@Component({
  selector: 'app-customers',
  standalone: false,
  templateUrl: './customers.component.html',
  styleUrl: './customers.component.css',
})
export class CustomersComponent implements OnInit {
  // Tab control
  activeTab: 'create' | 'list' = 'create';

  // Loading states
  isLoadingCreate = false;
  isLoadingList = false;
  isLoadingFiliais = false;
  isLoadingSecretKey = false;

  // Filial selection
  filialSelecionada: string = '';
  filiais: { [key: string]: Filial } = {};
  currentSecretKey: string = '';

  // Customer list
  customers: CustomerData[] = [];
  totalCustomers = 0;
  currentPage = 1;
  pageSize = 10;

  // Create form
  newCustomer: CreateCustomerRequest = {
    secretKey: '',
    name: '',
    email: '',
    document: '',
    documentType: 'CPF',
    type: 'individual',
    code: '',
    gender: undefined,
    birthdate: '',
    phones: {
      mobile_phone: {
        country_code: '55',
        area_code: '',
        number: '',
      },
    },
  };

  // Messages
  successMessage = '';
  errorMessage = '';

  constructor(
    private customerService: CustomerService,
    private filialService: FilialService
  ) {}

  ngOnInit(): void {
    this.loadFiliais();
  }

  loadFiliais(): void {
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

  getFilialConfig(): { secretKey: string; publicKey: string; nome: string } | null {
    const filial = this.filiais[this.filialSelecionada];
    if (filial) {
      return {
        ...filial,
        secretKey: this.currentSecretKey,
      };
    }
    return null;
  }

  onFilialChange(): void {
    this.currentSecretKey = '';
    this.successMessage = '';
    this.errorMessage = '';

    if (this.filialSelecionada) {
      this.loadSecretKey();
    }
  }

  loadSecretKey(): void {
    if (!this.filialSelecionada) return;

    this.isLoadingSecretKey = true;
    this.filialService.getSecretKey(this.filialSelecionada).subscribe({
      next: (res) => {
        if (res.success && res.secretKey) {
          this.currentSecretKey = res.secretKey;
          if (this.activeTab === 'list') {
            this.loadCustomers();
          }
        } else {
          this.errorMessage = res.error || 'Erro ao obter secretKey';
        }
        this.isLoadingSecretKey = false;
      },
      error: (err) => {
        this.errorMessage = 'Erro ao carregar configuração da filial';
        this.isLoadingSecretKey = false;
      },
    });
  }

  onTabChange(tab: 'create' | 'list'): void {
    this.activeTab = tab;
    this.successMessage = '';
    this.errorMessage = '';

    if (tab === 'list' && this.filialSelecionada) {
      this.loadCustomers();
    }
  }

  loadCustomers(): void {
    if (!this.currentSecretKey) {
      return;
    }

    this.isLoadingList = true;
    this.errorMessage = '';

    this.customerService
      .listCustomers(this.currentSecretKey, {
        page: this.currentPage,
        size: this.pageSize,
      })
      .subscribe({
        next: (res) => {
          if (res.success && res.data) {
            this.customers = res.data.data || [];
            this.totalCustomers = res.data.paging?.total || this.customers.length;
          } else {
            this.errorMessage = res.error || 'Erro ao carregar clientes';
          }
          this.isLoadingList = false;
        },
        error: (err) => {
          this.errorMessage = 'Erro ao carregar clientes. Verifique a conexão.';
          this.isLoadingList = false;
        },
      });
  }

  createCustomer(): void {
    if (!this.currentSecretKey) {
      this.errorMessage = 'Aguarde a secretKey carregar ou selecione outra filial!';
      return;
    }

    if (!this.newCustomer.name) {
      this.errorMessage = 'Nome é obrigatório!';
      return;
    }

    this.isLoadingCreate = true;
    this.successMessage = '';
    this.errorMessage = '';

    const request: CreateCustomerRequest = {
      ...this.newCustomer,
      secretKey: this.currentSecretKey,
    };

    this.customerService.createCustomer(request).subscribe({
      next: (res) => {
        if (res.success && res.customer) {
          this.successMessage = `Cliente "${res.customer.name}" criado com sucesso! ID: ${res.customer.id}`;
          this.resetForm();
        } else {
          this.errorMessage = res.error || 'Erro ao criar cliente';
        }
        this.isLoadingCreate = false;
      },
      error: (err) => {
        this.errorMessage =
          err.error?.error || 'Erro ao criar cliente. Verifique os dados.';
        this.isLoadingCreate = false;
      },
    });
  }

  resetForm(): void {
    this.newCustomer = {
      secretKey: '',
      name: '',
      email: '',
      document: '',
      documentType: 'CPF',
      type: 'individual',
      code: '',
      gender: undefined,
      birthdate: '',
      phones: {
        mobile_phone: {
          country_code: '55',
          area_code: '',
          number: '',
        },
      },
    };
  }

  nextPage(): void {
    this.currentPage++;
    this.loadCustomers();
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadCustomers();
    }
  }

  formatDocument(doc: string | undefined): string {
    if (!doc) return '-';
    if (doc.length === 11) {
      return doc.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    }
    if (doc.length === 14) {
      return doc.replace(
        /(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/,
        '$1.$2.$3/$4-$5'
      );
    }
    return doc;
  }
}
