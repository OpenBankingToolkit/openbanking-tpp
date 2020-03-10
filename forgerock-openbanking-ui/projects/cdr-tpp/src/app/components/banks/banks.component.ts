import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  Input,
  SimpleChanges,
  OnChanges,
  ChangeDetectorRef,
  EventEmitter,
  Output
} from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';

import { IBank } from 'cdr-tpp/src/models';
import { ngForStagger } from '../animations';

@Component({
  selector: 'app-banks',
  template: `
    <div fxLayout="column" fxLayoutAlign="center stretch">
      <mat-toolbar>
        <button mat-icon-button routerLink="/accounts" aria-label="Back to accounts">
          <mat-icon>arrow_back_ios</mat-icon>
        </button>
        <span fxFlex>Select a Bank</span>
      </mat-toolbar>
      <form [formGroup]="banksForm">
        <mat-form-field class="mat-form-field-appearance-fill">
          <mat-label>Search</mat-label>
          <mat-icon matPrefix>search</mat-icon>
          <input matInput formControlName="bankFilter" type="text" (keyup)="onFilter(banksForm.value.bankFilter)" />
        </mat-form-field>
      </form>
      <mat-progress-bar *ngIf="isLoading" mode="indeterminate"></mat-progress-bar>
      <div class="content">
        <div *ngIf="!isLoading && filteredBanks" [@ngForStagger]="filteredBanks.length">
          <app-bank fxFlex="100" fxFlex.gt-xs="50" [bank]="bank" (select)="addBank($event)" *ngFor="let bank of filteredBanks"></app-bank>
        </div>
        <forgerock-alert *ngIf="banks !== null && !banks.length" color="accent"
          >You do not have any banks yet</forgerock-alert
        >
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
      app-bank {
        margin-bottom: 1em;
      }
      mat-toolbar {
        margin-bottom: 1em;
      }
      mat-form-field {
        width: 100%;
      }
    `
  ],
  animations: [ngForStagger],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BanksComponent implements OnInit, OnChanges {
  @Input() isLoading: boolean;
  @Input() banks: IBank[];
  @Output() select = new EventEmitter<IBank>();
  public filteredBanks: IBank[];
  public banksForm: FormGroup;

  constructor(private formBuilder: FormBuilder, private cdr: ChangeDetectorRef) {
    this.banksForm = this.formBuilder.group({
      bankFilter: ''
    });
  }

  ngOnInit() {}

  addBank(bank: IBank) {
    this.select.emit(bank);
  }

  ngOnChanges(changes: SimpleChanges): void {
    //Called before any other lifecycle hook. Use it to inject dependencies, but avoid any serious work here.
    //Add '${implements OnChanges}' to the class.
    if (changes.banks && changes.banks.currentValue) {
      this.onFilter();
    }
  }

  onFilter(filter: string = '') {
    this.filteredBanks = this.banks.filter(bank => bank.name.toLowerCase().indexOf(filter.toLowerCase()) >= 0);
    this.cdr.markForCheck();
  }
}
