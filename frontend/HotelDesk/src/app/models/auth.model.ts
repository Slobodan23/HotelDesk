import { Pol } from './enums';
import { Korisnik } from './korisnik.model';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegistracijaRequest {
  ime: string;
  prezime: string;
  email: string;
  password: string;
  brojTelefona?: string;
  pol?: Pol;
}

export interface AuthResponse {
  token: string;
  korisnik: Korisnik;
}
