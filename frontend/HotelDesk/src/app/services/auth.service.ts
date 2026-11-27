import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, switchMap, tap } from 'rxjs';
import { API_URL } from '../api.config';
import { AuthResponse, Korisnik, LoginRequest, RegistracijaRequest } from '../models';

const KLJUC_TOKEN = 'hoteldesk.token';
const KLJUC_KORISNIK = 'hoteldesk.korisnik';

function citaj(kljuc: string): string | null {
  try {
    return localStorage.getItem(kljuc);
  } catch {
    return null;
  }
}

function istekao(token: string): boolean {
  try {
    const telo = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    return typeof telo.exp === 'number' && telo.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/auth`;

  private readonly stanje = signal<AuthResponse | null>(this.ucitaj());

  readonly korisnik = computed<Korisnik | null>(() => this.stanje()?.korisnik ?? null);
  readonly prijavljen = computed(() => this.stanje() != null);

  get token(): string | null {
    const s = this.stanje();
    if (s && istekao(s.token)) {
      this.odjava();
      return null;
    }
    return s?.token ?? null;
  }

  login(podaci: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.url}/login`, podaci).pipe(tap((odgovor) => this.sacuvaj(odgovor)));
  }

  registracija(podaci: RegistracijaRequest): Observable<AuthResponse> {
    return this.http
      .post<Korisnik>(`${this.url}/registracija`, podaci)
      .pipe(switchMap(() => this.login({ email: podaci.email, password: podaci.password })));
  }

  odjava(): void {
    this.stanje.set(null);
    try {
      localStorage.removeItem(KLJUC_TOKEN);
      localStorage.removeItem(KLJUC_KORISNIK);
    } catch {}
  }

  private sacuvaj(odgovor: AuthResponse): void {
    this.stanje.set(odgovor);
    try {
      localStorage.setItem(KLJUC_TOKEN, odgovor.token);
      localStorage.setItem(KLJUC_KORISNIK, JSON.stringify(odgovor.korisnik));
    } catch {}
  }

  private ucitaj(): AuthResponse | null {
    const token = citaj(KLJUC_TOKEN);
    const korisnik = citaj(KLJUC_KORISNIK);
    if (!token || !korisnik || istekao(token)) {
      return null;
    }
    try {
      const podaci = JSON.parse(korisnik) as Korisnik;
      return podaci.uloga === 'ZAPOSLENI' && podaci.tipZaposlenog === undefined ? null : { token, korisnik: podaci };
    } catch {
      return null;
    }
  }
}
