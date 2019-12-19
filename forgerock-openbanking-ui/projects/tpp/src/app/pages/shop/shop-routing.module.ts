import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { TppShopComponent } from './shop.component';

const routes: Routes = [
  {
    path: '**',
    component: TppShopComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TppShopRoutingModule {}
