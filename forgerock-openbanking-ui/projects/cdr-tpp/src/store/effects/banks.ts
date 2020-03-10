import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { HttpErrorResponse } from '@angular/common/http';
import { ForgerockMessagesService } from '@forgerock/openbanking-ngx-common/services/forgerock-messages';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Observable, of } from 'rxjs';
import { catchError, map, mergeMap } from 'rxjs/operators';
import _get from 'lodash-es/get';

import { types, GetBanksRequestAction, GetBanksSuccessAction, GetBanksErrorAction } from '../reducers/banks';
import { CDRService } from 'cdr-tpp/src/app/services/cdr.service';

@Injectable()
export class BanksEffects {
  constructor(private actions$: Actions, private cdrService: CDRService, private message: ForgerockMessagesService) {}

  @Effect()
  requestBanks$: Observable<Action> = this.actions$.pipe(
    ofType(types.BANKS_REQUEST),
    mergeMap((action: GetBanksRequestAction) => {
      return this.cdrService.getBanks().pipe(
        map(
          response =>
            new GetBanksSuccessAction({
              banks: response.banks
            })
        ),
        catchError((er: HttpErrorResponse) => {
          const error = _get(er, 'error.Message') || _get(er, 'error.message') || _get(er, 'message') || er;
          this.message.error(error);
          console.error(error);
          return of(new GetBanksErrorAction());
        })
      );
    })
  );
}
