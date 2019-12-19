import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';

import { catchError, finalize, tap } from 'rxjs/operators';
import { of } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import _get from 'lodash-es/get';

import { ForgerockMessagesService } from 'ob-ui-libs/services/forgerock-messages';
import { TppNodeService } from '../../services/node.service';

@Component({
  selector: 'app-shop',
  template: `
    <div fxLayout="column" fxLayoutAlign="center center">
      <mat-spinner *ngIf="isRunning"></mat-spinner>
      <button *ngIf="!isRunning" mat-raised-button color="accent" (click)="bankRedirection()">
        Simulate Domestic Payement consent
      </button>
    </div>
  `,
  styles: [
    `
      :host > div {
        height: 100%;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TppShopComponent implements OnInit {
  isRunning = false;
  constructor(
    private nodeService: TppNodeService,
    private messages: ForgerockMessagesService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {}

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
