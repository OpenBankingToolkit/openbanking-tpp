import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material';

import { ShopMenuComponent } from './shop-menu.component';

@NgModule({
  declarations: [ShopMenuComponent],
  exports: [ShopMenuComponent],
  imports: [CommonModule, RouterModule, MatButtonModule]
})
export class ShopMenuModule {}
