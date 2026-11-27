import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Drzava, Grad, Hotel, TipSobe, Zaposleni } from '../../../../models';
import { DeskService } from '../../../../services/desk.service';

export type KorakHotela = 'hotel' | 'sobe' | 'menadzer';

interface NovaSoba {
  kljuc: number;
  brojSobe: number | null;
  tipSobeId: number | null;
  cenaPoNoci: number | null;
}

interface Sprat {
  broj: number;
  sobe: NovaSoba[];
}

const NOVA_VRSTA = 'nova';

function poruka(e: HttpErrorResponse, podrazumevana: string): string {
  if (e.status === 400 && e.error?.errors && Object.keys(e.error.errors).length) {
    return Object.values(e.error.errors as Record<string, string>).join(' ');
  }
  return e.error?.message ?? podrazumevana;
}

function brojSoba(n: number): string {
  const d = n % 10;
  const s = n % 100;
  if (d >= 2 && d <= 4 && (s < 12 || s > 14)) {
    return `${n} sobe`;
  }
  return `${n} soba`;
}

function naSpratova(n: number): string {
  const d = n % 10;
  const s = n % 100;
  if (d === 1 && s !== 11) {
    return `na ${n} spratu`;
  }
  if (d >= 2 && d <= 4 && (s < 12 || s > 14)) {
    return `na ${n} sprata`;
  }
  return `na ${n} spratova`;
}

@Component({
  selector: 'app-novi-hotel',
  imports: [ReactiveFormsModule],
  templateUrl: './novi-hotel.html',
  styleUrl: './novi-hotel.css'
})
export class NoviHotel implements OnInit {
  private readonly desk = inject(DeskService);
  private readonly fb = inject(FormBuilder).nonNullable;
  private sledeciKljuc = 1;

  readonly pocetniKorak = input<KorakHotela>('hotel');
  readonly postojeciHotel = input<Hotel | null>(null);

  readonly hotelNapravljen = output<Hotel>();
  readonly menadzerNapravljen = output<Zaposleni>();
  readonly zatvori = output<void>();

  protected readonly novaVrsta = NOVA_VRSTA;
  protected readonly brojSoba = brojSoba;
  protected readonly naSpratova = naSpratova;
  protected readonly korak = signal<KorakHotela>('hotel');
  protected readonly hotel = signal<Hotel | null>(null);
  protected readonly gradovi = signal<Grad[]>([]);
  protected readonly drzave = signal<Drzava[]>([]);
  protected readonly tipovi = signal<TipSobe[]>([]);
  protected readonly poslato = signal(false);
  protected readonly cuva = signal(false);
  protected readonly greska = signal<string | null>(null);

  protected readonly noviGradOtvoren = signal(false);
  protected readonly greskaGrada = signal<string | null>(null);
  protected readonly noviTipZa = signal<number | null>(null);
  protected readonly greskaTipa = signal<string | null>(null);

  protected readonly spratovi = signal<Sprat[]>([]);

  protected readonly hotelForma = this.fb.group({
    naziv: ['', [Validators.required, Validators.pattern(/\S/)]],
    brojZvezdica: [3, [Validators.required, Validators.min(1), Validators.max(5)]],
    opis: ['', Validators.maxLength(2000)],
    email: ['', Validators.email],
    brojTelefona: [''],
    ulica: ['', [Validators.required, Validators.pattern(/\S/)]],
    broj: ['', [Validators.required, Validators.pattern(/\S/)]],
    gradId: ['', Validators.required]
  });

  protected readonly gradForma = this.fb.group({
    naziv: ['', [Validators.required, Validators.pattern(/\S/)]],
    postanskiBroj: [''],
    drzavaId: ['', Validators.required]
  });

  protected readonly tipForma = this.fb.group({
    tip: ['', [Validators.required, Validators.pattern(/\S/)]],
    maxGostiju: [2, [Validators.required, Validators.min(1)]],
    opis: ['']
  });

  protected readonly menadzerForma = this.fb.group({
    ime: ['', [Validators.required, Validators.pattern(/\S/)]],
    prezime: ['', [Validators.required, Validators.pattern(/\S/)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    brojTelefona: ['']
  });

  protected readonly ukupnoSoba = computed(() => this.spratovi().reduce((zbir, s) => zbir + s.sobe.length, 0));

  ngOnInit(): void {
    this.korak.set(this.pocetniKorak());
    this.hotel.set(this.postojeciHotel());
    forkJoin([this.desk.gradovi(), this.desk.drzave(), this.desk.tipoviSoba()]).subscribe({
      next: ([gradovi, drzave, tipovi]) => {
        this.gradovi.set(this.sortirajGradove(gradovi));
        this.drzave.set(drzave.sort((a, b) => a.naziv.localeCompare(b.naziv)));
        this.tipovi.set(tipovi.sort((a, b) => a.maxGostiju - b.maxGostiju));
      },
      error: () => this.greska.set('Gradovi i vrste soba ne mogu da se učitaju.')
    });
  }

  protected neispravno(forma: 'hotel' | 'grad' | 'tip' | 'menadzer', polje: string): boolean {
    const kontrola =
      forma === 'hotel'
        ? this.hotelForma.get(polje)
        : forma === 'grad'
          ? this.gradForma.get(polje)
          : forma === 'tip'
            ? this.tipForma.get(polje)
            : this.menadzerForma.get(polje);
    return this.poslato() && !!kontrola?.invalid;
  }

  protected izborGrada(vrednost: string): void {
    if (vrednost === NOVA_VRSTA) {
      this.hotelForma.controls.gradId.setValue('');
      this.gradForma.reset();
      this.greskaGrada.set(null);
      this.noviGradOtvoren.set(true);
    }
  }

  protected sacuvajGrad(): void {
    this.greskaGrada.set(null);
    if (this.gradForma.invalid) {
      this.greskaGrada.set('Upiši naziv grada i izaberi državu.');
      return;
    }
    const v = this.gradForma.getRawValue();
    this.desk
      .kreirajGrad({ naziv: v.naziv.trim(), postanskiBroj: v.postanskiBroj.trim() || undefined, drzavaId: Number(v.drzavaId) })
      .subscribe({
        next: (grad) => {
          this.gradovi.update((lista) => this.sortirajGradove([...lista, grad]));
          this.hotelForma.controls.gradId.setValue(String(grad.id));
          this.noviGradOtvoren.set(false);
        },
        error: (e: HttpErrorResponse) => this.greskaGrada.set(poruka(e, 'Grad nije dodat.'))
      });
  }

  protected sacuvajHotel(): void {
    this.poslato.set(true);
    this.greska.set(null);
    if (this.hotelForma.invalid) {
      return;
    }
    const v = this.hotelForma.getRawValue();
    this.cuva.set(true);
    this.desk
      .kreirajHotel({
        naziv: v.naziv.trim(),
        brojZvezdica: v.brojZvezdica,
        opis: v.opis.trim() || undefined,
        email: v.email.trim() || undefined,
        brojTelefona: v.brojTelefona.trim() || undefined,
        adresa: { ulica: v.ulica.trim(), broj: v.broj.trim(), gradId: Number(v.gradId) }
      })
      .subscribe({
        next: (hotel) => {
          this.cuva.set(false);
          this.hotel.set(hotel);
          this.hotelNapravljen.emit(hotel);
          this.spratovi.set([]);
          this.dodajSprat();
          this.idiNa('sobe');
        },
        error: (e: HttpErrorResponse) => {
          this.cuva.set(false);
          this.greska.set(poruka(e, 'Hotel nije napravljen. Pokušaj ponovo.'));
        }
      });
  }

  protected dodajSprat(): void {
    const sledeci = this.spratovi().reduce((max, s) => Math.max(max, s.broj), 0) + 1;
    this.spratovi.update((lista) => [...lista, { broj: sledeci, sobe: [] }]);
    this.dodajSobu(sledeci);
  }

  protected ukloniSprat(broj: number): void {
    this.spratovi.update((lista) => lista.filter((s) => s.broj !== broj));
  }

  protected dodajSobu(sprat: number): void {
    const sve = this.spratovi().flatMap((s) => s.sobe);
    const poslednja = sve[sve.length - 1];
    this.spratovi.update((lista) =>
      lista.map((s) => {
        if (s.broj !== sprat) {
          return s;
        }
        const zauzeti = new Set(sve.map((x) => x.brojSobe));
        let broj = sprat * 100 + s.sobe.length + 1;
        while (zauzeti.has(broj)) {
          broj++;
        }
        const soba: NovaSoba = {
          kljuc: this.sledeciKljuc++,
          brojSobe: broj,
          tipSobeId: poslednja?.tipSobeId ?? this.tipovi()[0]?.id ?? null,
          cenaPoNoci: poslednja?.cenaPoNoci ?? null
        };
        return { ...s, sobe: [...s.sobe, soba] };
      })
    );
  }

  protected ukloniSobu(sprat: number, kljuc: number): void {
    this.spratovi.update((lista) => lista.map((s) => (s.broj === sprat ? { ...s, sobe: s.sobe.filter((x) => x.kljuc !== kljuc) } : s)));
  }

  protected izmeniSobu(sprat: number, kljuc: number, polje: 'brojSobe' | 'cenaPoNoci', vrednost: string): void {
    const broj = vrednost === '' ? null : Number(vrednost);
    this.spratovi.update((lista) =>
      lista.map((s) => (s.broj === sprat ? { ...s, sobe: s.sobe.map((x) => (x.kljuc === kljuc ? { ...x, [polje]: broj } : x)) } : s))
    );
  }

  protected izborTipa(sprat: number, kljuc: number, vrednost: string): void {
    if (vrednost === NOVA_VRSTA) {
      this.tipForma.reset();
      this.greskaTipa.set(null);
      this.noviTipZa.set(kljuc);
      return;
    }
    this.postaviTip(sprat, kljuc, Number(vrednost));
  }

  protected sacuvajTip(sprat: number, kljuc: number): void {
    this.greskaTipa.set(null);
    if (this.tipForma.invalid) {
      this.greskaTipa.set('Upiši naziv vrste i koliko gostiju prima.');
      return;
    }
    const v = this.tipForma.getRawValue();
    this.desk.kreirajTipSobe({ tip: v.tip.trim(), maxGostiju: v.maxGostiju, opis: v.opis.trim() || undefined }).subscribe({
      next: (tip) => {
        this.tipovi.update((lista) => [...lista, tip].sort((a, b) => a.maxGostiju - b.maxGostiju));
        this.postaviTip(sprat, kljuc, tip.id ?? null);
        this.noviTipZa.set(null);
      },
      error: (e: HttpErrorResponse) => this.greskaTipa.set(poruka(e, 'Vrsta sobe nije dodata.'))
    });
  }

  protected otkaziTip(): void {
    this.noviTipZa.set(null);
  }

  protected sacuvajSobe(): void {
    const hotel = this.hotel();
    this.greska.set(null);
    if (hotel?.id == null) {
      return;
    }
    const sobe = this.spratovi().flatMap((s) => s.sobe.map((x) => ({ ...x, sprat: s.broj })));
    if (!sobe.length) {
      this.greska.set('Dodaj bar jednu sobu.');
      return;
    }
    const neispravna = sobe.find((x) => !x.brojSobe || x.brojSobe < 1 || !x.tipSobeId || !x.cenaPoNoci || x.cenaPoNoci <= 0);
    if (neispravna) {
      this.greska.set(`Proveri sobu na ${neispravna.sprat}. spratu: treba joj broj, vrsta i cena veća od nule.`);
      return;
    }
    const brojevi = sobe.map((x) => x.brojSobe);
    const dupla = brojevi.find((b, i) => brojevi.indexOf(b) !== i);
    if (dupla) {
      this.greska.set(`Broj sobe ${dupla} je unet dva puta.`);
      return;
    }
    this.cuva.set(true);
    this.desk
      .kreirajSobe(
        hotel.id,
        sobe.map((x) => ({ brojSobe: x.brojSobe!, sprat: x.sprat, cenaPoNoci: x.cenaPoNoci!, tipSobeId: x.tipSobeId!, hotelId: hotel.id! }))
      )
      .subscribe({
        next: () => {
          this.cuva.set(false);
          this.idiNa('menadzer');
        },
        error: (e: HttpErrorResponse) => {
          this.cuva.set(false);
          this.greska.set(poruka(e, 'Sobe nisu sačuvane. Pokušaj ponovo.'));
        }
      });
  }

  protected sacuvajMenadzera(): void {
    const hotel = this.hotel();
    this.poslato.set(true);
    this.greska.set(null);
    if (this.menadzerForma.invalid || hotel?.id == null) {
      return;
    }
    const v = this.menadzerForma.getRawValue();
    this.cuva.set(true);
    this.desk
      .kreirajZaposlenog({
        ime: v.ime.trim(),
        prezime: v.prezime.trim(),
        email: v.email.trim(),
        password: v.password,
        brojTelefona: v.brojTelefona.trim() || undefined,
        tipZaposlenog: 'MENADZER',
        hotelId: hotel.id
      })
      .subscribe({
        next: (menadzer) => {
          this.cuva.set(false);
          this.menadzerNapravljen.emit(menadzer);
        },
        error: (e: HttpErrorResponse) => {
          this.cuva.set(false);
          this.greska.set(poruka(e, 'Menadžer nije napravljen. Pokušaj ponovo.'));
        }
      });
  }

  private idiNa(korak: KorakHotela): void {
    this.poslato.set(false);
    this.greska.set(null);
    this.korak.set(korak);
    setTimeout(() => document.getElementById('forma-vlasnika')?.scrollIntoView({ behavior: 'smooth', block: 'start' }));
  }

  private postaviTip(sprat: number, kljuc: number, tipSobeId: number | null): void {
    this.spratovi.update((lista) =>
      lista.map((s) => (s.broj === sprat ? { ...s, sobe: s.sobe.map((x) => (x.kljuc === kljuc ? { ...x, tipSobeId } : x)) } : s))
    );
  }

  private sortirajGradove(lista: Grad[]): Grad[] {
    return lista.sort((a, b) => (a.drzavaNaziv ?? '').localeCompare(b.drzavaNaziv ?? '') || a.naziv.localeCompare(b.naziv));
  }
}
