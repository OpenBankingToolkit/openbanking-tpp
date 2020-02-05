import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FlexLayoutModule } from '@angular/flex-layout';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule, MatIconModule, MatBadgeModule } from '@angular/material';
import { RouterModule } from '@angular/router';

import { ShopToolbarComponent } from './shop-toolbar.component';
import { ShopToolbarContainer } from './shop-toolbar.container';

@NgModule({
  declarations: [ShopToolbarComponent, ShopToolbarContainer],
  exports: [ShopToolbarComponent, ShopToolbarContainer],
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatBadgeModule,
    FlexLayoutModule,
    RouterModule
  ]
})
export class ShopToolbarModule {}
