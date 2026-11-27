import { Component, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { Hotel, Recenzija, Rezervacija, Zaposleni } from '../../../models';
import { DeskService } from '../../../services/desk.service';
import { danas } from '../oznake';
import { Strane } from '../strane/strane';
import { Stranicenje } from '../strane/stranicenje';
import { KorakHotela, NoviHotel } from './novi-hotel/novi-hotel';

@Component({
  selector: 'app-vlasnik',
  imports: [Strane, NoviHotel],
  templateUrl: './vlasnik.html',
  styleUrl: './vlasnik.css'
})
export class Vlasnik {
  private readonly desk = inject(DeskService);

  protected readonly hoteli = signal<Hotel[]>([]);
  protected readonly izabran = signal<Hotel | null>(null);
  protected readonly rezervacije = signal<Rezervacija[]>([]);
  protected readonly recenzije = signal<Recenzija[]>([]);
  protected readonly zaposleni = signal<Zaposleni[]>([]);
  protected readonly ucitavanje = signal(true);
  protected readonly ucitavanjeHotela = signal(false);
  protected readonly greska = signal<string | null>(null);

  protected readonly carobnjak = signal<{ korak: KorakHotela; hotel: Hotel | null } | null>(null);
  protected readonly uspeh = signal<string | null>(null);



  protected readonly menadzeri = computed(() => this.zaposleni().filter((z) => z.tipZaposlenog === 'MENADZER'));

  protected readonly brojke = computed(() => {
    const d = danas();
    const pre30 = new Date();
    pre30.setDate(pre30.getDate() - 30);
    const granica = pre30.toISOString().slice(0, 10);
    const lista = this.rezervacije();
    const odjavljene = lista.filter((r) => r.status === 'ODJAVLJEN' && r.datumOdlaska >= granica && r.datumOdlaska <= d);
    return {
      uHotelu: lista.filter((r) => r.status === 'PRIJAVLJEN').length,
      predstoji: lista.filter((r) => (r.status === 'POTVRDJENA' || r.status === 'KREIRANA') && r.datumDolaska >= d).length,
      prihod: Math.round(odjavljene.reduce((zbir, r) => zbir + (r.ukupnaCena ?? 0), 0)),
      otkazano: lista.filter((r) => r.status === 'OTKAZANA').length
    };
  });

  protected readonly poslednjeRecenzije = computed(() =>
    [...this.recenzije()].sort((a, b) => (b.datum ?? '').localeCompare(a.datum ?? ''))
  );

  protected readonly sRecenzije = new Stranicenje(this.poslednjeRecenzije);

  constructor() {
    this.desk.mojiHoteli().subscribe({
      next: (hoteli) => {
        this.hoteli.set(hoteli);
        this.ucitavanje.set(false);
        if (hoteli.length) {
          this.izaberi(hoteli[0]);
        }
      },
      error: () => {
        this.greska.set('Hoteli ne mogu da se učitaju. Proveri da li je server pokrenut.');
        this.ucitavanje.set(false);
      }
    });
  }

  protected izaberi(hotel: Hotel): void {
    if (hotel.id == null) {
      return;
    }
    this.izabran.set(hotel);
    this.ucitavanjeHotela.set(true);
    forkJoin([this.desk.rezervacijeHotela(hotel.id), this.desk.recenzije(hotel.id), this.desk.zaposleni(hotel.id)]).subscribe({
      next: ([rezervacije, recenzije, zaposleni]) => {
        this.rezervacije.set(rezervacije);
        this.recenzije.set(recenzije);
        this.zaposleni.set(zaposleni);
        this.sRecenzije.naPocetak();
        this.ucitavanjeHotela.set(false);
      },
      error: () => {
        this.greska.set('Podaci hotela ne mogu da se učitaju.');
        this.ucitavanjeHotela.set(false);
      }
    });
  }








  protected otvoriNoviHotel(): void {
    this.uspeh.set(null);
    this.carobnjak.set({ korak: 'hotel', hotel: null });
    setTimeout(() => document.getElementById('forma-vlasnika')?.scrollIntoView({ behavior: 'smooth', block: 'start' }));
  }

  protected dodajMenadzera(hotel: Hotel): void {
    this.uspeh.set(null);
    this.carobnjak.set({ korak: 'menadzer', hotel });
    setTimeout(() => document.getElementById('forma-vlasnika')?.scrollIntoView({ behavior: 'smooth', block: 'start' }));
  }

  protected hotelNapravljen(hotel: Hotel): void {
    this.hoteli.update((lista) => [...lista, hotel]);
    this.izabran.set(hotel);
  }

  protected menadzerNapravljen(menadzer: Zaposleni): void {
    const hotel = this.carobnjak()?.hotel ?? this.izabran();
    this.carobnjak.set(null);
    this.uspeh.set(`${menadzer.ime} ${menadzer.prezime} je menadžer hotela ${hotel?.naziv ?? ''} i može da se prijavi.`);
    if (hotel) {
      this.izaberi(hotel);
    }
  }

  protected zatvoriCarobnjak(): void {
    const hotel = this.izabran();
    this.carobnjak.set(null);
    if (hotel) {
      this.izaberi(hotel);
    }
  }
}
