import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Grad } from '../models';

@Injectable({ providedIn: 'root' })
export class GradService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/gradovi`;

  findAll(): Observable<Grad[]> {
    return this.http.get<Grad[]>(this.url);
  }
}
