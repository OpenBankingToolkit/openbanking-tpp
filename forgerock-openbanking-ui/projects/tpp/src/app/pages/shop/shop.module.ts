import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FlexLayoutModule } from '@angular/flex-layout';

import { TppShopRoutingModule } from './shop-routing.module';
import { TppShopComponent } from './shop.component';
import { MatButtonModule, MatProgressSpinnerModule } from '@angular/material';
import { ForgerockMessagesModule } from '@forgerock/openbanking-ngx-common/services/forgerock-messages';

@NgModule({
  declarations: [TppShopComponent],
  imports: [
    CommonModule,
    TppShopRoutingModule,
    FlexLayoutModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    ForgerockMessagesModule
  ]
})
export class TppShopModule {}
