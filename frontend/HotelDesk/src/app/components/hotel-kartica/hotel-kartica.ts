import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Hotel } from '../../models';

@Component({
  selector: 'app-hotel-kartica',
  imports: [RouterLink],
  templateUrl: './hotel-kartica.html',
  styleUrl: './hotel-kartica.css'
})
export class HotelKartica {
  readonly hotel = input.required<Hotel>();
  readonly upit = input<Record<string, string>>({});

  protected readonly zvezdice = computed(() => '★'.repeat(this.hotel().brojZvezdica ?? 0));

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
