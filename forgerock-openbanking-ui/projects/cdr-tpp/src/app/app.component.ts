import { Component, Inject, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Platform } from '@angular/cdk/platform';
import { DOCUMENT } from '@angular/common';
import { TranslateService } from '@ngx-translate/core';
import { MediaMatcher } from '@angular/cdk/layout';

import { ForgerockSplashscreenService } from '@forgerock/openbanking-ngx-common/services/forgerock-splashscreen';

@Component({
  selector: 'app-root',
  template: `
    <mat-sidenav-container>
      <mat-sidenav #snav [fixedInViewport]="mobileQuery.matches" [mode]="mobileQuery.matches ? 'over' : 'push'">
        <!-- <app-shop-sign [sidenavRef]="snav"></app-shop-sign><app-shop-menu [sidenavRef]="snav"></app-shop-menu -->
      ></mat-sidenav>
      <mat-sidenav-content [class]="mobileQuery.matches ? 'mobile' : 'desktop'">
        <app-shop-toolbar-container [sidenavRef]="snav"></app-shop-toolbar-container>
        <div class="toolbar-placeholder" [style.height.px]="mobileQuery.matches ? 56 : 64"></div>
        <router-outlet></router-outlet
      ></mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnDestroy {
  mobileQuery: MediaQueryList;
  private _mobileQueryListener: () => void;

  constructor(
    @Inject(DOCUMENT) private document: any,
    private splashscreenService: ForgerockSplashscreenService,
    private platform: Platform,
    private translateService: TranslateService,
    private media: MediaMatcher,
    private changeDetectorRef: ChangeDetectorRef
  ) {
    this.splashscreenService.init();

    this.translateService.addLangs(['en', 'fr']);
    this.translateService.setDefaultLang('en');
    this.translateService.use(this.translateService.getBrowserLang() || 'en');

    // Add is-mobile class to the body if the platform is mobile
    if (this.platform.ANDROID || this.platform.IOS) {
      this.document.body.classList.add('is-mobile');
    }

    this.mobileQuery = this.media.matchMedia('(max-width: 600px)');
    this._mobileQueryListener = () => this.changeDetectorRef.detectChanges();
    this.mobileQuery.addListener(this._mobileQueryListener);
  }

  ngOnDestroy(): void {
    this.mobileQuery.removeListener(this._mobileQueryListener);
  }
}
