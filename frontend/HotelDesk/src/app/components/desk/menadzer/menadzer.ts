import { Component, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { Soba, Tiket, Zaposleni } from '../../../models';
import { AuthService } from '../../../services/auth.service';
import { DeskService } from '../../../services/desk.service';
import { Strane } from '../strane/strane';
import { Stranicenje } from '../strane/stranicenje';
import { STATUS_SOBE, STATUS_TIKETA, TIP_TIKETA, TIP_ZAPOSLENOG } from '../oznake';

@Component({
  selector: 'app-menadzer',
  imports: [Strane],
  templateUrl: './menadzer.html'
})
export class Menadzer {
  private readonly desk = inject(DeskService);
  private readonly hotelId = inject(AuthService).korisnik()?.hotelId ?? null;

  protected readonly statusSobe = STATUS_SOBE;
  protected readonly statusTiketa = STATUS_TIKETA;
  protected readonly tipTiketa = TIP_TIKETA;
  protected readonly tipZaposlenog = TIP_ZAPOSLENOG;
  protected readonly sobe = signal<Soba[]>([]);
  protected readonly tiketi = signal<Tiket[]>([]);
  protected readonly zaposleni = signal<Zaposleni[]>([]);
  protected readonly ucitavanje = signal(true);
  protected readonly greska = signal<string | null>(null);
  protected readonly radi = signal<number | null>(null);

  protected readonly otvoreniTiketi = computed(() =>
    this.tiketi()
      .filter((t) => t.status !== 'ZATVOREN')
      .sort((a, b) => (a.datumKreiranja ?? '').localeCompare(b.datumKreiranja ?? ''))
  );

  protected readonly sTiketi = new Stranicenje(this.otvoreniTiketi);
  protected readonly sSobe = new Stranicenje(this.sobe);
  protected readonly sZaposleni = new Stranicenje(this.zaposleni);

  protected readonly brojke = computed(() => {
    const sobe = this.sobe();
    return {
      sobe: sobe.length,
      cistih: sobe.filter((s) => s.status === 'CISTA').length,
      prljavih: sobe.filter((s) => s.status === 'PRLJAVA').length,
      odrzavanje: sobe.filter((s) => s.status === 'U_ODRZAVANJU').length,
      nedodeljenih: this.otvoreniTiketi().filter((t) => t.dodeljenId == null).length
    };
  });

  constructor() {
    this.ucitaj();
  }

  protected dodeli(tiket: Tiket, vrednost: string): void {
    if (tiket.id == null || !vrednost) {
      return;
    }
    this.radi.set(tiket.id);
    this.greska.set(null);
    this.desk.azurirajTiket(tiket.id, { dodeljenId: Number(vrednost) }).subscribe({
      next: (izmenjen) => {
        this.radi.set(null);
        this.tiketi.update((lista) => lista.map((t) => (t.id === izmenjen.id ? izmenjen : t)));
      },
      error: (e) => {
        this.radi.set(null);
        this.greska.set(e.error?.message ?? 'Dodela nije uspela. Pokušaj ponovo.');
      }
    });
  }

  private ucitaj(): void {
    if (this.hotelId == null) {
      this.greska.set('Nalog nije povezan sa hotelom.');
      this.ucitavanje.set(false);
      return;
    }
    forkJoin([this.desk.sobe(this.hotelId), this.desk.tiketiHotela(this.hotelId), this.desk.zaposleni(this.hotelId)]).subscribe({
      next: ([sobe, tiketi, zaposleni]) => {
        this.sobe.set(sobe.sort((a, b) => a.brojSobe - b.brojSobe));
        this.tiketi.set(tiketi);
        this.zaposleni.set(zaposleni.sort((a, b) => a.tipZaposlenog.localeCompare(b.tipZaposlenog) || a.prezime.localeCompare(b.prezime)));
        this.ucitavanje.set(false);
      },
      error: () => {
        this.greska.set('Podaci hotela ne mogu da se učitaju. Proveri da li je server pokrenut.');
        this.ucitavanje.set(false);
      }
    });
  }
}
