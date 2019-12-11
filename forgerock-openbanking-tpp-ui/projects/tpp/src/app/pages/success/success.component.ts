import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-success',
  template: `
    <div fxLayout="column" fxLayoutAlign="center center">
      <forgerock-alert color="primary">Transaction successful!</forgerock-alert>
      <button type="button" mat-raised-button color="accent" routerLink="/">
        Simulate another transaction
      </button>
    </div>
  `,
  styles: [
    `
      :host > div {
        height: 100%;
      }
      :host > div > forgerock-alert {
        margin-bottom: 20px;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TppSuccessComponent implements OnInit {
  constructor() {}

  ngOnInit() {}
}
