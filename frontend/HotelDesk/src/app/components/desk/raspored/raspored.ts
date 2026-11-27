import { Component, ElementRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { filter, map, of, startWith, switchMap, catchError } from 'rxjs';
import { AuthService } from '../../../services/auth.service';
import { DeskService } from '../../../services/desk.service';
import { TIP_ZAPOSLENOG, ULOGA } from '../oznake';

export interface Sekcija {
  id: string;
  naziv: string;
}

interface PodaciDeska {
  naslov: string;
  sekcije: Sekcija[];
}

@Component({
  selector: 'app-desk-raspored',
  imports: [RouterOutlet, RouterLink],
  templateUrl: './raspored.html',
  styleUrl: './raspored.css',
  host: {
    '(document:click)': 'klikVan($event)',
    '(document:keydown.escape)': 'meniOtvoren.set(false)'
  }
})
export class DeskRaspored {
  private readonly router = inject(Router);
  private readonly el = inject(ElementRef<HTMLElement>);
  private readonly desk = inject(DeskService);
  protected readonly auth = inject(AuthService);

  protected readonly meniOtvoren = signal(false);

  protected readonly podaci = toSignal(
    this.router.events.pipe(
      filter((e) => e instanceof NavigationEnd),
      startWith(null),
      map(() => {
        let r = this.router.routerState.snapshot.root;
        while (r.firstChild) {
          r = r.firstChild;
        }
        const data = r.data as Partial<PodaciDeska>;
        return { naslov: data.naslov ?? '', sekcije: data.sekcije ?? [] };
      })
    ),
    { initialValue: { naslov: '', sekcije: [] } as PodaciDeska }
  );

  protected readonly hotel = toSignal(
    of(this.auth.korisnik()?.hotelId).pipe(
      switchMap((id) => (id ? this.desk.hotel(id).pipe(catchError(() => of(null))) : of(null)))
    ),
    { initialValue: null }
  );

  protected readonly opisUloge = computed(() => {
    const k = this.auth.korisnik();
    if (!k?.uloga) {
      return '';
    }
    return k.tipZaposlenog ? TIP_ZAPOSLENOG[k.tipZaposlenog] : ULOGA[k.uloga];
  });

  protected readonly inicijali = computed(() => {
    const k = this.auth.korisnik();
    return k ? `${k.ime.charAt(0)}${k.prezime.charAt(0)}`.toUpperCase() : '';
  });

  constructor() {
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd), takeUntilDestroyed()).subscribe(() => this.meniOtvoren.set(false));
  }

  protected idiNa(id: string, dogadjaj: Event): void {
    dogadjaj.preventDefault();
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  protected klikVan(dogadjaj: MouseEvent): void {
    const profil = this.el.nativeElement.querySelector('.desk-profil');
    if (this.meniOtvoren() && profil && !profil.contains(dogadjaj.target as Node)) {
      this.meniOtvoren.set(false);
    }
  }

  protected odjavi(): void {
    this.auth.odjava();
    this.router.navigateByUrl('/prijava');
  }
}
