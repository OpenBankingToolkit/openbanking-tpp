import { Component, Inject } from '@angular/core';
import { Platform } from '@angular/cdk/platform';
import { DOCUMENT } from '@angular/common';
import { TranslateService } from '@ngx-translate/core';

import { ForgerockSplashscreenService } from '@forgerock/openbanking-ngx-common/services/forgerock-splashscreen';
import {
  ForgerockNativeSplashscreenService,
  ForgerockNativeDeepLinkService
} from '@forgerock/openbanking-ngx-common/native';

@Component({
  selector: 'app-root',
  template: `
    <router-outlet></router-outlet>
  `,
  styles: [
    `
      :host {
        position: relative;
        display: flex;
        flex: 1 1 auto;
        width: 100%;
        height: 100%;
        min-width: 0;
      }
    `
  ]
})
export class AppNativeComponent {
  constructor(
    @Inject(DOCUMENT) private document: any,
    private splashscreenService: ForgerockSplashscreenService,
    private platform: Platform,
    private translateService: TranslateService,
    private nativeSplashscreen: ForgerockNativeSplashscreenService,
    private nativeDeeplink: ForgerockNativeDeepLinkService
  ) {
    this.nativeDeeplink.init();
    this.splashscreenService.init();
    this.nativeSplashscreen.hide();

    this.translateService.addLangs(['en', 'fr']);
    this.translateService.setDefaultLang('en');
    this.translateService.use(this.translateService.getBrowserLang() || 'en');

    // Add is-mobile class to the body if the platform is mobile
    if (this.platform.ANDROID || this.platform.IOS) {
      this.document.body.classList.add('is-mobile');
    }
  }
}
