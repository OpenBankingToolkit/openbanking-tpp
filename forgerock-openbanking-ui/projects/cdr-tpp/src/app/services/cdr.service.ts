import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { CookieService } from 'ngx-cookie';

import { ForgerockConfigService } from '@forgerock/openbanking-ngx-common/services/forgerock-config';
import {
  IAccountsResponse,
  ITransactionsResponse,
  ILoginResponse,
  IBanksResponse,
  IUserResponse
} from 'cdr-tpp/src/models';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class CDRService {
  constructor(private http: HttpClient, private conf: ForgerockConfigService, private cookieService: CookieService) {}

  public exchangeCode(body: { code: string; state: string; idToken: string }) {
    return this.http.post(`${this.conf.get('nodeBackend')}/exchange-code`, body, this.getHeaders());
  }

  public register(body: { firstName: string; lastName: string; email: string; password: string }) {
    return this.http.post(`${this.conf.get('nodeBackend')}/register`, body);
  }

  public login(body: { email: string; password: string }) {
    return this.http
      .post<ILoginResponse>(`${this.conf.get('nodeBackend')}/login`, body)
      .pipe(tap(({ token }) => this.cookieService.put('bearer', token)));
  }

  public getUser() {
    return this.http.get<IUserResponse>(`${this.conf.get('nodeBackend')}/user`, this.getHeaders());
  }

  public getAccounts() {
    return this.http.get<IAccountsResponse>(`${this.conf.get('nodeBackend')}/accounts`, this.getHeaders());
  }

  public getBanks() {
    return this.http.get<IBanksResponse>(`${this.conf.get('nodeBackend')}/banks`, this.getHeaders());
  }

  public getTransactions(bankId: string, accountId: string) {
    return this.http.get<ITransactionsResponse>(
      `${this.conf.get('nodeBackend')}/transactions/${bankId}/${accountId}`,
      this.getHeaders()
    );
  }

  // public getBalances(accountIds: string[]) {
  //   return this.http.get<IBalancesResponse>(`assets/mocks/balances.json`);
  // }

  // public getBalance(accountId: string) {
  //   return this.http.get<IBalanceResponse>(`assets/mocks/balance.${accountId}.json`);
  // }

  private getHeaders() {
    return {
      withCredentials: false,
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        Authorization: `Bearer ${this.cookieService.get('bearer')}`
      })
    };
  }
}
