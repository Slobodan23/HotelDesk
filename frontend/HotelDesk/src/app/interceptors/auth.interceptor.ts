import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { API_URL } from '../api.config';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const token = req.url.startsWith(API_URL) ? auth.token : null;
  const zahtev = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(zahtev).pipe(
    catchError((greska: unknown) => {
      if (token && greska instanceof HttpErrorResponse && greska.status === 401) {
        auth.odjava();
        router.navigate(['/prijava'], { queryParams: { povratak: router.url } });
      }
      return throwError(() => greska);
    })
  );
};
