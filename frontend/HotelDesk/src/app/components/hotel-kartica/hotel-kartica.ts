import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Hotel } from '../../models';

const PALETE = [
  ['#2a2d34', '#5d6371'],
  ['#2a2d34', '#2f5bff'],
  ['#1f2127', '#8a8b85'],
  ['#1f45e0', '#8fa6ff'],
  ['#3a3e47', '#b9b7ae']
];

@Component({
  selector: 'app-hotel-kartica',
  imports: [RouterLink],
  templateUrl: './hotel-kartica.html',
  styleUrl: './hotel-kartica.css'
})
export class HotelKartica {
  readonly hotel = input.required<Hotel>();
  readonly upit = input<Record<string, string>>({});

  protected readonly zvezdice = computed(() => Array.from({ length: this.hotel().brojZvezdica ?? 0 }));

  protected readonly pozadina = computed(() => {
    const [tamna, svetla] = PALETE[(this.hotel().id ?? 0) % PALETE.length];
    return `linear-gradient(135deg, ${tamna} 0%, ${svetla} 100%)`;
  });

  protected readonly opisOcene = computed(() => {
    const ocena = this.hotel().prosecnaOcena;
    if (ocena == null) {
      return 'Još bez ocena';
    }
    if (ocena >= 9) {
      return 'Izvanredan';
    }
    if (ocena >= 8) {
      return 'Odličan';
    }
    if (ocena >= 7) {
      return 'Vrlo dobar';
    }
    return 'Dobar';
  });
}
