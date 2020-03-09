import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';
import { FlexLayoutModule } from '@angular/flex-layout';
import { LottieModule } from 'ngx-lottie';

import { ConsentRoutingModule } from './consent-routing.module';
import { ConsentComponent } from './consent.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@NgModule({
  declarations: [ConsentComponent],
  imports: [
    CommonModule,
    MatButtonModule,
    FlexLayoutModule,
    RouterModule,
    MatProgressSpinnerModule,
    ConsentRoutingModule,
    LottieModule
  ]
})
export class ConsentPageModule {}
