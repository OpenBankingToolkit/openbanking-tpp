import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { HttpErrorResponse } from '@angular/common/http';
import { ForgerockMessagesService } from '@forgerock/openbanking-ngx-common/services/forgerock-messages';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Observable, of } from 'rxjs';
import { catchError, map, mergeMap } from 'rxjs/operators';
import _get from 'lodash-es/get';

import {
  types,
  GetAccountsRequestAction,
  GetAccountsSuccessAction,
  GetAccountsErrorAction
  // GetAccountsBalancesRequestAction,
  // GetAccountsBalancesSuccessAction,
  // GetAccountsBalancesErrorAction
} from '../reducers/accounts';
import { CDRService } from 'cdr-tpp/src/app/services/cdr.service';

@Injectable()
export class AccountsEffects {
  constructor(private actions$: Actions, private cdrService: CDRService, private message: ForgerockMessagesService) {}

  @Effect()
  requestAccounts$: Observable<Action> = this.actions$.pipe(
    ofType(types.ACCOUNTS_REQUEST),
    mergeMap((action: GetAccountsRequestAction) => {
      return this.cdrService.getAccounts().pipe(
        map(
          response =>
            new GetAccountsSuccessAction({
              banks: response.banks,
              accounts: response.accounts
            })
        ),
        catchError((er: HttpErrorResponse) => {
          const error = _get(er, 'error.Message') || _get(er, 'error.message') || _get(er, 'message') || er;
          this.message.error(error);
          console.error(error);
          return of(new GetAccountsErrorAction({ error }));
        })
      );
    })
  );

  // @Todo: re enable that once balance bulk will be implemented
  // @Effect()
  // afterAccountSuccess$: Observable<Action> = this.actions$.pipe(
  //   ofType(types.ACCOUNTS_SUCCESS),
  //   mergeMap((action: GetAccountsSuccessAction) => {
  //     const { accounts } = action.payload;
  //     const accountIds = accounts.map(account => account.accountId);

  //     return of(
  //       new GetAccountsBalancesRequestAction({
  //         accountIds
  //       })
  //     );
  //   })
  // );

  // @Effect()
  // requestBalances$: Observable<Action> = this.actions$.pipe(
  //   ofType(types.ACCOUNTS_BALANCES_REQUEST),
  //   mergeMap((action: GetAccountsBalancesRequestAction) => {
  //     const { accountIds } = action.payload;
  //     return this.cdrService.getBalances(accountIds).pipe(
  //       delay(1000), // remove when prod
  //       map(
  //         response =>
  //           new GetAccountsBalancesSuccessAction({
  //             page: 0, // @TODO: handling pagination
  //             balances: response.data.balances
  //           })
  //       ),
  //       catchError((er: HttpErrorResponse) => {
  //         const error = _get(er, 'error.Message') || _get(er, 'error.message') || _get(er, 'message') || er;
  //         this.message.error(error);
  //         console.error(error);
  //         return of(new GetAccountsBalancesErrorAction());
  //       })
  //     );
  //   })
  // );
}
