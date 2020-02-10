import { Component, OnInit, ChangeDetectionStrategy, Input, EventEmitter, Output } from '@angular/core';
import _get from 'lodash-es/get';

import { ShopItem } from 'tpp/src/store/models';

@Component({
  selector: 'app-shop',
  template: `
    <div fxLayoutGap="20px grid" fxLayout="column" fxLayout.gt-xs="row wrap">
      <div *ngFor="let item of items" fxFlex fxFlex.gt-xs="50" fxFlex.gt-sm="33" fxFlex.gt-md="25">
        <mat-card fxLayout="column">
          <mat-card-header>
            <mat-card-title>{{ item.title }}</mat-card-title>
          </mat-card-header>
          <img mat-card-image [src]="item.img" alt="Dress image" />
          <mat-card-content>
            <p>
              {{ item.description }}
            </p>
          </mat-card-content>
          <span fxFlex></span>
          <mat-card-actions>
            <span fxFlex>{{ item.price | currency: 'GBP' }}</span>
            <button
              [disabled]="selected.includes(item.id)"
              mat-raised-button
              color="primary"
              (click)="select.emit(item.id)"
            >
              <mat-icon>add_shopping_cart</mat-icon> Add to cart
            </button>
          </mat-card-actions>
        </mat-card>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
        height: 100%;
        width: 100%;
      }

      mat-card {
        height: 100%;
      }

      mat-card img {
        max-width: initial !important;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TppShopComponent implements OnInit {
  @Input() items: ShopItem[];
  @Input() selected: number[];
  @Output() select = new EventEmitter<string>();

  constructor() {}

  ngOnInit() {}
}
