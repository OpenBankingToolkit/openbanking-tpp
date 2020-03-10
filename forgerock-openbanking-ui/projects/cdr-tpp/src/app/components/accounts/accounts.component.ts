import { Component, OnInit, ChangeDetectionStrategy, Input } from '@angular/core';

import { IUIAccount } from 'cdr-tpp/src/models';
import { ngForStagger } from '../animations';

@Component({
  selector: 'app-accounts',
  template: `
    <div fxLayout="column" fxLayoutAlign="center stretch">
      <mat-toolbar>
        <span fxFlex>Accounts</span>
        <button mat-icon-button routerLink="/add-bank" aria-label="Add new bank">
          <mat-icon>add_circle</mat-icon>
        </button></mat-toolbar
      >
      <mat-progress-bar *ngIf="isAccountsLoading" mode="indeterminate"></mat-progress-bar>
      <div class="content">
        <div *ngIf="!isAccountsLoading && accounts" [@ngForStagger]="accounts.length">
          <app-account [account]="account" *ngFor="let account of accounts" [loading]="isBalancesLoading"></app-account>
        </div>
        <forgerock-alert *ngIf="accounts !== null && !accounts.length" color="accent"
          >You do not have any accounts yet</forgerock-alert
        >
        <forgerock-alert *ngIf="error" color="warn">{{ error }}</forgerock-alert>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
        max-width: 500px;
        margin: auto;
      }
      .content {
        margin: 1em 0;
      }
      app-account {
        margin-bottom: 1em;
      }
    `
  ],
  animations: [ngForStagger],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AccountsComponent implements OnInit {
  @Input() isAccountsLoading: boolean;
  @Input() isBalancesLoading: boolean;
  @Input() accounts: IUIAccount[];
  @Input() error: string;

  constructor() {}

  ngOnInit() {}

  addAccount() {}
}
