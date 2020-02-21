import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { ForgerockConfigService } from '@forgerock/openbanking-ngx-common/services/forgerock-config';
import { IAccountsResponse, ITransactionsResponse, IBalanceResponse, IBalancesResponse } from 'cdr-tpp/src/models';

@Injectable({
  providedIn: 'root'
})
export class CDRService {
  constructor(private http: HttpClient, private conf: ForgerockConfigService) {}

  public getAccounts() {
    return this.http.get<IAccountsResponse>(`assets/mocks/accounts.json`);
  }

  public getTransactions(accountId: string) {
    return this.http.get<ITransactionsResponse>(`assets/mocks/transactions.${accountId}.json`);
  }

  public getBalances(accountIds: string[]) {
    // POST /banking/accounts/balances
    /** BODY
     * {
     * "data": {
     *   "accountIds": [
     *     "string"
     *   ]
     * },
     * "meta": {}
     * }
     */
    return this.http.get<IBalancesResponse>(`assets/mocks/balances.json`);
  }

  public getBalance(accountId: string) {
    return this.http.get<IBalanceResponse>(`assets/mocks/balance.${accountId}.json`);
  }
}
