import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AccountsRoutingModule } from './accounts-routing.module';
import { AccountsComponent } from './accounts.component';
import { AccountsModule } from 'cdr-tpp/src/app/components/accounts/accounts.module';

@NgModule({
  declarations: [AccountsComponent],
  imports: [CommonModule, AccountsRoutingModule, AccountsModule]
})
export class AccountsPageModule {}
