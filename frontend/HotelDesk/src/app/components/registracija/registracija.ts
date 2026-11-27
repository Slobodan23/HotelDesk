import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Pol } from '../../models';
import { pocetnaZa } from '../../guards/uloga';
import { AuthService } from '../../services/auth.service';

type Polje = 'ime' | 'prezime' | 'email' | 'password' | 'brojTelefona';

const REDOSLED: Polje[] = ['ime', 'prezime', 'email', 'password', 'brojTelefona'];

@Component({
  selector: 'app-registracija',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registracija.html',
  styleUrl: './auth.css'
})
export class Registracija {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly povratak = input<string>();

  protected readonly forma = inject(FormBuilder).nonNullable.group({
    ime: ['', [Validators.required, Validators.pattern(/\S/)]],
    prezime: ['', [Validators.required, Validators.pattern(/\S/)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    brojTelefona: ['', Validators.pattern(/^[+\d][\d\s/-]{5,}$/)],
    pol: ['' as Pol | '']
  });

  protected readonly poslato = signal(false);
  protected readonly salje = signal(false);
  protected readonly greska = signal<string | null>(null);
  protected readonly zauzetEmail = signal(false);

  protected neispravno(polje: Polje): boolean {
    if (polje === 'email' && this.zauzetEmail()) {
      return true;
    }
    return this.poslato() && this.forma.controls[polje].invalid;
  }

  protected registruj(): void {
    this.poslato.set(true);
    this.greska.set(null);
    this.zauzetEmail.set(false);
    if (this.forma.invalid) {
      const prvo = REDOSLED.find((p) => this.forma.controls[p].invalid);
      document.getElementById(`r-${prvo}`)?.focus();
      return;
    }
    this.salje.set(true);
    const v = this.forma.getRawValue();
    this.auth
      .registracija({
        ime: v.ime.trim(),
        prezime: v.prezime.trim(),
        email: v.email.trim(),
        password: v.password,
        brojTelefona: v.brojTelefona.trim() || undefined,
        pol: v.pol || undefined
      })
      .subscribe({
        next: (odgovor) => {
          const cilj = this.povratak();
          this.router.navigateByUrl(cilj && cilj.startsWith('/') && !cilj.startsWith('//') ? cilj : pocetnaZa(odgovor.korisnik));
        },
        error: (e: HttpErrorResponse) => {
          this.salje.set(false);
          if (e.status === 409) {
            this.zauzetEmail.set(true);
            document.getElementById('r-email')?.focus();
          } else if (e.status === 400) {
            this.greska.set('Neka polja nisu dobro popunjena. Proveri ih i pošalji ponovo.');
          } else {
            this.greska.set('Server trenutno ne odgovara. Pokušaj ponovo za par minuta.');
          }
        }
      });
  }
}
