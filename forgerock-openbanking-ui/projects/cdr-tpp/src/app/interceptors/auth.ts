import { Injectable } from '@angular/core';
import { tap } from 'rxjs/operators';
import { HttpInterceptor, HttpHandler, HttpRequest, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { CookieService } from 'ngx-cookie';
import { Store } from '@ngrx/store';

import { IState } from 'cdr-tpp/src/models';
import { GetUserLogoutRequestAction } from 'cdr-tpp/src/store/reducers/user';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private cookieService: CookieService, private router: Router, private store: Store<IState>) {}

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    return next.handle(req).pipe(
      tap(
        () => {},
        (err: any) => {
          if (err instanceof HttpErrorResponse) {
            if (err.status !== 401) {
              return;
            }
            console.log('wwwww', this.router.url)
            this.store.dispatch(new GetUserLogoutRequestAction());
          }
        }
      )
    );
  }
}
