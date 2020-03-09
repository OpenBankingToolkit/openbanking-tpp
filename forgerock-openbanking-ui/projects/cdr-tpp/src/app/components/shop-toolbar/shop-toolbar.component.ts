import { Component, OnInit, ChangeDetectionStrategy, Input, EventEmitter, Output } from '@angular/core';
import { MatSidenav } from '@angular/material/sidenav';
import { ForgerockConfigService } from '@forgerock/openbanking-ngx-common/services/forgerock-config';

import { IUser } from 'cdr-tpp/src/models';

@Component({
  selector: 'app-shop-toolbar',
  template: `
    <mat-toolbar color="primary">
      <!-- <button mat-icon-button (click)="toggle()" aria-label="Open menu">
        <mat-icon>menu</mat-icon>
      </button> -->
      <button style="font-size: 1em; min-width: auto;" mat-button routerLink="/" aria-label="Home">
        <span>{{ appName }}</span>
      </button>
      <span fxFlex></span>
      <button *ngIf="user" mat-button [matMenuTriggerFor]="userMenu" class="button-user">
        <span class="email">{{ user.email }}</span>
        <mat-icon>keyboard_arrow_down</mat-icon>
      </button>
      <mat-menu #userMenu="matMenu" [overlapTrigger]="false" xPosition="before">
        <button mat-menu-item (click)="onLogout()">
          <mat-icon>exit_to_app</mat-icon>
          <span>Signout</span>
        </button>
      </mat-menu>
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
      mat-toolbar {
        box-sizing: content-box;
      }
      img {
        margin-right: 0.5em;
      }
      ::ng-deep .button-user > span {
        display: flex;
        flex-direction: row;
        align-items: center;
      }
      ::ng-deep .button-user .email {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ShopToolbarComponent implements OnInit {
  @Input() user: IUser;
  @Input() sidenavRef: MatSidenav;
  @Output() logout = new EventEmitter<void>();
  appName: '';
  constructor(private appConfig: ForgerockConfigService) {
    this.appName = this.appConfig.get('client.name', 'MoneyWatch');
  }

  ngOnInit() {}

  toggle() {
    this.sidenavRef.toggle();
  }

  onLogout() {
    this.logout.emit();
  }
}
