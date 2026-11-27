import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const samoGost: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.prijavljen() ? inject(Router).createUrlTree(['/']) : true;
};

export const samoPrijavljen: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  return auth.prijavljen() ? true : inject(Router).createUrlTree(['/prijava'], { queryParams: { povratak: state.url } });
};
