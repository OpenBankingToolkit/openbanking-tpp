import { Component, OnInit, ChangeDetectionStrategy, Input } from '@angular/core';
import { MatSidenav } from '@angular/material/sidenav';
import { ForgerockConfigService } from '@forgerock/openbanking-ngx-common/services/forgerock-config';

import { environment } from 'cdr-tpp/src/environments/environment';

@Component({
  selector: 'app-shop-toolbar',
  template: `
    <mat-toolbar fxLayout="row">
      <button mat-icon-button (click)="toggle()" aria-label="Open menu">
        <mat-icon>menu</mat-icon>
      </button>
      <img width="40" src="./assets/images/logo.svg" />
      <span fxFlex>{{ appName }}</span>
    </mat-toolbar>
  `,
  styles: [
    `
      :host {
        display: block;
        position: fixed;
        width: 100%;
        z-index: 2;
      }
      img {
        margin-right: 0.5em;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ShopToolbarComponent implements OnInit {
  @Input() sidenavRef: MatSidenav;
  appName: '';
  constructor(private appConfig: ForgerockConfigService) {
    this.appName = this.appConfig.get('client.name', 'MoneyWatch');
  }

  ngOnInit() {}

  toggle() {
    this.sidenavRef.toggle();
  }
}
