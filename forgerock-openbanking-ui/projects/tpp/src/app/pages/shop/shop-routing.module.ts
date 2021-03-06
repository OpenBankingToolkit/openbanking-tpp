import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { TppShopContainer } from './shop.container';

const routes: Routes = [
  {
    path: '**',
    component: TppShopContainer
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TppShopRoutingModule {}
