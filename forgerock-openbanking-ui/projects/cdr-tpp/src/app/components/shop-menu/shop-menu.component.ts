import { Component, OnInit, ChangeDetectionStrategy, Input } from '@angular/core';
import { MatSidenav } from '@angular/material/sidenav';
import { CookieService } from 'ngx-cookie';
import { Router } from '@angular/router';

@Component({
  selector: 'app-shop-menu',
  template: `
    <!-- <button mat-button mat-flat-button color="primary" routerLink="/shop" (click)="toggle()">Shop</button> -->
    <button mat-raised-button color="warn" (click)="logout()"><mat-icon>exit_to_app</mat-icon> Logout</button>
  `,
  styles: [
    `
      :host {
        display: block;
        width: 100%;
      }

      button {
        width: 100%;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ShopMenuComponent implements OnInit {
  @Input() sidenavRef: MatSidenav;
  constructor(private router: Router, private cookieService: CookieService) {}

  ngOnInit() {}

  toggle() {
    this.sidenavRef.toggle();
  }

  logout() {
    this.cookieService.remove('bearer');
    this.sidenavRef.toggle();
    this.router.navigate(['/']);
  }
}
