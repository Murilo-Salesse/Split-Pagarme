import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CheckoutComponent } from './pages/checkout/checkout.component';
import { CustomersComponent } from './pages/customers/customers.component';

const routes: Routes = [
  { path: '', component: CheckoutComponent },
  { path: 'checkout', component: CheckoutComponent },
  { path: 'customers', component: CustomersComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
