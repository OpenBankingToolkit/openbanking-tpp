import { Component, OnInit, ChangeDetectionStrategy, Input } from '@angular/core';

import { ITransaction, ProductCategory } from 'cdr-tpp/src/models';

@Component({
  selector: 'app-transaction',
  template: `
    <mat-card>
      <!-- <mat-progress-bar *ngIf="loading" mode="indeterminate"></mat-progress-bar> -->

      <mat-card-content fxLayout="row" fxLayoutAlign="space-between center">
        <div fxFlex class="title-wrapper">
          <div class="title">{{ transaction.description }}</div>
          <div class="transaction-id">{{ transaction.executionDateTime | date }}</div>
        </div>
        <div class="balance">{{ transaction.amount | currency: transaction.currency }}</div>
        <div class="caret">
          <mat-icon class="up" *ngIf="!isAmoutPositive(transaction.amount)">arrow_drop_up</mat-icon>
          <mat-icon class="down" *ngIf="isAmoutPositive(transaction.amount)">arrow_drop_down</mat-icon>
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
      .transaction-id {
        font-size: 0.8em;
        color: #757575;
      }
      .caret > mat-icon {
        width: 30px;
        height: 40px;
        font-size: 40px;
      }
      .caret .up {
        color: green;
      }
      .caret .down {
        color: red;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionComponent implements OnInit {
  @Input() loading: boolean;
  @Input() transaction: ITransaction;

  constructor() {}

  ngOnInit() {}

  isAmoutPositive = amount => parseFloat(amount) > 0;
}
