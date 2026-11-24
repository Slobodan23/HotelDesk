import { Pol, TipZaposlenog } from './enums';

export interface Zaposleni {
  id?: number;
  ime: string;
  prezime: string;
  email: string;
  password?: string;
  brojTelefona?: string;
  pol?: Pol;
  tipZaposlenog: TipZaposlenog;
  hotelId: number;
  hotelNaziv?: string;
}
