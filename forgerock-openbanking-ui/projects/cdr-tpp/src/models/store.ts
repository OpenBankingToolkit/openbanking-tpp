import { ITransaction, IAccount, IBalance } from './cdr';

export interface IAccountsState {
  loadingAccounts: boolean;
  loadingBalances: boolean;
  list: string[] | null;
  accounts: {
    [accountId: string]: IAccount;
  };
  balances: {
    [accountId: string]: IBalance[];
  };
}

export interface ITransactionsState {
  isLoading: {
    [accountId: string]: boolean;
  };
  list: {
    [accountId: string]: string[];
  };
  transactions: {
    [transactionId: string]: ITransaction;
  };
}

export interface IState {
  accounts: IAccountsState;
  transactions: ITransactionsState;
}
