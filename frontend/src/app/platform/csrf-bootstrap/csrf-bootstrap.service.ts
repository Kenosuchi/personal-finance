import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_BASE_PATH } from '../http/api-base-path';
import { firstValueFrom } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CsrfBootstrapService {
  private readonly http = inject(HttpClient);
  private readonly apiBasePath = inject(API_BASE_PATH);

  async initialize(): Promise<void> {
    await firstValueFrom(this.http.get<void>(`${this.apiBasePath}/csrf`));
  }
}
