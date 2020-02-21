import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ForgerockSimpleLayoutModule, SimpleLayoutComponent } from '@forgerock/openbanking-ngx-common/layouts/simple';

export const routes: Routes = [
  {
    path: 'dev/info',
    component: SimpleLayoutComponent,
    pathMatch: 'full',
    loadChildren: () => import('forgerock/src/ob-ui-libs-lazy/dev-info.module.ts').then(m => m.OBUILibsLazyDevInfoPage)
  },
  {
    path: '',
    children: [
      {
        path: '',
        pathMatch: 'full',
        loadChildren: () => import('cdr-tpp/src/app/pages/home/home.module').then(m => m.HomeModule)
      },
      {
        path: 'accounts',
        pathMatch: 'full',
        loadChildren: () => import('cdr-tpp/src/app/pages/accounts/accounts.module').then(m => m.AccountsPageModule)
      },
      {
        path: 'accounts/:accountId',
        pathMatch: 'full',
        loadChildren: () =>
          import('cdr-tpp/src/app/pages/transactions/transactions.module').then(m => m.TransactionsPageModule)
      },
      {
        path: 'add-bank',
        pathMatch: 'full',
        loadChildren: () => import('cdr-tpp/src/app/pages/banks/banks.module').then(m => m.BanksPageModule)
      },
      {
        path: 'success',
        loadChildren: () => import('cdr-tpp/src/app/pages/success/success.module').then(m => m.TppSuccessModule)
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
