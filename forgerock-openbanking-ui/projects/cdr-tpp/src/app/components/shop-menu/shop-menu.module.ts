import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

import { ShopMenuComponent } from './shop-menu.component';

@NgModule({
  declarations: [ShopMenuComponent],
  exports: [ShopMenuComponent],
  imports: [CommonModule, RouterModule, MatButtonModule, MatIconModule]
})
export class ShopMenuModule {}
