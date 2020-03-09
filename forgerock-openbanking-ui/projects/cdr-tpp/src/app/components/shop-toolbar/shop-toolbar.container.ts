import { Component, Input } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { MatSidenav } from '@angular/material/sidenav';
import { IState, IUser } from 'cdr-tpp/src/models';
import { selectUser, GetUserLogoutRequestAction } from 'cdr-tpp/src/store/reducers/user';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-shop-toolbar-container',
  template: `
    <app-shop-toolbar [user]="user$ | async" (logout)="logout()" [sidenavRef]="sidenavRef"></app-shop-toolbar>
  `
})
export class ShopToolbarContainer {
  @Input() sidenavRef: MatSidenav;
  public user$: Observable<IUser | null> = this.store.pipe(select(selectUser));

  constructor(private store: Store<IState>) {}

  logout() {
    this.store.dispatch(new GetUserLogoutRequestAction());
  }
}
