import { Pol, Uloga } from './enums';

export interface Korisnik {
  id?: number;
  ime: string;
  prezime: string;
  email?: string;
  brojTelefona?: string;
  pol?: Pol;
  uloga?: Uloga;
}

export interface PromenaLozinkeRequest {
  staraLozinka: string;
  novaLozinka: string;
}
