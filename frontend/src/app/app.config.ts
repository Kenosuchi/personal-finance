import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient } from '@angular/common/http';
import { CsrfBootstrapService } from './platform/csrf-bootstrap/csrf-bootstrap.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(),
    provideAppInitializer(() => inject(CsrfBootstrapService).initialize()),
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
  ],
};
