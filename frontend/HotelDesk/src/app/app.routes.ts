import { Routes } from '@angular/router';
import { Pocetna } from './components/pocetna/pocetna';

export const routes: Routes = [
  { path: '', component: Pocetna, title: 'HotelDesk' },
  { path: '**', redirectTo: '' }
];
