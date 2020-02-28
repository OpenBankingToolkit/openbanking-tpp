import { Component, OnInit, ChangeDetectionStrategy, Input } from '@angular/core';
import { MatSidenav } from '@angular/material/sidenav';

@Component({
  selector: 'app-shop-menu',
  template: `
    <button mat-button mat-flat-button color="primary" routerLink="/shop" (click)="toggle()">Shop</button>
  `,
  styles: [
    `
      :host {
        display: block;
        width: 100%;
      }

      button {
        width: 100%;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ShopMenuComponent implements OnInit {
  @Input() sidenavRef: MatSidenav;
  constructor() {}

  ngOnInit() {}

  toggle() {
    this.sidenavRef.toggle();
  }
}
