import { Signal, computed, signal } from '@angular/core';

export const REDOVA_PO_STRANI = 10;

export class Stranicenje<T> {
  readonly strana = signal(0);

  readonly brojStrana = computed(() => Math.max(1, Math.ceil(this.lista().length / REDOVA_PO_STRANI)));

  readonly prikazani = computed(() => {
    const strana = Math.min(this.strana(), this.brojStrana() - 1);
    const od = strana * REDOVA_PO_STRANI;
    return this.lista().slice(od, od + REDOVA_PO_STRANI);
  });

  constructor(private readonly lista: Signal<T[]>) {}

  idi(korak: number): void {
    this.strana.set(Math.min(this.brojStrana() - 1, Math.max(0, this.strana() + korak)));
  }

  naPocetak(): void {
    this.strana.set(0);
  }
}
