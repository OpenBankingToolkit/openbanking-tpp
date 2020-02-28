import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FlexLayoutModule } from '@angular/flex-layout';
import { MatCardModule } from '@angular/material/card';

import { ShopSignComponent } from './shop-sign.component';

@NgModule({
  declarations: [ShopSignComponent],
  exports: [ShopSignComponent],
  imports: [CommonModule, FlexLayoutModule, MatCardModule, RouterModule]
})
export class ShopSignModule {}
