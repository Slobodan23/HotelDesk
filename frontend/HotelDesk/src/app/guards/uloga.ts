import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Korisnik, TipZaposlenog, Uloga } from '../models';
import { AuthService } from '../services/auth.service';

export type Desk = 'recepcija' | 'ciscenje' | 'menadzer' | 'vlasnik' | 'admin';

const DESK_ZAPOSLENOG: Record<TipZaposlenog, Desk> = {
  RECEPCIONER: 'recepcija',
  CISTAC: 'ciscenje',
  MENADZER: 'menadzer'
};

export function deskZa(korisnik: Korisnik | null): Desk | null {
  switch (korisnik?.uloga) {
    case 'ADMIN':
      return 'admin';
    case 'VLASNIK':
      return 'vlasnik';
    case 'ZAPOSLENI':
      return korisnik.tipZaposlenog ? DESK_ZAPOSLENOG[korisnik.tipZaposlenog] : null;
    default:
      return null;
  }
}

export function pocetnaZa(korisnik: Korisnik | null): string {
  const desk = deskZa(korisnik);
  return desk ? `/${desk}` : '/';
}

export function samoUloga(uloga: Uloga, tip?: TipZaposlenog): CanActivateFn {
  return () => {
    const korisnik = inject(AuthService).korisnik();
    const dozvoljeno = korisnik?.uloga === uloga && (!tip || korisnik.tipZaposlenog === tip);
    return dozvoljeno ? true : inject(Router).parseUrl(pocetnaZa(korisnik));
  };
}
