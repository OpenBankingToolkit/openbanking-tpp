import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppNativeModule } from 'tpp/src/app/app-native.module';
import { environment } from 'tpp/src/environments/environment';

if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic()
  .bootstrapModule(AppNativeModule)
  .catch(err => console.log(err));
