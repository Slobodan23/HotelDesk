import { Component, computed, inject, signal } from '@angular/core';
import { Observable, forkJoin } from 'rxjs';
import { Soba, StatusTiketa, Tiket } from '../../../models';
import { AuthService } from '../../../services/auth.service';
import { DeskService } from '../../../services/desk.service';
import { Strane } from '../strane/strane';
import { Stranicenje } from '../strane/stranicenje';
import { STATUS_TIKETA, TIP_TIKETA } from '../oznake';

@Component({
  selector: 'app-ciscenje',
  imports: [Strane],
  templateUrl: './ciscenje.html'
})
export class Ciscenje {
  private readonly desk = inject(DeskService);
  private readonly hotelId = inject(AuthService).korisnik()?.hotelId ?? null;

  protected readonly statusTiketa = STATUS_TIKETA;
  protected readonly tipTiketa = TIP_TIKETA;
  protected readonly sobe = signal<Soba[]>([]);
  protected readonly tiketi = signal<Tiket[]>([]);
  protected readonly ucitavanje = signal(true);
  protected readonly greska = signal<string | null>(null);
  protected readonly radi = signal<string | null>(null);

  protected readonly zaCiscenje = computed(() =>
    this.sobe()
      .filter((s) => s.status === 'PRLJAVA')
      .sort((a, b) => a.brojSobe - b.brojSobe)
  );

  protected readonly otvoreniTiketi = computed(() => this.tiketi().filter((t) => t.status !== 'ZATVOREN'));

  protected readonly sSobe = new Stranicenje(this.zaCiscenje);
  protected readonly sTiketi = new Stranicenje(this.otvoreniTiketi);

  protected readonly brojke = computed(() => ({
    prljavih: this.zaCiscenje().length,
    cistih: this.sobe().filter((s) => s.status === 'CISTA').length,
    odrzavanje: this.sobe().filter((s) => s.status === 'U_ODRZAVANJU').length,
    tiketa: this.otvoreniTiketi().length
  }));

  constructor() {
    this.ucitaj();
  }

  protected ocisti(soba: Soba): void {
    if (soba.id == null) {
      return;
    }
    this.pokreni(`s${soba.id}`, this.desk.promeniStatusSobe(soba.id, 'CISTA'));
  }

  protected promeniTiket(tiket: Tiket, status: StatusTiketa): void {
    if (tiket.id == null) {
      return;
    }
    this.pokreni(`t${tiket.id}`, this.desk.azurirajTiket(tiket.id, { status }));
  }

  private pokreni(kljuc: string, zahtev: Observable<unknown>): void {
    this.radi.set(kljuc);
    this.greska.set(null);
    zahtev.subscribe({
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
    forkJoin([this.desk.sobe(this.hotelId), this.desk.dodeljeniTiketi()]).subscribe({
      next: ([sobe, tiketi]) => {
        this.sobe.set(sobe);
        this.tiketi.set(tiketi);
        this.ucitavanje.set(false);
      },
      error: () => {
        this.greska.set('Podaci ne mogu da se učitaju. Proveri da li je server pokrenut.');
        this.ucitavanje.set(false);
      }
    });
  }
}
