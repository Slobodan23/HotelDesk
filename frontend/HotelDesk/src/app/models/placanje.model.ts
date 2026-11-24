import { NacinPlacanja } from './enums';
import { Kartica } from './kartica.model';

export interface Placanje {
  id: number;
  iznos: number;
  nacinPlacanja: NacinPlacanja;
  placeno: boolean;
  datumPlacanja: string | null;
  kartica: Kartica | null;
}
