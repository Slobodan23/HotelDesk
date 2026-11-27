import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Grad, Hotel, Korisnik, Pol, TipSobe, TipZaposlenog, Uloga } from '../../../models';
import { AuthService } from '../../../services/auth.service';
import { DeskService } from '../../../services/desk.service';
import { Strane } from '../strane/strane';
import { Stranicenje } from '../strane/stranicenje';
import { TIP_ZAPOSLENOG, ULOGA } from '../oznake';

type PoljeNovog = 'ime' | 'prezime' | 'email' | 'password' | 'tipZaposlenog' | 'hotelId';

function poruka(e: HttpErrorResponse, podrazumevana: string): string {
  if (e.status === 409) {
    return e.error?.message ?? 'Promena nije moguća zbog postojećih podataka.';
  }
  if (e.status === 400 && e.error?.errors && Object.keys(e.error.errors).length) {
    return Object.values(e.error.errors as Record<string, string>).join(' ');
  }
  return e.error?.message ?? podrazumevana;
}

@Component({
  selector: 'app-admin',
  imports: [ReactiveFormsModule, Strane],
  templateUrl: './admin.html',
  styleUrl: './admin.css'
})
export class Admin {
  private readonly desk = inject(DeskService);
  private readonly mojId = inject(AuthService).korisnik()?.id ?? null;

  protected readonly uloga = ULOGA;
  protected readonly tipZaposlenog = TIP_ZAPOSLENOG;
  protected readonly uloge: Uloga[] = ['ADMIN', 'VLASNIK', 'ZAPOSLENI', 'GOST'];
  protected readonly poslovi: TipZaposlenog[] = ['RECEPCIONER', 'CISTAC', 'MENADZER'];
  protected readonly korisnici = signal<Korisnik[]>([]);
  protected readonly hoteli = signal<Hotel[]>([]);
  protected readonly gradovi = signal<Grad[]>([]);
  protected readonly tipovi = signal<TipSobe[]>([]);
  protected readonly ucitavanje = signal(true);
  protected readonly greska = signal<string | null>(null);
  protected readonly filterUloga = signal<Uloga | ''>('');
  protected readonly pretraga = signal('');

  protected readonly noviOtvoren = signal(false);
  protected readonly poslatNovi = signal(false);
  protected readonly cuvaNovi = signal(false);
  protected readonly greskaNovog = signal<string | null>(null);
  protected readonly uspeh = signal<string | null>(null);

  protected readonly novi = inject(FormBuilder).nonNullable.group({
    ime: ['', [Validators.required, Validators.pattern(/\S/)]],
    prezime: ['', [Validators.required, Validators.pattern(/\S/)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    brojTelefona: [''],
    pol: ['' as Pol | ''],
    uloga: ['GOST' as Uloga],
    tipZaposlenog: ['' as TipZaposlenog | ''],
    hotelId: ['']
  });

  protected readonly novaUloga = signal<Uloga>('GOST');

  protected readonly izmenaId = signal<number | null>(null);
  protected readonly brisanjeId = signal<number | null>(null);
  protected readonly brise = signal(false);
  protected readonly greskaBrisanja = signal<string | null>(null);

  protected readonly poUlozi = computed(() => {
    const broj: Record<Uloga, number> = { ADMIN: 0, VLASNIK: 0, ZAPOSLENI: 0, GOST: 0 };
    for (const k of this.korisnici()) {
      if (k.uloga) {
        broj[k.uloga]++;
      }
    }
    return broj;
  });

  protected readonly filtrirani = computed(() => {
    const u = this.filterUloga();
    const tekst = this.pretraga().trim().toLowerCase();
    return this.korisnici().filter(
      (k) => (!u || k.uloga === u) && (!tekst || `${k.ime} ${k.prezime} ${k.email}`.toLowerCase().includes(tekst))
    );
  });

  protected readonly sKorisnici = new Stranicenje(this.filtrirani);
  protected readonly sGradovi = new Stranicenje(this.gradovi);
  protected readonly sTipovi = new Stranicenje(this.tipovi);

  constructor() {
    this.novi.controls.uloga.valueChanges.subscribe((u) => this.podesiZaposlenog(u));
    this.podesiZaposlenog('GOST');
    forkJoin([this.desk.korisnici(), this.desk.sviHoteli(), this.desk.gradovi(), this.desk.tipoviSoba()]).subscribe({
      next: ([korisnici, hoteli, gradovi, tipovi]) => {
        this.korisnici.set(this.sortiraj(korisnici));
        this.hoteli.set(hoteli.sort((a, b) => a.naziv.localeCompare(b.naziv)));
        this.gradovi.set(gradovi.sort((a, b) => (a.drzavaNaziv ?? '').localeCompare(b.drzavaNaziv ?? '') || a.naziv.localeCompare(b.naziv)));
        this.tipovi.set(tipovi.sort((a, b) => a.maxGostiju - b.maxGostiju));
        this.ucitavanje.set(false);
      },
      error: () => {
        this.greska.set('Podaci ne mogu da se učitaju. Proveri da li je server pokrenut.');
        this.ucitavanje.set(false);
      }
    });
  }

  protected jaSam(k: Korisnik): boolean {
    return k.id === this.mojId;
  }

  protected neispravnoNovo(polje: PoljeNovog): boolean {
    return this.poslatNovi() && this.novi.controls[polje].invalid;
  }

  protected otvoriNovi(): void {
    if (this.noviOtvoren() && this.izmenaId() == null) {
      this.zatvoriFormu();
      return;
    }
    this.izmenaId.set(null);
    this.novi.reset();
    this.podesiLozinku(false);
    this.poslatNovi.set(false);
    this.greskaNovog.set(null);
    this.uspeh.set(null);
    this.noviOtvoren.set(true);
  }

  protected izmeni(k: Korisnik): void {
    if (k.id == null) {
      return;
    }
    this.izmenaId.set(k.id);
    this.novi.reset({
      ime: k.ime,
      prezime: k.prezime,
      email: k.email ?? '',
      password: '',
      brojTelefona: k.brojTelefona ?? '',
      pol: k.pol ?? '',
      uloga: k.uloga ?? 'GOST',
      tipZaposlenog: k.tipZaposlenog ?? '',
      hotelId: k.hotelId != null ? String(k.hotelId) : ''
    });
    this.podesiLozinku(true);
    this.poslatNovi.set(false);
    this.greskaNovog.set(null);
    this.uspeh.set(null);
    this.noviOtvoren.set(true);
    setTimeout(() => document.getElementById('novi-korisnik')?.scrollIntoView({ behavior: 'smooth', block: 'start' }));
  }

  protected zatvoriFormu(): void {
    this.noviOtvoren.set(false);
    this.izmenaId.set(null);
    this.greskaNovog.set(null);
  }

  protected sacuvajNovi(): void {
    this.poslatNovi.set(true);
    this.greskaNovog.set(null);
    this.uspeh.set(null);
    if (this.novi.invalid) {
      return;
    }
    const v = this.novi.getRawValue();
    const zaposleni = v.uloga === 'ZAPOSLENI';
    const zajednicko = {
      ime: v.ime.trim(),
      prezime: v.prezime.trim(),
      email: v.email.trim(),
      brojTelefona: v.brojTelefona.trim() || undefined,
      pol: v.pol || undefined,
      uloga: v.uloga,
      tipZaposlenog: zaposleni && v.tipZaposlenog ? v.tipZaposlenog : undefined,
      hotelId: zaposleni && v.hotelId ? Number(v.hotelId) : undefined
    };
    const id = this.izmenaId();
    const zahtev =
      id == null
        ? this.desk.kreirajKorisnika({ ...zajednicko, password: v.password })
        : this.desk.izmeniKorisnika(id, { ...zajednicko, novaLozinka: v.password || undefined });
    this.cuvaNovi.set(true);
    zahtev.subscribe({
      next: (korisnik) => {
        this.cuvaNovi.set(false);
        this.korisnici.update((lista) =>
          this.sortiraj(id == null ? [...lista, korisnik] : lista.map((x) => (x.id === korisnik.id ? korisnik : x)))
        );
        this.uspeh.set(
          id == null
            ? `Nalog za ${korisnik.ime} ${korisnik.prezime} je napravljen.`
            : `Izmene za ${korisnik.ime} ${korisnik.prezime} su sačuvane.`
        );
        this.zatvoriFormu();
        this.novi.reset();
        this.poslatNovi.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.cuvaNovi.set(false);
        this.greskaNovog.set(poruka(e, 'Izmene nisu sačuvane. Pokušaj ponovo.'));
      }
    });
  }

  protected pitajZaBrisanje(k: Korisnik): void {
    this.brisanjeId.set(k.id ?? null);
    this.greskaBrisanja.set(null);
  }

  protected obrisi(k: Korisnik): void {
    if (k.id == null) {
      return;
    }
    this.brise.set(true);
    this.greskaBrisanja.set(null);
    this.desk.obrisiKorisnika(k.id).subscribe({
      next: () => {
        this.brise.set(false);
        this.brisanjeId.set(null);
        this.korisnici.update((lista) => lista.filter((x) => x.id !== k.id));
        if (this.izmenaId() === k.id) {
          this.zatvoriFormu();
        }
        this.uspeh.set(`Nalog ${k.ime} ${k.prezime} je obrisan.`);
      },
      error: (e: HttpErrorResponse) => {
        this.brise.set(false);
        this.greskaBrisanja.set(poruka(e, 'Nalog nije obrisan. Pokušaj ponovo.'));
      }
    });
  }

  protected promeniFilter(vrednost: string): void {
    this.filterUloga.set(vrednost as Uloga | '');
    this.sKorisnici.naPocetak();
  }

  protected promeniPretragu(vrednost: string): void {
    this.pretraga.set(vrednost);
    this.sKorisnici.naPocetak();
  }

  private podesiZaposlenog(uloga: Uloga): void {
    this.novaUloga.set(uloga);
    const { tipZaposlenog, hotelId } = this.novi.controls;
    const validator = uloga === 'ZAPOSLENI' ? [Validators.required] : [];
    tipZaposlenog.setValidators(validator);
    hotelId.setValidators(validator);
    tipZaposlenog.updateValueAndValidity({ emitEvent: false });
    hotelId.updateValueAndValidity({ emitEvent: false });
  }

  private podesiLozinku(izmena: boolean): void {
    const lozinka = this.novi.controls.password;
    lozinka.setValidators(izmena ? [Validators.minLength(8)] : [Validators.required, Validators.minLength(8)]);
    lozinka.updateValueAndValidity({ emitEvent: false });
  }

  private sortiraj(lista: Korisnik[]): Korisnik[] {
    return lista.sort((a, b) => a.prezime.localeCompare(b.prezime) || a.ime.localeCompare(b.ime));
  }
}
