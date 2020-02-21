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
import { AccountComponent } from './account.component';
import { AccountsComponent } from './accounts.component';
import { AccountsContainer } from './accounts.container';

@NgModule({
  declarations: [AccountsComponent, AccountComponent, AccountsContainer],
  exports: [AccountsComponent, AccountComponent, AccountsContainer],
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
    RouterModule
  ]
})
export class AccountsModule {}
