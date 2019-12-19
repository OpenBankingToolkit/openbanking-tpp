import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { TppSuccessComponent } from './success.component';

const routes: Routes = [
  {
    path: '**',
    component: TppSuccessComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TppSuccessRoutingModule {}
