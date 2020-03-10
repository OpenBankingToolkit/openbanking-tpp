import { Component, OnInit, ChangeDetectionStrategy, Input } from '@angular/core';

import { ProductCategory, IUIAccount } from 'cdr-tpp/src/models';

@Component({
  selector: 'app-account',
  template: `
    <mat-card [routerLink]="'/accounts/' + account.bankId + '/' + account.accountId">
      <!-- <mat-progress-bar *ngIf="loading" mode="indeterminate"></mat-progress-bar> -->

      <mat-card-content fxLayout="row" fxLayoutAlign="space-between center">
        <div
          *ngIf="account.bank"
          class="bank-logo"
          [ngStyle]="{ backgroundImage: getImageSrc(account.bank.logo) }"
        ></div>
        <div fxFlex class="title-wrapper">
          <div class="title">{{ account.displayName }} - {{ getProductCategory(account.productCategory) }}</div>
          <div class="account-id">{{ account.accountId }}</div>
        </div>
        <div class="balance">
          <mat-progress-spinner
            *ngIf="!account.currentBalance"
            diameter="30"
            mode="indeterminate"
          ></mat-progress-spinner>
          <span *ngIf="account.currentBalance">{{ account.currentBalance | currency: account.currency }}</span>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [
    `
      :host,
      mat-card {
        display: block;
        width: 100%;
      }
      .bank-logo {
        height: 70px;
        width: 70px;
        margin: 0;
        background-size: contain;
        background-repeat: no-repeat;
        background-position: center;
        margin-right: 1em;
      }
      .title-wrapper {
        margin-right: 1em;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .title {
        font-size: 1.1em;
        font-weight: bold;
      }
      .account-id {
        font-size: 0.8em;
        color: #757575;
      }
      .balance {
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AccountComponent implements OnInit {
  @Input() loading: boolean;
  @Input() account: IUIAccount;

  constructor() {}

  ngOnInit() {}

  public getImageSrc(logo) {
    return `url("${logo}")`;
  }

  getProductCategory(category: ProductCategory) {
    switch (category) {
      case ProductCategory.CRED_AND_CHRG_CARDS:
        return 'Credit card';
      case ProductCategory.TRAVEL_CARDS:
        return 'Travel card';
      case ProductCategory.TERM_DEPOSITS:
        return 'Term deposit';
      case ProductCategory.TRANS_AND_SAVINGS_ACCOUNTS:
        return 'Savings account';
      case ProductCategory.BUSINESS_LOANS:
        return 'Business loan';
      case ProductCategory.LEASES:
        return 'Leases';
      case ProductCategory.MARGIN_LOANS:
        return 'Margin loans';
      case ProductCategory.OVERDRAFTS:
        return 'Overdrafts';
      case ProductCategory.PERS_LOANS:
        return 'Personal loan';
      case ProductCategory.REGULATED_TRUST_ACCOUNTS:
        return 'Regulated trust account';
      case ProductCategory.RESIDENTIAL_MORTGAGES:
        return 'Residential morgage';
      case ProductCategory.TRADE_FINANCE:
        return 'Trade finance';
      default:
        return category;
    }
  }
}
