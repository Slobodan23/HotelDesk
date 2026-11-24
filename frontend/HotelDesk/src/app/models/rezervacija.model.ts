import { NacinPlacanja, StatusRezervacije } from './enums';
import { Placanje } from './placanje.model';

export interface Rezervacija {
  id?: number;
  sobaId: number;
  datumDolaska: string;
  datumOdlaska: string;
  brojGostiju: number;
  nacinPlacanja?: NacinPlacanja;
  karticaId?: number;
  ukupnaCena?: number;
  status?: StatusRezervacije;
  datumKreiranja?: string;
  gostId?: number;
  gostImePrezime?: string;
  brojSobe?: number;
  hotelId?: number;
  hotelNaziv?: string;
  placanje?: Placanje | null;
}

export interface RezervacijaStatusRequest {
  status: StatusRezervacije;
}
