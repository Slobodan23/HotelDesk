import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Grad, Hotel } from '../../models';
import { GradService } from '../../services/grad.service';
import { HotelService } from '../../services/hotel.service';
import { HotelKartica } from '../hotel-kartica/hotel-kartica';

type Sortiranje = 'preporuka' | 'ocena' | 'zvezdice' | 'naziv';

interface Destinacija {
  gradId: number;
  naziv: string;
  drzava: string;
  brojHotela: number;
}

function uIsoDatum(datum: Date): string {
  const m = String(datum.getMonth() + 1).padStart(2, '0');
  const d = String(datum.getDate()).padStart(2, '0');
  return `${datum.getFullYear()}-${m}-${d}`;
}

function dodajDane(iso: string, dani: number): string {
  const [g, m, d] = iso.split('-').map(Number);
  return uIsoDatum(new Date(g, m - 1, d + dani));
}

@Component({
  selector: 'app-pocetna',
  imports: [ReactiveFormsModule, HotelKartica],
  templateUrl: './pocetna.html',
  styleUrl: './pocetna.css'
})
export class Pocetna {
  private readonly hotelService = inject(HotelService);
  private readonly gradService = inject(GradService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly danas = uIsoDatum(new Date());

  protected readonly forma = inject(FormBuilder).nonNullable.group({
    gradId: '',
    naziv: '',
    dolazak: '',
    odlazak: '',
    gosti: 2
  });

  protected readonly gradovi = signal<Grad[]>([]);
  protected readonly sviHoteli = signal<Hotel[]>([]);
  protected readonly hoteli = signal<Hotel[]>([]);
  protected readonly ucitavanje = signal(true);
  protected readonly greska = signal<string | null>(null);
  protected readonly greskaForme = signal<string | null>(null);
  protected readonly minZvezdica = signal<number | null>(null);
  protected readonly sortiranje = signal<Sortiranje>('preporuka');
  protected readonly upit = signal<Record<string, string>>({});
  protected readonly aktivanGrad = signal<string | null>(null);

  protected readonly zvezdiceOpcije = [3, 4, 5];

  protected readonly destinacije = computed<Destinacija[]>(() => {
    const mapa = new Map<number, Destinacija>();
    for (const h of this.sviHoteli()) {
      const a = h.adresa;
      const postojeca = mapa.get(a.gradId);
      if (postojeca) {
        postojeca.brojHotela++;
      } else {
        mapa.set(a.gradId, {
          gradId: a.gradId,
          naziv: a.gradNaziv ?? '',
          drzava: a.drzavaNaziv ?? '',
          brojHotela: 1
        });
      }
    }
    return [...mapa.values()].sort((a, b) => b.brojHotela - a.brojHotela || a.naziv.localeCompare(b.naziv));
  });

  protected readonly gradoviSaHotelima = computed(() => {
    const ids = new Set(this.sviHoteli().map((h) => h.adresa.gradId));
    return this.gradovi()
      .filter((g) => g.id != null && ids.has(g.id))
      .sort((a, b) => a.naziv.localeCompare(b.naziv));
  });

  protected readonly prikazaniHoteli = computed(() => {
    const lista = [...this.hoteli()];
    const ocena = (h: Hotel) => h.prosecnaOcena ?? 0;
    switch (this.sortiranje()) {
      case 'ocena':
        return lista.sort((a, b) => ocena(b) - ocena(a));
      case 'zvezdice':
        return lista.sort((a, b) => (b.brojZvezdica ?? 0) - (a.brojZvezdica ?? 0) || ocena(b) - ocena(a));
      case 'naziv':
        return lista.sort((a, b) => a.naziv.localeCompare(b.naziv));
      default:
        return lista.sort(
          (a, b) => ocena(b) * 2 + (b.brojZvezdica ?? 0) - (ocena(a) * 2 + (a.brojZvezdica ?? 0))
        );
    }
  });

  protected readonly brojNoci = computed(() => {
    const u = this.upit();
    if (!u['dolazak'] || !u['odlazak']) {
      return null;
    }
    const ms = new Date(u['odlazak']).getTime() - new Date(u['dolazak']).getTime();
    return Math.round(ms / 86400000);
  });

  constructor() {
    this.gradService
      .findAll()
      .pipe(takeUntilDestroyed())
      .subscribe({ next: (g) => this.gradovi.set(g) });

    this.hotelService
      .pretraga()
      .pipe(takeUntilDestroyed())
      .subscribe({ next: (h) => this.sviHoteli.set(h) });

    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe((p) => this.primeniUpit(p));
  }

  protected promenaDolaska(): void {
    const { dolazak, odlazak } = this.forma.getRawValue();
    if (dolazak && (!odlazak || odlazak <= dolazak)) {
      this.forma.controls.odlazak.setValue(dodajDane(dolazak, 1));
    }
  }

  protected minOdlazak(): string {
    const dolazak = this.forma.controls.dolazak.value;
    return dolazak ? dodajDane(dolazak, 1) : dodajDane(this.danas, 1);
  }

  protected promeniGoste(korak: number): void {
    const c = this.forma.controls.gosti;
    c.setValue(Math.min(8, Math.max(1, c.value + korak)));
  }

  protected pretrazi(): void {
    const { gradId, naziv, dolazak, odlazak, gosti } = this.forma.getRawValue();
    if ((dolazak && !odlazak) || (!dolazak && odlazak)) {
      this.greskaForme.set('Izaberite i datum dolaska i datum odlaska.');
      return;
    }
    if (dolazak && dolazak < this.danas) {
      this.greskaForme.set('Datum dolaska ne može biti u prošlosti.');
      return;
    }
    if (dolazak && odlazak <= dolazak) {
      this.greskaForme.set('Datum odlaska mora biti posle datuma dolaska.');
      return;
    }
    this.greskaForme.set(null);
    this.idiNa({
      gradId: gradId || null,
      naziv: naziv.trim() || null,
      dolazak: dolazak || null,
      odlazak: odlazak || null,
      gosti: dolazak ? gosti : null
    });
  }

  protected izaberiDestinaciju(d: Destinacija): void {
    this.forma.controls.gradId.setValue(String(d.gradId));
    this.idiNa({ gradId: d.gradId, naziv: null });
  }

  protected izaberiZvezdice(z: number | null): void {
    this.idiNa({ minZvezdica: this.minZvezdica() === z ? null : z });
  }

  protected promeniSortiranje(vrednost: string): void {
    this.sortiranje.set(vrednost as Sortiranje);
  }

  protected ponisti(): void {
    this.forma.reset({ gradId: '', naziv: '', dolazak: '', odlazak: '', gosti: 2 });
    this.greskaForme.set(null);
    this.router.navigate([], { queryParams: {} });
  }

  protected ponovo(): void {
    this.primeniUpit(this.route.snapshot.queryParamMap);
  }

  private idiNa(promene: Record<string, string | number | null>): void {
    this.router
      .navigate([], { queryParams: promene, queryParamsHandling: 'merge' })
      .then(() => document.getElementById('rezultati')?.scrollIntoView({ behavior: 'smooth' }));
  }

  private primeniUpit(p: ParamMap): void {
    const gradId = p.get('gradId') ?? '';
    const naziv = p.get('naziv') ?? '';
    const dolazak = p.get('dolazak') ?? '';
    const odlazak = p.get('odlazak') ?? '';
    const gosti = Number(p.get('gosti')) || 2;
    const minZvezdica = Number(p.get('minZvezdica')) || null;

    this.forma.patchValue({ gradId, naziv, dolazak, odlazak, gosti });
    this.minZvezdica.set(minZvezdica);

    const upit: Record<string, string> = {};
    if (dolazak && odlazak) {
      upit['dolazak'] = dolazak;
      upit['odlazak'] = odlazak;
      upit['gosti'] = String(gosti);
    }
    this.upit.set(upit);

    this.ucitavanje.set(true);
    this.greska.set(null);
    this.hotelService
      .pretraga({ gradId: gradId ? Number(gradId) : null, naziv, minZvezdica })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (h) => {
          this.hoteli.set(h);
          this.aktivanGrad.set(h[0]?.adresa.gradNaziv && gradId ? h[0].adresa.gradNaziv : this.nazivGrada(gradId));
          this.ucitavanje.set(false);
        },
        error: () => {
          this.hoteli.set([]);
          this.greska.set('Trenutno ne možemo da učitamo hotele. Proverite da li je server pokrenut.');
          this.ucitavanje.set(false);
        }
      });
  }

  private nazivGrada(gradId: string): string | null {
    if (!gradId) {
      return null;
    }
    return this.gradovi().find((g) => String(g.id) === gradId)?.naziv ?? null;
  }
}
