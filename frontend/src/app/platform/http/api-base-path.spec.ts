import { HttpClient, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { inject, Injectable } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { API_BASE_PATH } from './api-base-path';

@Injectable()
class ApiBoundaryProbe {
  private readonly http = inject(HttpClient);
  private readonly basePath = inject(API_BASE_PATH);

  request() {
    return this.http.get(`${this.basePath}/foundation-check`);
  }
}

describe('API HTTP boundary', () => {
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ApiBoundaryProbe, provideHttpClient(), provideHttpClientTesting()],
    });

    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('uses the same-origin relative /api path', async () => {
    const response = firstValueFrom(TestBed.inject(ApiBoundaryProbe).request());
    const request = httpTesting.expectOne({
      method: 'GET',
      url: '/api/foundation-check',
    });

    expect(request.request.url).not.toMatch(/^https?:\/\//);

    request.flush({ status: 'ok' });
    await expect(response).resolves.toEqual({ status: 'ok' });
  });
});
