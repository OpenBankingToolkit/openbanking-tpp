import { Component, Input } from '@angular/core';
import { Store } from '@ngrx/store';
import { MatSidenav } from '@angular/material/sidenav';
import { IState } from 'cdr-tpp/src/models';

@Component({
  selector: 'app-shop-toolbar-container',
  template: `
    <app-shop-toolbar [sidenavRef]="sidenavRef"></app-shop-toolbar>
  `
})
export class ShopToolbarContainer {
  @Input() sidenavRef: MatSidenav;

  constructor(private store: Store<IState>) {}
}
