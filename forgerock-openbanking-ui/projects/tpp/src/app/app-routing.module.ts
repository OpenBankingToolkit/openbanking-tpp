import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ForgerockGDPRService, ForegerockGDPRConsentGuard } from '@forgerock/openbanking-ngx-common/gdpr';
import { ForgerockSimpleLayoutModule, SimpleLayoutComponent } from '@forgerock/openbanking-ngx-common/layouts/simple';

export const routes: Routes = [
  {
    path: ForgerockGDPRService.gdprDeniedPage,
    component: SimpleLayoutComponent,
    loadChildren: () => import('forgerock/src/ob-ui-libs-lazy/gdpr.module.ts').then(m => m.OBUILibsLazyGDPRPage)
  },
  {
    path: 'dev/info',
    component: SimpleLayoutComponent,
    pathMatch: 'full',
    loadChildren: () => import('forgerock/src/ob-ui-libs-lazy/dev-info.module.ts').then(m => m.OBUILibsLazyDevInfoPage)
  },
  {
    path: '',
    canActivate: [ForegerockGDPRConsentGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        loadChildren: () => import('tpp/src/app/pages/shop/shop.module').then(m => m.TppShopModule)
      },
      {
        path: 'success',
        loadChildren: () => import('tpp/src/app/pages/success/success.module').then(m => m.TppSuccessModule)
      }
    ]
  },
  {
    path: '**',
    pathMatch: 'full',
    redirectTo: ''
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes), ForgerockSimpleLayoutModule],
  exports: [RouterModule]
})
export class AppRoutingModule {}
