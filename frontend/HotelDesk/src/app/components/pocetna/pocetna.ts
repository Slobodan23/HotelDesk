import { Component, DestroyRef, afterNextRender, computed, inject, signal } from '@angular/core';
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
  slika: Slika;
}

interface Slika {
  src: string;
  srcset: string;
  alt: string;
}

const PO_STRANI = 6;

const SLIKE_GRADOVA: Record<string, Slika> = {
  Beograd: {
    src: 'assets/img/photo-f110403744-960.jpg',
    srcset: 'assets/img/photo-f110403744-500.jpg 500w, assets/img/photo-f110403744-960.jpg 960w',
    alt: 'Kalemegdanska tvrđava i ušće Save u Dunav pri zalasku sunca'
  },
  'Novi Sad': {
    src: 'assets/img/photo-f51508079-960.jpg',
    srcset: 'assets/img/photo-f51508079-500.jpg 500w, assets/img/photo-f51508079-960.jpg 960w',
    alt: 'Petrovaradinska tvrđava iznad Dunava'
  },
  Niš: {
    src: 'assets/img/photo-f35650253-960.jpg',
    srcset: 'assets/img/photo-f35650253-500.jpg 500w, assets/img/photo-f35650253-960.jpg 960w',
    alt: 'Kamena kapija Niške tvrđave'
  },
  Zlatibor: {
    src: 'assets/img/photo-f195715241-960.jpg',
    srcset: 'assets/img/photo-f195715241-500.jpg 500w, assets/img/photo-f195715241-960.jpg 960w',
    alt: 'Pašnjaci i brda Zlatibora sa kućom od drveta'
  },
  Kopaonik: {
    src: 'assets/img/photo-f157666886-960.jpg',
    srcset: 'assets/img/photo-f157666886-500.jpg 500w, assets/img/photo-f157666886-960.jpg 960w',
    alt: 'Šumoviti vrhovi Kopaonika'
  },
  Budva: {
    src: 'assets/img/photo-f106409013-960.jpg',
    srcset: 'assets/img/photo-f106409013-500.jpg 500w, assets/img/photo-f106409013-960.jpg 960w',
    alt: 'Šetalište uz more u Budvi i ostrvo Sveti Nikola'
  },
  Kotor: {
    src: 'assets/img/photo-f22143533-960.jpg',
    srcset: 'assets/img/photo-f22143533-500.jpg 500w, assets/img/photo-f22143533-960.jpg 960w',
    alt: 'Kameni zvonik u starom gradu Kotoru i zidine na brdu'
  },
  Dubrovnik: {
    src: 'assets/img/photo-f79842579-960.jpg',
    srcset: 'assets/img/photo-f79842579-500.jpg 500w, assets/img/photo-f79842579-960.jpg 960w',
    alt: 'Crveni krovovi starog grada Dubrovnika'
  }
};

const SKRIPTE = ['assets/system.js', 'assets/motion/motion.js', 'assets/layout/layout.js'];

function uIsoDatum(datum: Date): string {
  const m = String(datum.getMonth() + 1).padStart(2, '0');
  const d = String(datum.getDate()).padStart(2, '0');
  return `${datum.getFullYear()}-${m}-${d}`;
}

function dodajDane(iso: string, dani: number): string {
  const [g, m, d] = iso.split('-').map(Number);
  return uIsoDatum(new Date(g, m - 1, d + dani));
}

function brojHotela(n: number): string {
  return n % 10 === 1 && n % 100 !== 11 ? `${n} hotel` : `${n} hotela`;
}

function brojGradova(n: number): string {
  if (n % 10 === 1 && n % 100 !== 11) {
    return `${n} gradu`;
  }
  return `${n} gradova`;
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
  protected readonly brojHotela = brojHotela;

  protected readonly forma = inject(FormBuilder).nonNullable.group({
    gradId: '',
    naziv: '',
    dolazak: '',
    odlazak: '',
    gosti: 2,
    minZvezdica: ''
  });

  protected readonly gradovi = signal<Grad[]>([]);
  protected readonly sviHoteli = signal<Hotel[]>([]);
  protected readonly hoteli = signal<Hotel[]>([]);
  protected readonly ucitavanje = signal(true);
  protected readonly greska = signal<string | null>(null);
  protected readonly greskaForme = signal<string | null>(null);
  protected readonly sortiranje = signal<Sortiranje>('preporuka');
  protected readonly strana = signal(0);
  protected readonly upit = signal<Record<string, string>>({});

  protected readonly destinacije = computed<Destinacija[]>(() => {
    const mapa = new Map<number, Destinacija>();
    for (const h of this.sviHoteli()) {
      const a = h.adresa;
      const slika = SLIKE_GRADOVA[a.gradNaziv ?? ''];
      if (!slika) {
        continue;
      }
      const postojeca = mapa.get(a.gradId);
      if (postojeca) {
        postojeca.brojHotela++;
      } else {
        mapa.set(a.gradId, {
          gradId: a.gradId,
          naziv: a.gradNaziv ?? '',
          drzava: a.drzavaNaziv ?? '',
          brojHotela: 1,
          slika
        });
      }
    }
    return [...mapa.values()].sort((a, b) => b.brojHotela - a.brojHotela || a.naziv.localeCompare(b.naziv)).slice(0, 8);
  });

  protected readonly gradoviSaHotelima = computed(() => {
    const ids = new Set(this.sviHoteli().map((h) => h.adresa.gradId));
    return this.gradovi()
      .filter((g) => g.id != null && ids.has(g.id))
      .sort((a, b) => a.naziv.localeCompare(b.naziv));
  });

  protected readonly najbolji = computed(() => {
    const ocenjeni = this.sviHoteli().filter((h) => h.prosecnaOcena != null);
    return ocenjeni.sort((a, b) => (b.prosecnaOcena ?? 0) - (a.prosecnaOcena ?? 0))[0] ?? null;
  });

  protected readonly najava = computed(() => {
    const hoteli = this.sviHoteli();
    if (!hoteli.length) {
      return 'Srbija, Crna Gora i Hrvatska';
    }
    const gradova = new Set(hoteli.map((h) => h.adresa.gradId)).size;
    return `${brojHotela(hoteli.length)} u ${brojGradova(gradova)}`;
  });

  protected readonly sortirani = computed(() => {
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

  protected readonly brojStrana = computed(() => Math.max(1, Math.ceil(this.hoteli().length / PO_STRANI)));

  protected readonly prikazani = computed(() => {
    const od = this.strana() * PO_STRANI;
    return this.sortirani().slice(od, od + PO_STRANI);
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

    afterNextRender(() => this.ucitajSkripte());
  }

  protected promenaDolaska(): void {
    const { dolazak, odlazak } = this.forma.getRawValue();
    if (dolazak && (!odlazak || odlazak <= dolazak)) {
      this.forma.controls.odlazak.setValue(dodajDane(dolazak, 1));
    }
  }

  protected minOdlazak(): string {
    const dolazak = this.forma.controls.dolazak.value;
    return dodajDane(dolazak || this.danas, 1);
  }

  protected pretrazi(): void {
    const { gradId, naziv, dolazak, odlazak, gosti, minZvezdica } = this.forma.getRawValue();
    if ((dolazak && !odlazak) || (!dolazak && odlazak)) {
      this.greskaForme.set('Upiši i dan dolaska i dan odlaska.');
      return;
    }
    if (dolazak && dolazak < this.danas) {
      this.greskaForme.set('Dan dolaska ne može biti u prošlosti.');
      return;
    }
    if (dolazak && odlazak <= dolazak) {
      this.greskaForme.set('Dan odlaska mora biti posle dana dolaska.');
      return;
    }
    if (!gosti || gosti < 1 || gosti > 8) {
      this.greskaForme.set('Upiši broj gostiju od 1 do 8.');
      return;
    }
    this.greskaForme.set(null);
    this.idiNa({
      gradId: gradId || null,
      naziv: naziv.trim() || null,
      dolazak: dolazak || null,
      odlazak: odlazak || null,
      gosti: dolazak ? gosti : null,
      minZvezdica: minZvezdica || null
    });
  }

  protected izaberiDestinaciju(d: Destinacija): void {
    this.forma.controls.gradId.setValue(String(d.gradId));
    this.idiNa({ gradId: d.gradId, naziv: null });
  }

  protected promeniSortiranje(vrednost: string): void {
    this.sortiranje.set(vrednost as Sortiranje);
    this.strana.set(0);
  }

  protected idiNaStranu(korak: number): void {
    this.strana.set(Math.min(this.brojStrana() - 1, Math.max(0, this.strana() + korak)));
  }

  protected ponisti(): void {
    this.forma.reset({ gradId: '', naziv: '', dolazak: '', odlazak: '', gosti: 2, minZvezdica: '' });
    this.greskaForme.set(null);
    this.router.navigate([], { queryParams: {} });
  }

  protected ponovo(): void {
    this.primeniUpit(this.route.snapshot.queryParamMap);
  }

  private idiNa(promene: Record<string, string | number | null>): void {
    this.router
      .navigate([], { queryParams: promene, queryParamsHandling: 'merge', preserveFragment: false })
      .then(() => document.getElementById('pretraga')?.scrollIntoView());
  }

  private primeniUpit(p: ParamMap): void {
    const gradId = p.get('gradId') ?? '';
    const naziv = p.get('naziv') ?? '';
    const dolazak = p.get('dolazak') ?? '';
    const odlazak = p.get('odlazak') ?? '';
    const gosti = Number(p.get('gosti')) || 2;
    const minZvezdica = p.get('minZvezdica') ?? '';

    this.forma.patchValue({ gradId, naziv, dolazak, odlazak, gosti, minZvezdica });

    const upit: Record<string, string> = {};
    if (dolazak && odlazak) {
      upit['dolazak'] = dolazak;
      upit['odlazak'] = odlazak;
      upit['gosti'] = String(gosti);
    }
    this.upit.set(upit);
    this.strana.set(0);

    this.ucitavanje.set(true);
    this.greska.set(null);
    this.hotelService
      .pretraga({ gradId: gradId ? Number(gradId) : null, naziv, minZvezdica: minZvezdica ? Number(minZvezdica) : null })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (h) => {
          this.hoteli.set(h);
          this.ucitavanje.set(false);
        },
        error: () => {
          this.hoteli.set([]);
          this.greska.set('Hoteli trenutno ne mogu da se učitaju. Proveri da li je server pokrenut.');
          this.ucitavanje.set(false);
        }
      });
  }

  private ucitajSkripte(): void {
    for (const src of SKRIPTE) {
      if (document.querySelector(`script[src="${src}"]`)) {
        continue;
      }
      const s = document.createElement('script');
      s.type = 'module';
      s.src = src;
      document.body.appendChild(s);
    }
  }
}
