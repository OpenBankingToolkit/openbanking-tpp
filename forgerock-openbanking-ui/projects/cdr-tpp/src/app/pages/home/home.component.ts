import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { Store, select } from '@ngrx/store';

import { IState, IUser } from 'cdr-tpp/src/models';
import { selectUser } from 'cdr-tpp/src/store/reducers/user';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-home',
  template: `
    <div fxLayout="column" fxLayoutAlign="center center">
      <ng-container *ngIf="user$ | async as user; else notloggedin">
        <h1>Welcome {{ user.firstName }}</h1>
        <div fxLayout="row">
          <button mat-raised-button color="primary" routerLink="/accounts">Accounts</button>
        </div>
      </ng-container>
      <ng-template #notloggedin>
        <h1>Welcome</h1>
        <div fxLayout="row">
          <button mat-raised-button color="primary" routerLink="/login">Login</button>
          <button mat-raised-button color="primary" routerLink="/register">Register</button>
        </div>
      </ng-template>
    </div>
  `,
  styles: [
    `
      :host > div {
        height: 100%;
        text-align: center;
      }
      :host h1 {
        margin: 0 0 1em 0;
        font-size: 3em;
      }
      :host mat-card {
        text-align: center;
      }
      :host button {
        margin: 0 1em;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomeComponent implements OnInit {
  public user$: Observable<IUser | null> = this.store.pipe(select(selectUser));
  constructor(private store: Store<IState>) {}

  ngOnInit() {}
}
