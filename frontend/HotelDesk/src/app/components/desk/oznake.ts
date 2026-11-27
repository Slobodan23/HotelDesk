import { StatusRezervacije, StatusSobe, StatusTiketa, TipTiketa, TipZaposlenog, Uloga } from '../../models';

export const STATUS_REZERVACIJE: Record<StatusRezervacije, string> = {
  KREIRANA: 'Čeka potvrdu',
  POTVRDJENA: 'Potvrđena',
  PRIJAVLJEN: 'Gost u hotelu',
  ODJAVLJEN: 'Odjavljen',
  OTKAZANA: 'Otkazana'
};

export const STATUS_SOBE: Record<StatusSobe, string> = {
  CISTA: 'Čista',
  PRLJAVA: 'Za čišćenje',
  U_ODRZAVANJU: 'U održavanju'
};

export const STATUS_TIKETA: Record<StatusTiketa, string> = {
  OTVOREN: 'Otvoren',
  U_TOKU: 'U toku',
  ZATVOREN: 'Zatvoren'
};

export const TIP_TIKETA: Record<TipTiketa, string> = {
  PRIJAVA_PROBLEMA: 'Kvar',
  ZAHTEV_ZA_CISCENJE: 'Čišćenje'
};

export const TIP_ZAPOSLENOG: Record<TipZaposlenog, string> = {
  RECEPCIONER: 'Recepcioner',
  CISTAC: 'Čistač',
  MENADZER: 'Menadžer'
};

export const ULOGA: Record<Uloga, string> = {
  ADMIN: 'Admin',
  VLASNIK: 'Vlasnik',
  ZAPOSLENI: 'Zaposleni',
  GOST: 'Gost'
};

export function danas(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

export function brojNoci(od: string, doDatuma: string): number {
  return Math.round((new Date(doDatuma).getTime() - new Date(od).getTime()) / 86400000);
}
