import { Component, Input } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { Observable } from 'rxjs';

import { IState, ShopItem } from 'tpp/src/store/models';
import { selectShopSelectedItems, GetShopRemoveAllAction } from 'tpp/src/store/reducers/shop';

@Component({
  selector: 'app-cart-container',
  template: `
    <app-cart [items]="items$ | async" (clear)="clear($event)"></app-cart>
  `
})
export class CartContainer {
  items$: Observable<ShopItem[]> = this.store.pipe(select(selectShopSelectedItems));

  constructor(private store: Store<IState>) {}

  clear(id: string) {
    this.store.dispatch(new GetShopRemoveAllAction());
  }
}
