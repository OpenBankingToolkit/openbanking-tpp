import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { HttpErrorResponse } from '@angular/common/http';
import { ForgerockMessagesService } from '@forgerock/openbanking-ngx-common/services/forgerock-messages';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Observable, of } from 'rxjs';
import { catchError, map, mergeMap, delay } from 'rxjs/operators';
import _get from 'lodash-es/get';

import {
  types,
  GetUserLogoutRequestAction,
  GetUserLogoutErrorAction,
  GetUserLogoutSuccessAction
} from '../reducers/user';
import { CookieService } from 'ngx-cookie';
import { Router } from '@angular/router';

@Injectable()
export class UserEffects {
  constructor(
    private actions$: Actions,
    private message: ForgerockMessagesService,
    private cookieService: CookieService,
    private router: Router
  ) {}

  @Effect()
  requestLogout$: Observable<Action> = this.actions$.pipe(
    ofType(types.USER_LOGOUT_REQUEST),
    mergeMap((action: GetUserLogoutRequestAction) => {
      try {
        this.cookieService.remove('bearer');
        console.log('333', this.router.url)
        this.router.navigate(['/login']);
        return of(new GetUserLogoutSuccessAction());
      } catch (error) {
        this.message.error('Something wrong happened');
        return of(new GetUserLogoutErrorAction());
      }
    })
  );
}
