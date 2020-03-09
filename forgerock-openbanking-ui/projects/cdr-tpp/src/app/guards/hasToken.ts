import { Injectable } from '@angular/core';
import { Router, CanActivate, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { Store } from '@ngrx/store';

import { IState } from 'cdr-tpp/src/models';
import { CDRService } from '../services/cdr.service';
import { of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { GetUserSuccessAction, GetUserErrorAction } from 'cdr-tpp/src/store/reducers/user';

@Injectable()
export class HasTokenGuard implements CanActivate {
  constructor(
    private router: Router,
    protected store: Store<IState>,
    protected cdrService: CDRService
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    return this.cdrService.getUser().pipe(
      map(response => {
        this.store.dispatch(new GetUserSuccessAction({ user: response.user }));
        return true;
      }),
      catchError(() => {
        if (state.url === '/') {
          return of(true);
        } else {
          this.store.dispatch(new GetUserErrorAction());
          this.router.navigate(['/login']);
        }
      })
    );
  }
}
