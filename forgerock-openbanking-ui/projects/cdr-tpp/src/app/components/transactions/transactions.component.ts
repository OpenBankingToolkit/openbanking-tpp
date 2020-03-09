import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  Input,
  ElementRef,
  ViewChild,
  HostListener,
  AfterViewInit,
  ChangeDetectorRef
} from '@angular/core';

import { IUIAccount, ITransaction } from 'cdr-tpp/src/models';
import { ngForStagger } from '../animations';
import { ScrollDispatcher, CdkScrollable } from '@angular/cdk/overlay';

const appToolbarHeight = 56;

@Component({
  selector: 'app-transactions',
  template: `
    <div fxLayout="column" fxLayoutAlign="center stretch">
      <mat-toolbar>
        <button mat-icon-button routerLink="/accounts" aria-label="Add new bank">
          <mat-icon>arrow_back_ios</mat-icon>
        </button>
        <span fxFlex>Transactions</span></mat-toolbar
      >
      <mat-progress-bar *ngIf="isLoading" mode="indeterminate"></mat-progress-bar>
      <div class="content">
        <div *ngIf="isAccountElementSticky" [class.sticky]="isAccountElementSticky">
          <app-account [account]="account" [loading]="isLoading"></app-account>
        </div>
        <div #accountElement>
          <app-account
            *ngIf="account"
            [style.visibility]="isAccountElementSticky ? 'hidden' : 'visible'"
            [account]="account"
            [loading]="isLoading"
          ></app-account>
        </div>

        <div *ngIf="!isLoading && transactions" [@ngForStagger]="transactions.length">
          <app-transaction
            [transaction]="transaction"
            *ngFor="let transaction of transactions"
            [loading]="isBalancesLoading"
          ></app-transaction>
        </div>
        <forgerock-alert *ngIf="transactions !== null && !transactions.length" color="accent"
          >You do not have any transactions yet</forgerock-alert
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
      .sticky {
        position: sticky;
        z-index: 1;
        top: 0;
        background-color: #fafafa;
        width: 100%;
        max-width: 500px;
      }
      .sticky app-account {
        margin-bottom: 0em;
      }
      app-account,
      app-transaction {
        margin-bottom: 1em;
      }
    `
  ],
  animations: [ngForStagger],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionsComponent implements OnInit, AfterViewInit {
  accountElementPosition: number;
  isAccountElementSticky = false;

  @Input() isLoading: boolean;
  @Input() account: IUIAccount;
  @Input() error: string;
  @Input() transactions: ITransaction[];
  @ViewChild('accountElement') accountElement: ElementRef;

  constructor(private cdr: ChangeDetectorRef, private scrollDispatcher: ScrollDispatcher) {}

  ngOnInit() {}

  ngAfterViewInit() {
    const scrollableParents = this.scrollDispatcher.getAncestorScrollContainers(this.accountElement);
    const possibleScrollableParent = scrollableParents.filter(
      scrollable => scrollable.getElementRef().nativeElement.tagName === 'APP-TRANSACTIONS'
    );

    if (possibleScrollableParent.length === 1) {
      possibleScrollableParent[0].elementScrolled().subscribe((e: Event) => {
        const parentElement = <HTMLElement>e.srcElement;
        const {
          top: accountElementTop,
          bottom: accountElementBottom
        } = this.accountElement.nativeElement.getBoundingClientRect();
        const newValue =
          (this.isAccountElementSticky ? accountElementBottom : accountElementTop) - parentElement.scrollTop <= 0;

        if (this.isAccountElementSticky !== newValue) {
          this.isAccountElementSticky = newValue;
          this.cdr.detectChanges();
        }
      });
    }
    this.accountElementPosition = this.accountElement.nativeElement.offsetTop;
  }

  addAccount() {}
}
