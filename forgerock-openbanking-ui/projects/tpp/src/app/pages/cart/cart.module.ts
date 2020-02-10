import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {  MatButtonModule, MatProgressBarModule } from '@angular/material';
import { MatTableModule } from '@angular/material/table';
import { ForgerockAlertModule } from '@forgerock/openbanking-ngx-common/components/forgerock-alert';
import { ForgerockCustomerIconModule } from '@forgerock/openbanking-ngx-common/components/forgerock-customer-icon';
import { RouterModule } from '@angular/router';

import { CartRoutingModule } from './cart-routing.module';
import { CartComponent } from './cart.component';
import { CartContainer } from './cart.container';

@NgModule({
  declarations: [CartComponent, CartContainer],
  imports: [
    CommonModule,
    MatProgressBarModule,
    MatTableModule,
    MatButtonModule,
    RouterModule,
    ForgerockAlertModule,
    ForgerockCustomerIconModule,
    CartRoutingModule
  ]
})
export class CartModule {}
