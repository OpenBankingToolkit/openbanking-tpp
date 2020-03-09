import { Component, Input, OnInit } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { Observable } from 'rxjs';

import { IState, IBank } from 'cdr-tpp/src/models';
import { selectIsLoading, selectBanksSelector, GetBanksRequestAction } from 'cdr-tpp/src/store/reducers/banks';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-banks-container',
  template: `
    <app-banks [isLoading]="isLoading$ | async" (select)="select($event)" [banks]="banks$ | async"></app-banks>
  `
})
export class BanksContainer implements OnInit {
  public isLoading$: Observable<boolean> = this.store.pipe(select(selectIsLoading));
  public banks$: Observable<IBank[] | null> = this.store.pipe(select(selectBanksSelector));

  constructor(private store: Store<IState>) {}

  ngOnInit() {
    this.banks$.pipe(first()).subscribe(data => !!!data && this.store.dispatch(new GetBanksRequestAction()));
  }

  select(bank: IBank) {
    this.store.dispatch(new GetBanksRequestAction());
  }
}
