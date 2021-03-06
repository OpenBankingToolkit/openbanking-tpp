import { BrowserModule } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';
import { NgModule, InjectionToken, APP_INITIALIZER } from '@angular/core';
import { StoreModule, ActionReducerMap } from '@ngrx/store';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { ForgerockSharedModule } from '@forgerock/openbanking-ngx-common/shared';
import {
  ForgerockConfigService,
  ForgerockConfigModule
} from '@forgerock/openbanking-ngx-common/services/forgerock-config';
import { AppComponent } from 'tpp/src/app/app.component';
import { TranslateSharedModule } from 'tpp/src/app/translate-shared.module';
import { MatSidenavModule } from '@angular/material/sidenav';
// @ts-ignore
import cssVars from 'tpp/src/scss/cssvars.scss';
import { AppRoutingModule } from 'tpp/src/app/app-routing.module';
import { environment } from 'tpp/src/environments/environment';
import rootReducer from 'tpp/src/store';
import { ShopSignModule } from './components/shop-sign/shop-sign.module';
import { ShopToolbarModule } from './components/shop-toolbar/shop-toolbar.module';
import { ShopMenuModule } from './components/shop-menu/shop-menu.module';

export const REDUCER_TOKEN = new InjectionToken<ActionReducerMap<{}>>('Registered Reducers');

export function getReducers() {
  return rootReducer;
}

export function createTranslateLoader(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}

export function init_app(appConfig: ForgerockConfigService) {
  return () => appConfig.fetchAndMerge(environment);
}

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    ForgerockSharedModule,
    ForgerockConfigModule.forRoot(),
    TranslateSharedModule,
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    AppRoutingModule,
    MatSidenavModule,
    ShopSignModule,
    ShopToolbarModule,
    ShopMenuModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: createTranslateLoader,
        deps: [HttpClient]
      }
    }),
    // Store
    StoreModule.forRoot(REDUCER_TOKEN),
    environment.devModules || []
  ],
  providers: [
    {
      provide: REDUCER_TOKEN,
      deps: [],
      useFactory: getReducers
    },
    {
      provide: APP_INITIALIZER,
      useFactory: init_app,
      deps: [ForgerockConfigService],
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
