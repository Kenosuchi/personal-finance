import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { App } from './app';
import { routes } from './app.routes';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter(routes)],
    }).compileComponents();
  });

  async function renderAt(url: string) {
    const fixture = TestBed.createComponent(App);
    const router = TestBed.inject(Router);

    await router.navigateByUrl(url);
    await fixture.whenStable();

    return { fixture, router };
  }

  it('renders the common shell and foundation message at the root route', async () => {
    const { fixture } = await renderAt('/');
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('header')?.textContent).toContain('Personal Finance');
    expect(element.querySelector('main h1')?.textContent).toBe('Personal Finance');
    expect(element.querySelector('main')?.textContent).toContain(
      'Finance workflows are not available yet.',
    );
  });

  it('renders an unknown route inside the common shell', async () => {
    const { fixture } = await renderAt('/missing/nested-route');
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('header')?.textContent).toContain('Personal Finance');
    expect(element.querySelector('main h1')?.textContent).toBe('Page not found');
    expect(element.querySelector('main a')?.textContent).toContain('Return to Home');
  });

  it('navigates from the unknown route back to the root', async () => {
    const { fixture, router } = await renderAt('/missing');
    const link = fixture.nativeElement.querySelector('main a') as HTMLAnchorElement;

    link.click();
    await fixture.whenStable();

    expect(router.url).toBe('/');
    expect((fixture.nativeElement as HTMLElement).querySelector('main')?.textContent).toContain(
      'Finance workflows are not available yet.',
    );
  });
});
