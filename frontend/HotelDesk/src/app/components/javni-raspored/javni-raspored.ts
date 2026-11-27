import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Footer } from '../footer/footer';
import { Header } from '../header/header';

@Component({
  selector: 'app-javni-raspored',
  imports: [RouterOutlet, Header, Footer],
  template: '<app-header /><router-outlet /><app-footer />',
  host: { style: 'display: contents' }
})
export class JavniRaspored {}
