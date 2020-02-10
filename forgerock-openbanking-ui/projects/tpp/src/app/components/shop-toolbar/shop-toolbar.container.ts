import { Component, Input } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { MatSidenav } from '@angular/material';
import { Observable } from 'rxjs';

import { IState } from 'tpp/src/store/models';
import { selectShopSelected } from 'tpp/src/store/reducers/shop';

@Component({
  selector: 'app-shop-toolbar-container',
  template: `
    <app-shop-toolbar [cartBadge]="(selected$ | async).length" [sidenavRef]="sidenavRef"></app-shop-toolbar>
  `
})
export class ShopToolbarContainer {
  @Input() sidenavRef: MatSidenav;
  selected$: Observable<number[]> = this.store.pipe(select(selectShopSelected));

  constructor(private store: Store<IState>) {}
}
