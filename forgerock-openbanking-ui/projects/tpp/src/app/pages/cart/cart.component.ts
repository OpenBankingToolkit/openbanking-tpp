import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Input,
  EventEmitter,
  Output
} from '@angular/core';

import { catchError, finalize, tap } from 'rxjs/operators';
import { of } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import _get from 'lodash-es/get';

import { ForgerockMessagesService } from '@forgerock/openbanking-ngx-common/services/forgerock-messages';
import { TppNodeService } from '../../services/node.service';
import { ShopItem } from 'tpp/src/store/models';

@Component({
  selector: 'app-cart',
  template: `
    <div fxLayout="column" fxLayoutAlign="center center">
      <ng-template #elseBlock>
        <forgerock-alert color="accent"
          >No items selected yet. <button mat-raised-button routerLink="/shop">Go shopping!</button></forgerock-alert
        ></ng-template
      >
      <div *ngIf="items.length; else elseBlock">
        <table mat-table [dataSource]="items" class="mat-elevation-z8">
          <ng-container matColumnDef="img">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let element">
              <img loading="lazy" mat-card-image [src]="element.img" alt="Dress image" />
            </td>
            <td mat-footer-cell *matFooterCellDef>Total</td>
          </ng-container>

          <ng-container matColumnDef="title">
            <th mat-header-cell *matHeaderCellDef>Name</th>
            <td mat-cell *matCellDef="let element">{{ element.title }}</td>
            <td mat-footer-cell *matFooterCellDef></td>
          </ng-container>

          <ng-container matColumnDef="price">
            <th mat-header-cell *matHeaderCellDef>Price</th>
            <td mat-cell *matCellDef="let element">{{ element.price | currency: 'GBP' }}</td>
            <td mat-footer-cell *matFooterCellDef>
              <b>{{ getTotalCost() | currency: 'GBP' }}</b>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
          <tr mat-footer-row *matFooterRowDef="displayedColumns"></tr>
        </table>

        <div class="payment" fxLayout="row" fxLayoutAlign="center center">
          <mat-progress-bar mode="indeterminate" *ngIf="isRunning"></mat-progress-bar>
          <button
            *ngIf="!isRunning"
            fxLayout="column"
            fxLayoutAlign="center center"
            mat-raised-button
            color="primary"
            (click)="bankRedirection()"
          >
            <forgerock-customer-icon></forgerock-customer-icon> Pay with OpenBanking!
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      table {
        width: 100%;
        margin-bottom: 2em;
      }

      img {
        width: 150px;
        margin: 1em;
      }

      .payment button {
        padding: 1em;
      }

      .payment {
        text-align: center;
      }

      ::ng-deep forgerock-customer-icon {
        margin: auto;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CartComponent implements OnInit {
  @Input() items: ShopItem[];
  @Output() select = new EventEmitter<string>();
  displayedColumns: string[] = ['img', 'title', 'price'];
  isRunning = false;
  constructor(
    private nodeService: TppNodeService,
    private messages: ForgerockMessagesService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {}

  getTotalCost() {
    return this.items.map(t => t.price).reduce((acc, value) => acc + value, 0);
  }

  bankRedirection() {
    console.log('this.isRunning', this.isRunning);
    this.isRunning = true;
    this.nodeService
      .getRestDomesticPayementRedirection()
      .pipe(
        tap(({ redirection }) => {
          window.location.href = redirection;
        }),
        catchError((er: HttpErrorResponse | Error) => {
          console.log('catchError', er);
          console.log('this.isRunning', this.isRunning);
          const error = _get(er, 'error.error') || er;
          this.messages.error(error);
          return of(er);
        }),
        finalize(() => {
          console.log('finalize');
          console.log('this.isRunning', this.isRunning);
          this.isRunning = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe();
    console.log('this.isRunning', this.isRunning);
  }

  amRedirection() {
    console.log('this.isRunning', this.isRunning);
    this.isRunning = true;
    this.nodeService
      .getDomesticPayementRedirection()
      .pipe(
        tap(({ redirection }) => {
          window.location.href = redirection;
        }),
        catchError((er: HttpErrorResponse | Error) => {
          console.log('catchError', er);
          console.log('this.isRunning', this.isRunning);
          const error = _get(er, 'error.error') || er;
          this.messages.error(error);
          return of(er);
        }),
        finalize(() => {
          console.log('finalize');
          console.log('this.isRunning', this.isRunning);
          this.isRunning = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe();
    console.log('this.isRunning', this.isRunning);
  }
}
