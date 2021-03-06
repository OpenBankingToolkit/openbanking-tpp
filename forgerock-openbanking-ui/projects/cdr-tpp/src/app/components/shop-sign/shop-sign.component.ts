import { Component, OnInit, ChangeDetectionStrategy, Input } from '@angular/core';
import { MatSidenav } from '@angular/material/sidenav';
import { ForgerockConfigService } from '@forgerock/openbanking-ngx-common/services/forgerock-config';
import { AnimationItem } from 'lottie-web';
import { AnimationOptions } from 'ngx-lottie';

@Component({
  selector: 'app-shop-sign',
  template: `
    <mat-card routerLink="/" fxLayout="column" fxLayoutAlign="center center" (click)="toggle()">
      <ng-lottie [options]="options" (animationCreated)="animationCreated($event)"></ng-lottie>
      <h1>{{ appName }}</h1>
    </mat-card>
  `,
  styles: [
    `
      mat-card {
        cursor: pointer;
      }
      mat-card:hover {
        box-shadow: 0px 2px 10px -1px rgba(0, 0, 0, 0.2), 0px 1px 1px 0px rgba(0, 0, 0, 0.14),
          0px 1px 3px 0px rgba(0, 0, 0, 0.12);
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ShopSignComponent implements OnInit {
  @Input() sidenavRef: MatSidenav;
  options: AnimationOptions = {
    path: '/assets/animations/piggy-bank.json'
  };
  appName: '';

  constructor(private appConfig: ForgerockConfigService) {
    this.appName = this.appConfig.get('client.name', 'MoneyWatch');
  }

  animationCreated(animationItem: AnimationItem): void {
    console.log(animationItem);
  }

  ngOnInit() {}

  toggle() {
    this.sidenavRef.toggle();
  }
}
