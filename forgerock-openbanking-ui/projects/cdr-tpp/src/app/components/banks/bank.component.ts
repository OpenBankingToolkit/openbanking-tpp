import { Component, OnInit, ChangeDetectionStrategy, Input, Output, EventEmitter } from '@angular/core';

import { IBank } from 'cdr-tpp/src/models';
import { ForgerockConfigService } from '@forgerock/openbanking-ngx-common/services/forgerock-config';
import { CookieService } from 'ngx-cookie';

@Component({
  selector: 'app-bank',
  template: `
    <mat-card (click)="selectBank()">
      <mat-card-header>
        <mat-card-title>{{ bank.name }}</mat-card-title>
      </mat-card-header>
      <img mat-card-image [src]="bank.logo" />
    </mat-card>
  `,
  styles: [
    `
      :host,
      mat-card {
        display: block;
        width: 100%;
      }
      .mat-card-image {
        margin: auto;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BankComponent implements OnInit {
  @Input() bank: IBank;
  @Output() select = new EventEmitter<IBank>();

  constructor(private conf: ForgerockConfigService, private cookieService: CookieService) {}

  ngOnInit() {}

  selectBank() {
    const bearer = this.cookieService.get('bearer');
    window.location.href = `${this.conf.get('nodeBackend')}/banks/${this.bank.id}?token=${bearer}`;
  }
}
