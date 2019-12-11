import { Component, Inject } from '@angular/core';
import { Platform } from '@angular/cdk/platform';
import { DOCUMENT } from '@angular/common';
import { TranslateService } from '@ngx-translate/core';

import { ForgerockConfigService } from 'ob-ui-libs/services/forgerock-config';
import { ForgerockSplashscreenService } from 'ob-ui-libs/services/forgerock-splashscreen';
import { ForgerockGDPRService } from 'ob-ui-libs/gdpr';
import { ForgerockNativeSplashscreenService, ForgerockNativeDeepLinkService } from 'ob-ui-libs/native';

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
  enableCustomization: string = this.configService.get('enableCustomization');

  constructor(
    @Inject(DOCUMENT) private document: any,
    private splashscreenService: ForgerockSplashscreenService,
    private configService: ForgerockConfigService,
    private platform: Platform,
    private translateService: TranslateService,
    private gdprService: ForgerockGDPRService,
    private nativeSplashscreen: ForgerockNativeSplashscreenService,
    private nativeDeeplink: ForgerockNativeDeepLinkService
  ) {
    this.nativeDeeplink.init();
    this.splashscreenService.init();
    this.gdprService.init();
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