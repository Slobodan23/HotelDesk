import { Adresa } from './adresa.model';

export interface Hotel {
  id?: number;
  naziv: string;
  opis?: string;
  brojZvezdica?: number;
  email?: string;
  brojTelefona?: string;
  adresa: Adresa;
  vlasnikId?: number;
  vlasnikImePrezime?: string;
  prosecnaOcena?: number | null;
}
