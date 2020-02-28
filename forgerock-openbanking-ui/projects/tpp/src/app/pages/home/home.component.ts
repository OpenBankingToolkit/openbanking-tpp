import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-home',
  template: `
    <mat-card>
      <forgerock-customer-logo></forgerock-customer-logo>
      <h1>Welcome to ForgeRock Shop!</h1>
      <p>This is a PISP provided as part of the ForgeRock Open Banking Sandbox</p>
      <button mat-raised-button color="primary" routerLink="/shop">Go shopping!</button>
    </mat-card>
  `,
  styles: [
    `
      h1 {
        margin: 1em 0;
      }
      mat-card {
        text-align: center;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomeComponent implements OnInit {
  constructor() {}

  ngOnInit() {}
}
