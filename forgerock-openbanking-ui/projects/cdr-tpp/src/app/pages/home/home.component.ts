import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-home',
  template: `
    <div fxLayout="column" fxLayoutAlign="center center">
      <h1>Welcome</h1>
      <div fxLayout="row">
        <button mat-raised-button color="primary" routerLink="/shop">Login</button>
        <button mat-raised-button color="primary" routerLink="/shop">Register</button>
      </div>
      <button mat-raised-button color="primary" routerLink="/accounts">Accounts</button>
      <button mat-raised-button color="primary" routerLink="/accounts/test">Transactions</button>
      <button mat-raised-button color="primary" routerLink="/banks">Bank selection</button>
    </div>
  `,
  styles: [
    `
      :host > div {
        height: 100%;
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
  constructor() {}

  ngOnInit() {}
}
