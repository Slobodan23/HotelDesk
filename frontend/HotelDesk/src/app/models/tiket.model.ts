import { StatusTiketa, TipTiketa } from './enums';

export interface Tiket {
  id?: number;
  hotelId: number;
  sobaId?: number | null;
  tipTiketa: TipTiketa;
  sadrzaj: string;
  status?: StatusTiketa;
  datumKreiranja?: string;
  datumZatvaranja?: string | null;
  brojSobe?: number | null;
  kreiraoId?: number | null;
  kreiraoImePrezime?: string | null;
  dodeljenId?: number | null;
  dodeljenImePrezime?: string | null;
}

export interface TiketUpdateRequest {
  status?: StatusTiketa;
  dodeljenId?: number;
}
