import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import {
  Drzava,
  Grad,
  IzmenaKorisnikaRequest,
  Hotel,
  KreiranjeKorisnikaRequest,
  Korisnik,
  Recenzija,
  Rezervacija,
  Soba,
  StatusRezervacije,
  StatusSobe,
  StatusTiketa,
  Tiket,
  TiketUpdateRequest,
  TipSobe,
  Zaposleni
} from '../models';

function parametri(vrednosti: Record<string, string | null | undefined>): HttpParams {
  let params = new HttpParams();
  for (const [kljuc, vrednost] of Object.entries(vrednosti)) {
    if (vrednost) {
      params = params.set(kljuc, vrednost);
    }
  }
  return params;
}

@Injectable({ providedIn: 'root' })
export class DeskService {
  private readonly http = inject(HttpClient);

  hotel(id: number): Observable<Hotel> {
    return this.http.get<Hotel>(`${API_URL}/hoteli/${id}`);
  }

  mojiHoteli(): Observable<Hotel[]> {
    return this.http.get<Hotel[]>(`${API_URL}/hoteli/moji`);
  }

  dolasci(hotelId: number, datum: string): Observable<Rezervacija[]> {
    return this.http.get<Rezervacija[]>(`${API_URL}/hoteli/${hotelId}/dolasci`, { params: parametri({ datum }) });
  }

  odlasci(hotelId: number, datum: string): Observable<Rezervacija[]> {
    return this.http.get<Rezervacija[]>(`${API_URL}/hoteli/${hotelId}/odlasci`, { params: parametri({ datum }) });
  }

  rezervacijeHotela(hotelId: number, status?: StatusRezervacije): Observable<Rezervacija[]> {
    return this.http.get<Rezervacija[]>(`${API_URL}/hoteli/${hotelId}/rezervacije`, { params: parametri({ status }) });
  }

  promeniStatusRezervacije(id: number, status: StatusRezervacije): Observable<Rezervacija> {
    return this.http.patch<Rezervacija>(`${API_URL}/rezervacije/${id}/status`, { status });
  }

  sobe(hotelId: number, status?: StatusSobe): Observable<Soba[]> {
    return this.http.get<Soba[]>(`${API_URL}/hoteli/${hotelId}/sobe`, { params: parametri({ status }) });
  }

  promeniStatusSobe(id: number, status: StatusSobe): Observable<Soba> {
    return this.http.patch<Soba>(`${API_URL}/sobe/${id}/status`, { status });
  }

  tiketiHotela(hotelId: number, status?: StatusTiketa): Observable<Tiket[]> {
    return this.http.get<Tiket[]>(`${API_URL}/hoteli/${hotelId}/tiketi`, { params: parametri({ status }) });
  }

  dodeljeniTiketi(): Observable<Tiket[]> {
    return this.http.get<Tiket[]>(`${API_URL}/tiketi/dodeljeni`);
  }

  azurirajTiket(id: number, izmena: TiketUpdateRequest): Observable<Tiket> {
    return this.http.patch<Tiket>(`${API_URL}/tiketi/${id}`, izmena);
  }

  zaposleni(hotelId: number): Observable<Zaposleni[]> {
    return this.http.get<Zaposleni[]>(`${API_URL}/hoteli/${hotelId}/zaposleni`);
  }

  recenzije(hotelId: number): Observable<Recenzija[]> {
    return this.http.get<Recenzija[]>(`${API_URL}/hoteli/${hotelId}/recenzije`);
  }

  korisnici(): Observable<Korisnik[]> {
    return this.http.get<Korisnik[]>(`${API_URL}/korisnici`);
  }

  kreirajHotel(hotel: Hotel): Observable<Hotel> {
    return this.http.post<Hotel>(`${API_URL}/hoteli`, hotel);
  }

  drzave(): Observable<Drzava[]> {
    return this.http.get<Drzava[]>(`${API_URL}/drzave`);
  }

  kreirajGrad(grad: Grad): Observable<Grad> {
    return this.http.post<Grad>(`${API_URL}/gradovi`, grad);
  }

  kreirajTipSobe(tip: TipSobe): Observable<TipSobe> {
    return this.http.post<TipSobe>(`${API_URL}/tipovi-soba`, tip);
  }

  kreirajSobe(hotelId: number, sobe: Soba[]): Observable<Soba[]> {
    return this.http.post<Soba[]>(`${API_URL}/hoteli/${hotelId}/sobe`, { sobe });
  }

  kreirajZaposlenog(zaposleni: Zaposleni): Observable<Zaposleni> {
    return this.http.post<Zaposleni>(`${API_URL}/zaposleni`, zaposleni);
  }

  sviHoteli(): Observable<Hotel[]> {
    return this.http.get<Hotel[]>(`${API_URL}/hoteli`);
  }

  kreirajKorisnika(podaci: KreiranjeKorisnikaRequest): Observable<Korisnik> {
    return this.http.post<Korisnik>(`${API_URL}/korisnici`, podaci);
  }

  izmeniKorisnika(id: number, podaci: IzmenaKorisnikaRequest): Observable<Korisnik> {
    return this.http.put<Korisnik>(`${API_URL}/korisnici/${id}`, podaci);
  }

  obrisiKorisnika(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/korisnici/${id}`);
  }

  gradovi(): Observable<Grad[]> {
    return this.http.get<Grad[]>(`${API_URL}/gradovi`);
  }

  tipoviSoba(): Observable<TipSobe[]> {
    return this.http.get<TipSobe[]>(`${API_URL}/tipovi-soba`);
  }
}
