import { BrowserModule } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';
import { NgModule, InjectionToken, APP_INITIALIZER } from '@angular/core';
import { StoreModule, ActionReducerMap } from '@ngrx/store';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { ForgerockSharedModule } from '@forgerock/openbanking-ngx-common/shared';
import { ForgerockConfigService } from '@forgerock/openbanking-ngx-common/services/forgerock-config';
import { ForgerockConfigModule } from '@forgerock/openbanking-ngx-common/services/forgerock-config';
import { ForgerockNativeModule } from '@forgerock/openbanking-ngx-common/native';
import { AppNativeComponent } from 'tpp/src/app/app-native.component';
import { TranslateSharedModule } from 'tpp/src/app/translate-shared.module';
// @ts-ignore
import cssVars from 'tpp/src/scss/cssvars.scss';
import { AppRoutingModule } from 'tpp/src/app/app-routing.module';
import { environment } from 'tpp/src/environments/environment';
import rootReducer from 'tpp/src/store';

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
  declarations: [AppNativeComponent],
  imports: [
    BrowserModule,
    ForgerockSharedModule,
    ForgerockConfigModule.forRoot(),
    ForgerockNativeModule,
    TranslateSharedModule,
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    AppRoutingModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: createTranslateLoader,
        deps: [HttpClient]
      }
    }),
    // Store
    StoreModule.forRoot(REDUCER_TOKEN)
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
  bootstrap: [AppNativeComponent]
})
export class AppNativeModule {}
