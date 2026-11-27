import { Component, input } from '@angular/core';
import { Stranicenje } from './stranicenje';

@Component({
  selector: 'app-strane',
  template: `
    @if (s().brojStrana() > 1) {
      <div class="desk-strane">
        <button type="button" class="desk-dugme" (click)="s().idi(-1)" [disabled]="s().strana() === 0">Prethodna</button>
        <span class="desk-polje">Strana {{ s().strana() + 1 }} od {{ s().brojStrana() }}</span>
        <button type="button" class="desk-dugme" (click)="s().idi(1)" [disabled]="s().strana() >= s().brojStrana() - 1">Sledeća</button>
      </div>
    }
  `
})
export class Strane {
  readonly s = input.required<Stranicenje<unknown>>();
}
