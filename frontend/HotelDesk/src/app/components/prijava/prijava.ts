import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { pocetnaZa } from '../../guards/uloga';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-prijava',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './prijava.html',
  styleUrl: '../registracija/auth.css'
})
export class Prijava {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly povratak = input<string>();

  protected readonly forma = inject(FormBuilder).nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  protected readonly poslato = signal(false);
  protected readonly salje = signal(false);
  protected readonly greska = signal<string | null>(null);

  protected neispravno(polje: 'email' | 'password'): boolean {
    return this.poslato() && this.forma.controls[polje].invalid;
  }

  protected prijavi(): void {
    this.poslato.set(true);
    this.greska.set(null);
    if (this.forma.invalid) {
      document.getElementById(this.forma.controls.email.invalid ? 'p-email' : 'p-lozinka')?.focus();
      return;
    }
    this.salje.set(true);
    const { email, password } = this.forma.getRawValue();
    this.auth.login({ email: email.trim(), password }).subscribe({
      next: (odgovor) => {
        const cilj = this.povratak();
        this.router.navigateByUrl(cilj && cilj.startsWith('/') && !cilj.startsWith('//') ? cilj : pocetnaZa(odgovor.korisnik));
      },
      error: (e: HttpErrorResponse) => {
        this.salje.set(false);
        this.greska.set(
          e.status === 401 || e.status === 400
            ? 'Email ili lozinka nisu tačni.'
            : 'Server trenutno ne odgovara. Pokušaj ponovo za par minuta.'
        );
      }
    });
  }
}
