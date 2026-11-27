import { Component, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { Rezervacija, StatusRezervacije } from '../../../models';
import { AuthService } from '../../../services/auth.service';
import { DeskService } from '../../../services/desk.service';
import { Strane } from '../strane/strane';
import { Stranicenje } from '../strane/stranicenje';
import { STATUS_REZERVACIJE, brojNoci, danas } from '../oznake';

interface Akcija {
  naziv: string;
  status: StatusRezervacije;
  glavna: boolean;
}

@Component({
  selector: 'app-recepcija',
  imports: [Strane],
  templateUrl: './recepcija.html'
})
export class Recepcija {
  private readonly desk = inject(DeskService);
  private readonly hotelId = inject(AuthService).korisnik()?.hotelId ?? null;

  protected readonly status = STATUS_REZERVACIJE;
  protected readonly brojNoci = brojNoci;
  protected readonly datum = signal(danas());
  protected readonly dolasci = signal<Rezervacija[]>([]);
  protected readonly odlasci = signal<Rezervacija[]>([]);
  protected readonly ucitavanje = signal(true);
  protected readonly greska = signal<string | null>(null);
  protected readonly radi = signal<number | null>(null);

  protected readonly sDolasci = new Stranicenje(this.dolasci);
  protected readonly sOdlasci = new Stranicenje(this.odlasci);

  protected readonly brojke = computed(() => ({
    dolazaka: this.dolasci().filter((r) => r.status !== 'OTKAZANA').length,
    prijavljeno: this.dolasci().filter((r) => r.status === 'PRIJAVLJEN').length,
    odlazaka: this.odlasci().filter((r) => r.status !== 'OTKAZANA').length,
    odjavljeno: this.odlasci().filter((r) => r.status === 'ODJAVLJEN').length
  }));

  constructor() {
    this.ucitaj();
  }

  protected promeniDatum(vrednost: string): void {
    if (vrednost) {
      this.datum.set(vrednost);
      this.ucitaj();
    }
  }

  protected akcijaDolaska(r: Rezervacija): Akcija | null {
    if (r.status === 'KREIRANA') {
      return { naziv: 'Potvrdi', status: 'POTVRDJENA', glavna: false };
    }
    if (r.status === 'POTVRDJENA') {
      return { naziv: 'Prijavi gosta', status: 'PRIJAVLJEN', glavna: true };
    }
    return null;
  }

  protected akcijaOdlaska(r: Rezervacija): Akcija | null {
    return r.status === 'PRIJAVLJEN' ? { naziv: 'Odjavi gosta', status: 'ODJAVLJEN', glavna: true } : null;
  }

  protected izvrsi(r: Rezervacija, akcija: Akcija): void {
    if (r.id == null) {
      return;
    }
    this.radi.set(r.id);
    this.greska.set(null);
    this.desk.promeniStatusRezervacije(r.id, akcija.status).subscribe({
      next: () => {
        this.radi.set(null);
        this.ucitaj();
      },
      error: (e) => {
        this.radi.set(null);
        this.greska.set(e.error?.message ?? 'Promena nije uspela. Pokušaj ponovo.');
      }
    });
  }

  private ucitaj(): void {
    if (this.hotelId == null) {
      this.greska.set('Nalog nije povezan sa hotelom.');
      this.ucitavanje.set(false);
      return;
    }
    this.ucitavanje.set(true);
    forkJoin([this.desk.dolasci(this.hotelId, this.datum()), this.desk.odlasci(this.hotelId, this.datum())]).subscribe({
      next: ([dolasci, odlasci]) => {
        this.dolasci.set(dolasci);
        this.sDolasci.naPocetak();
        this.sOdlasci.naPocetak();
        this.odlasci.set(odlasci);
        this.ucitavanje.set(false);
      },
      error: () => {
        this.greska.set('Rezervacije ne mogu da se učitaju. Proveri da li je server pokrenut.');
        this.ucitavanje.set(false);
      }
    });
  }
}
