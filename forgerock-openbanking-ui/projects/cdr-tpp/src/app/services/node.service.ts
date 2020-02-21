import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { ForgerockConfigService } from '@forgerock/openbanking-ngx-common/services/forgerock-config';

@Injectable({
  providedIn: 'root'
})
export class TppNodeService {
  constructor(private http: HttpClient, private conf: ForgerockConfigService) {}

  public getDomesticPayementRedirection() {
    return this.http.get<{
      redirection: string;
    }>(`${this.conf.get('nodeBackend')}/domestic-payment`);
  }

  public getRestDomesticPayementRedirection() {
    return this.http.get<{
      redirection: string;
    }>(`${this.conf.get('nodeBackend')}/rest-domestic-payment`);
  }
}
