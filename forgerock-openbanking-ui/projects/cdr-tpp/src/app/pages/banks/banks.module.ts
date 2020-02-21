import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { BanksRoutingModule } from './banks-routing.module';
import { BanksComponent } from './banks.component';
import { RouterModule } from '@angular/router';

@NgModule({
  declarations: [BanksComponent],
  imports: [
    CommonModule,
    MatToolbarModule,
    MatInputModule,
    MatIconModule,
    FormsModule,
    RouterModule,
    ReactiveFormsModule,
    BanksRoutingModule
  ]
})
export class BanksPageModule {}
