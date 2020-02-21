import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FlexLayoutModule } from '@angular/flex-layout';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterModule } from '@angular/router';

import { ForgerockAlertModule } from '@forgerock/openbanking-ngx-common/components/forgerock-alert';
import { TransactionComponent } from './transaction.component';
import { TransactionsComponent } from './transactions.component';
import { TransactionsContainer } from './transactions.container';
import { AccountsModule } from '../accounts/accounts.module';

@NgModule({
  declarations: [TransactionsComponent, TransactionComponent, TransactionsContainer],
  exports: [TransactionsComponent, TransactionComponent, TransactionsContainer],
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatToolbarModule,
    ForgerockAlertModule,
    FlexLayoutModule,
    RouterModule,
    AccountsModule
  ]
})
export class TransactionsModule {}
