import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { CartContainer } from './cart.container';

const routes: Routes = [
  {
    path: '**',
    component: CartContainer
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CartRoutingModule {}
