import { Component } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { Observable } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { map } from 'rxjs/operators';
import { Router } from '@angular/router';

import { IState, ShopItem } from 'tpp/src/store/models';
import { selectShopSelected, selectShopItems, GetShopAddAction } from 'tpp/src/store/reducers/shop';

@Component({
  selector: 'app-shop-container',
  template: `
    <app-shop [selected]="selected$ | async" [items]="items$ | async" (select)="select($event)"></app-shop>
  `
})
export class TppShopContainer {
  selected$: Observable<number[]> = this.store.pipe(select(selectShopSelected));
  items$: Observable<ShopItem[]> = this.store.pipe(select(selectShopItems)).pipe(map(items => Object.values(items)));

  constructor(private store: Store<IState>, private snackBar: MatSnackBar, private router: Router) {}

  select(id: string) {
    this.store.dispatch(new GetShopAddAction({ id }));
    const snackBar = this.snackBar.open('Item added!', 'Go to cart', {
      duration: 4000
    });

    snackBar.onAction().subscribe(() => {
      console.log('The snack-bar action was triggered!');
      this.router.navigate(['/cart']);

      snackBar.dismiss();
    });
  }
}
