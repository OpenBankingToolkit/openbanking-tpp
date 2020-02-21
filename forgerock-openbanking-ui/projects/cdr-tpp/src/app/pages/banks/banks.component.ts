import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-banks',
  template: `
    <mat-toolbar>
      <button mat-icon-button routerLink="/accounts" aria-label="Add new bank">
        <mat-icon>arrow_back_ios</mat-icon>
      </button>
      <span fxFlex>Select a Bank</span>
    </mat-toolbar>
    <form [formGroup]="banksForm">
      <mat-form-field class="mat-form-field-appearance-fill">
        <mat-label>Search</mat-label>
        <mat-icon matPrefix>search</mat-icon>
        <input matInput formControlName="bankFilter" type="text" (keyup)="onSubmit(banksForm.value)" />
      </mat-form-field>
    </form>
  `,
  styles: [
    `
      mat-toolbar {
        margin-bottom: 1em;
      }
      mat-form-field {
        width: 100%;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BanksComponent implements OnInit {
  banksForm: FormGroup;
  constructor(private formBuilder: FormBuilder) {
    this.banksForm = this.formBuilder.group({
      bankFilter: ''
    });
  }

  ngOnInit() {}

  onSubmit(e: string) {
    console.log('eee', e);
  }
}
