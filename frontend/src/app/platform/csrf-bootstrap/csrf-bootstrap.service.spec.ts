import { HttpClient, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { CsrfBootstrapService } from './csrf-bootstrap.service';

describe('CsrfBootstrapService', () => {
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CsrfBootstrapService, provideHttpClient(), provideHttpClientTesting()],
    });
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    document.cookie = 'XSRF-TOKEN=; Max-Age=0; Path=/';
    httpTesting.verify();
  });

  it('bootstraps through the relative API boundary', async () => {
    const initialization = TestBed.inject(CsrfBootstrapService).initialize();
    const request = httpTesting.expectOne({ method: 'GET', url: '/api/csrf' });

    expect(request.request.url).not.toMatch(/^https?:\/\//);
    request.flush(null, { status: 204, statusText: 'No Content' });

    await expect(initialization).resolves.toBeUndefined();
  });

  it('keeps bootstrap failure observable without retrying', async () => {
    const initialization = TestBed.inject(CsrfBootstrapService).initialize();
    const request = httpTesting.expectOne({ method: 'GET', url: '/api/csrf' });

    request.flush(
      { code: 'DEPENDENCY_UNAVAILABLE' },
      { status: 503, statusText: 'Service Unavailable' },
    );

    await expect(initialization).rejects.toMatchObject({ status: 503 });
    httpTesting.expectNone('/api/csrf');
  });

  it('adds Angular default XSRF header to a relative unsafe request', async () => {
    document.cookie = 'XSRF-TOKEN=test-csrf-token; Path=/';

    const response = firstValueFrom(TestBed.inject(HttpClient).post<void>('/api/test-mutation', {}));
    const request = httpTesting.expectOne({ method: 'POST', url: '/api/test-mutation' });

    expect(request.request.headers.get('X-XSRF-TOKEN')).toBe('test-csrf-token');
    request.flush(null, { status: 204, statusText: 'No Content' });
    await expect(response).resolves.toBeNull();
  });
});
