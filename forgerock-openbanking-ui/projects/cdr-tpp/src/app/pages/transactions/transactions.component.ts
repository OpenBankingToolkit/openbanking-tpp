import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  ElementRef,
  NgZone,
  OnDestroy
} from '@angular/core';
import { CdkScrollable, ScrollDispatcher } from '@angular/cdk/overlay';

@Component({
  selector: 'app-transactions',
  template: `
    <app-transactions-container></app-transactions-container>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionsComponent implements OnInit, OnDestroy {
  cdkScrollable: CdkScrollable;
  constructor(private hostRef: ElementRef, private scrollDispatcher: ScrollDispatcher, protected ngZone: NgZone) {}

  ngOnInit() {
    this.cdkScrollable = new CdkScrollable(this.hostRef, this.scrollDispatcher, this.ngZone);
    this.cdkScrollable.ngOnInit();
    // this.scrollDispatcher.register(this.cdkScrollable);
  }
  ngOnDestroy() {
    this.cdkScrollable.ngOnDestroy();
    // this.scrollDispatcher.deregister(this.cdkScrollable);
  }
}
