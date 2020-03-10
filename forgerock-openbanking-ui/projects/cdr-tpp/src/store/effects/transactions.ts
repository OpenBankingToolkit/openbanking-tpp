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
  GetTransactionsRequestAction,
  GetTransactionsSuccessAction,
  GetTransactionsErrorAction
} from '../reducers/transactions';
import { CDRService } from 'cdr-tpp/src/app/services/cdr.service';

@Injectable()
export class TransactionsEffects {
  constructor(private actions$: Actions, private cdrService: CDRService, private message: ForgerockMessagesService) {}

  @Effect()
  requestAccounts$: Observable<Action> = this.actions$.pipe(
    ofType(types.TRANSACTIONS_REQUEST),
    mergeMap((action: GetTransactionsRequestAction) => {
      const { bankId, accountId } = action.payload;
      return this.cdrService.getTransactions(bankId, accountId).pipe(
        map(
          response =>
            new GetTransactionsSuccessAction({
              accountId,
              transactions: response.transactions
            })
        ),
        catchError((er: HttpErrorResponse) => {
          const error = _get(er, 'error.Message') || _get(er, 'error.message') || _get(er, 'message') || er;
          this.message.error(error);
          console.error(error);
          return of(new GetTransactionsErrorAction({ accountId, error }));
        })
      );
    })
  );
}
