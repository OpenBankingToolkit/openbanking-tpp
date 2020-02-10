import { Component, OnInit, ChangeDetectionStrategy, Input } from '@angular/core';
import { MatSidenav } from '@angular/material';

@Component({
  selector: 'app-shop-toolbar',
  template: `
    <mat-toolbar fxLayout="row">
      <button mat-icon-button (click)="toggle()" aria-label="Open menu">
        <mat-icon>menu</mat-icon>
      </button>
      <img width="40" src="./assets/images/shop_logo.svg" />
      <span fxFlex>My e-shop</span>
      <button mat-icon-button routerLink="/cart" aria-label="Go to cart page">
        <mat-icon [matBadge]="cartBadge" matBadgeColor="warn">shopping_cart</mat-icon>
      </button></mat-toolbar
    >
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
  @Input() cartBadge = 0;
  @Input() sidenavRef: MatSidenav;
  constructor() {}

  ngOnInit() {}

  toggle() {
    this.sidenavRef.toggle();
  }
}
