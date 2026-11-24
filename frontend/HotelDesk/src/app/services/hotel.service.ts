import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Hotel } from '../models';

export interface HotelPretraga {
  gradId?: number | null;
  naziv?: string | null;
  minZvezdica?: number | null;
}

@Injectable({ providedIn: 'root' })
export class HotelService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/hoteli`;

  pretraga(filter: HotelPretraga = {}): Observable<Hotel[]> {
    let params = new HttpParams();
    if (filter.gradId) {
      params = params.set('gradId', filter.gradId);
    }
    if (filter.naziv?.trim()) {
      params = params.set('naziv', filter.naziv.trim());
    }
    if (filter.minZvezdica) {
      params = params.set('minZvezdica', filter.minZvezdica);
    }
    return this.http.get<Hotel[]>(this.url, { params });
  }

  getById(id: number): Observable<Hotel> {
    return this.http.get<Hotel>(`${this.url}/${id}`);
  }
}
