import { Component, ElementRef, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { deskZa, pocetnaZa } from '../../guards/uloga';
import { AuthService } from '../../services/auth.service';

const NAZIV_DESKA = { recepcija: 'FrontDesk', ciscenje: 'StaffDesk', menadzer: 'ManagerDesk', vlasnik: 'OwnerDesk', admin: 'AdminDesk' } as const;

@Component({
  selector: 'app-header',
  imports: [RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.css',
  host: {
    '(document:click)': 'klikVan($event)',
    '(document:keydown.escape)': 'meniOtvoren.set(false)'
  }
})
export class Header {
  private readonly router = inject(Router);
  private readonly el = inject(ElementRef<HTMLElement>);
  protected readonly auth = inject(AuthService);

  protected readonly meniOtvoren = signal(false);

  protected readonly inicijali = computed(() => {
    const k = this.auth.korisnik();
    if (!k) {
      return '';
    }
    return `${k.ime?.trim().charAt(0) ?? ''}${k.prezime?.trim().charAt(0) ?? ''}`.toUpperCase();
  });

  protected readonly desk = computed(() => {
    const d = deskZa(this.auth.korisnik());
    return d ? { url: pocetnaZa(this.auth.korisnik()), naziv: NAZIV_DESKA[d] } : null;
  });

  protected klikVan(dogadjaj: MouseEvent): void {
    const profil = this.el.nativeElement.querySelector('.profil');
    if (this.meniOtvoren() && profil && !profil.contains(dogadjaj.target as Node)) {
      this.meniOtvoren.set(false);
    }
  }

  protected odjavi(): void {
    this.meniOtvoren.set(false);
    this.auth.odjava();
    this.router.navigateByUrl('/');
  }
}
