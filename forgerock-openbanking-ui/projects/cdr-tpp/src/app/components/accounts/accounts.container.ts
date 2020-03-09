import { Component, Input, OnInit } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { Observable } from 'rxjs';

import { IState, IUIAccount } from 'cdr-tpp/src/models';
import {
  selectLoadingAccounts,
  selectAccountsSelector,
  GetAccountsRequestAction,
  selectLoadingBalances,
  selectAccountsError
} from 'cdr-tpp/src/store/reducers/accounts';
import { first, filter } from 'rxjs/operators';

@Component({
  selector: 'app-accounts-container',
  template: `
    <app-accounts
      [isAccountsLoading]="isAccountsLoading$ | async"
      [isBalancesLoading]="isBalancesLoading$ | async"
      [accounts]="accounts$ | async"
      [error]="error$ | async"
    ></app-accounts>
  `
})
export class AccountsContainer implements OnInit {
  public isAccountsLoading$: Observable<boolean> = this.store.pipe(select(selectLoadingAccounts));
  public isBalancesLoading$: Observable<boolean> = this.store.pipe(select(selectLoadingBalances));
  public accounts$: Observable<IUIAccount[]> = this.store.pipe(select(selectAccountsSelector));
  public error$: Observable<string> = this.store.pipe(select(selectAccountsError));

  constructor(private store: Store<IState>) {}

  ngOnInit() {
    this.accounts$.pipe(first()).subscribe(data => !!!data && this.store.dispatch(new GetAccountsRequestAction()));
  }
}
