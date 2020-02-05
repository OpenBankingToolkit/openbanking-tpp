import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FlexLayoutModule } from '@angular/flex-layout';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterModule } from '@angular/router';

import { TppShopRoutingModule } from './shop-routing.module';
import { TppShopComponent } from './shop.component';
import { MatButtonModule, MatCardModule, MatIconModule } from '@angular/material';
import { ForgerockMessagesModule } from '@forgerock/openbanking-ngx-common/services/forgerock-messages';
import { TppShopContainer } from './shop.container';

@NgModule({
  declarations: [TppShopComponent, TppShopContainer],
  imports: [
    CommonModule,
    TppShopRoutingModule,
    FlexLayoutModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatSnackBarModule,
    RouterModule,
    ForgerockMessagesModule
  ]
})
export class TppShopModule {}
