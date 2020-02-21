import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';
import { FlexLayoutModule } from '@angular/flex-layout';

import { HomeRoutingModule } from './home-routing.module';
import { HomeComponent } from './home.component';
@NgModule({
  declarations: [HomeComponent],
  imports: [CommonModule, MatButtonModule, FlexLayoutModule, RouterModule, HomeRoutingModule]
})
export class HomeModule {}
