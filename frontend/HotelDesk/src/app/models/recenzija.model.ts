export interface Recenzija {
  id?: number;
  rezervacijaId: number;
  ocena: number;
  tekstRecenzije?: string;
  datum?: string;
  gostId?: number;
  gostImePrezime?: string;
  hotelId?: number;
}
