import { Pol, TipZaposlenog, Uloga } from './enums';

export interface Korisnik {
  id?: number;
  ime: string;
  prezime: string;
  email?: string;
  brojTelefona?: string;
  pol?: Pol;
  uloga?: Uloga;
  tipZaposlenog?: TipZaposlenog | null;
  hotelId?: number | null;
}

export interface PromenaLozinkeRequest {
  staraLozinka: string;
  novaLozinka: string;
}

export interface KreiranjeKorisnikaRequest {
  ime: string;
  prezime: string;
  email: string;
  password: string;
  brojTelefona?: string;
  pol?: Pol;
  uloga: Uloga;
  tipZaposlenog?: TipZaposlenog;
  hotelId?: number;
}

export interface IzmenaKorisnikaRequest {
  ime: string;
  prezime: string;
  email: string;
  novaLozinka?: string;
  brojTelefona?: string;
  pol?: Pol;
  uloga: Uloga;
  tipZaposlenog?: TipZaposlenog;
  hotelId?: number;
}
