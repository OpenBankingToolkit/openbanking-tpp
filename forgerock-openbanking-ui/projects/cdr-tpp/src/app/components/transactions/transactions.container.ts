import { Component, OnInit } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { Observable, combineLatest } from 'rxjs';
import { ActivatedRoute } from '@angular/router';

import { IState, IUIAccount, ITransaction } from 'cdr-tpp/src/models';
import {
  selectAccountSelector,
  GetAccountsRequestAction,
  selectLoadingAccounts,
  selectAccountsError
} from 'cdr-tpp/src/store/reducers/accounts';
import { first } from 'rxjs/operators';
import {
  selectTransactionsSelector,
  selectIsLoading,
  GetTransactionsRequestAction,
  selectError as selectTransactionsError
} from 'cdr-tpp/src/store/reducers/transactions';

@Component({
  selector: 'app-transactions-container',
  template: `
    <app-transactions
      [isLoading]="isLoading$ | async"
      [account]="account$ | async"
      [error]="error$ | async"
      [transactions]="transactions$ | async"
    ></app-transactions>
  `
})
export class TransactionsContainer implements OnInit {
  public account$: Observable<IUIAccount> = this.store.pipe(
    select(state => selectAccountSelector(state, this.activatedRoute.snapshot.params.accountId))
  );
  public transactions$: Observable<ITransaction[]> = this.store.pipe(
    select(state => selectTransactionsSelector(state, this.activatedRoute.snapshot.params.accountId))
  );
  public isAccountsLoading$: Observable<boolean> = this.store.pipe(select(selectLoadingAccounts));
  public isTransactionsLoading$: Observable<boolean> = this.store.pipe(
    select(state => selectIsLoading(state, this.activatedRoute.snapshot.params.accountId))
  );
  public isLoading$: Observable<boolean> = combineLatest(
    this.isAccountsLoading$,
    this.isTransactionsLoading$,
    (isAccountsLoading: boolean, isTransactionsLoading: boolean) => isAccountsLoading || isTransactionsLoading
  );
  public error$: Observable<string> = combineLatest(
    this.store.pipe(select(selectAccountsError)),
    this.store.pipe(select(selectTransactionsError)),
    (accountError: string, transactionError: string) => accountError || transactionError || ''
  );

  constructor(private activatedRoute: ActivatedRoute, private store: Store<IState>) {}

  ngOnInit() {
    this.account$.pipe(first()).subscribe(data => !!!data && this.store.dispatch(new GetAccountsRequestAction()));
    this.transactions$.pipe(first()).subscribe(
      data =>
        !!!data &&
        this.store.dispatch(
          new GetTransactionsRequestAction({
            bankId: this.activatedRoute.snapshot.params.bankId,
            accountId: this.activatedRoute.snapshot.params.accountId
          })
        )
    );
  }
}
