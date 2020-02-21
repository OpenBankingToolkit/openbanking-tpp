import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-accounts',
  template: `
    <app-accounts-container></app-accounts-container>
  `,
  styles: [``],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AccountsComponent implements OnInit {
  constructor() {}

  ngOnInit() {}
}
