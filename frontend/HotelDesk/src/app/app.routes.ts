import { Routes } from '@angular/router';
import { samoGost, samoPrijavljen } from './guards/auth.guards';
import { samoUloga } from './guards/uloga';
import { JavniRaspored } from './components/javni-raspored/javni-raspored';
import { Pocetna } from './components/pocetna/pocetna';

export const routes: Routes = [
  {
    path: '',
    component: JavniRaspored,
    children: [
      { path: '', component: Pocetna, title: 'HotelDesk: rezervacija hotela u Srbiji i regionu' },
      {
        path: 'prijava',
        canActivate: [samoGost],
        loadComponent: () => import('./components/prijava/prijava').then((m) => m.Prijava),
        title: 'Prijava | HotelDesk'
      },
      {
        path: 'registracija',
        canActivate: [samoGost],
        loadComponent: () => import('./components/registracija/registracija').then((m) => m.Registracija),
        title: 'Napravi nalog | HotelDesk'
      }
    ]
  },
  {
    path: '',
    canActivate: [samoPrijavljen],
    loadComponent: () => import('./components/desk/raspored/raspored').then((m) => m.DeskRaspored),
    children: [
      {
        path: 'recepcija',
        canActivate: [samoUloga('ZAPOSLENI', 'RECEPCIONER')],
        loadComponent: () => import('./components/desk/recepcija/recepcija').then((m) => m.Recepcija),
        title: 'FrontDesk | HotelDesk',
        data: { naslov: 'FrontDesk', sekcije: [{ id: 'pregled', naziv: 'Pregled dana' }, { id: 'dolasci', naziv: 'Dolasci' }, { id: 'odlasci', naziv: 'Odlasci' }] }
      },
      {
        path: 'ciscenje',
        canActivate: [samoUloga('ZAPOSLENI', 'CISTAC')],
        loadComponent: () => import('./components/desk/ciscenje/ciscenje').then((m) => m.Ciscenje),
        title: 'StaffDesk | HotelDesk',
        data: { naslov: 'StaffDesk', sekcije: [{ id: 'pregled', naziv: 'Stanje soba' }, { id: 'sobe', naziv: 'Za čišćenje' }, { id: 'tiketi', naziv: 'Moji tiketi' }] }
      },
      {
        path: 'menadzer',
        canActivate: [samoUloga('ZAPOSLENI', 'MENADZER')],
        loadComponent: () => import('./components/desk/menadzer/menadzer').then((m) => m.Menadzer),
        title: 'ManagerDesk | HotelDesk',
        data: {
          naslov: 'ManagerDesk',
          sekcije: [{ id: 'pregled', naziv: 'Pregled' }, { id: 'tiketi', naziv: 'Tiketi' }, { id: 'sobe', naziv: 'Sobe' }, { id: 'zaposleni', naziv: 'Zaposleni' }]
        }
      },
      {
        path: 'vlasnik',
        canActivate: [samoUloga('VLASNIK')],
        loadComponent: () => import('./components/desk/vlasnik/vlasnik').then((m) => m.Vlasnik),
        title: 'OwnerDesk | HotelDesk',
        data: { naslov: 'OwnerDesk', sekcije: [{ id: 'hoteli', naziv: 'Moji hoteli' }, { id: 'poslovanje', naziv: 'Poslovanje' }, { id: 'recenzije', naziv: 'Recenzije' }] }
      },
      {
        path: 'admin',
        canActivate: [samoUloga('ADMIN')],
        loadComponent: () => import('./components/desk/admin/admin').then((m) => m.Admin),
        title: 'AdminDesk | HotelDesk',
        data: {
          naslov: 'AdminDesk',
          sekcije: [{ id: 'pregled', naziv: 'Pregled' }, { id: 'korisnici', naziv: 'Nalozi' }, { id: 'gradovi', naziv: 'Gradovi' }, { id: 'tipovi', naziv: 'Tipovi soba' }]
        }
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
