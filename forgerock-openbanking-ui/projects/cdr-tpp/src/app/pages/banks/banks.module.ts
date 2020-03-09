import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { BanksRoutingModule } from './banks-routing.module';
import { BanksComponent } from './banks.component';
import { RouterModule } from '@angular/router';
import { BanksModule } from '../../components/banks/banks.module';

@NgModule({
  declarations: [BanksComponent],
  imports: [
    CommonModule,
    RouterModule,
    BanksRoutingModule,
    BanksModule
  ]
})
export class BanksPageModule {}
