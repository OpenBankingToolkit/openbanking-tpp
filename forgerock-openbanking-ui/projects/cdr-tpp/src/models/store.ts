import { ITransaction, IBalance, IBank, IUser, IUIAccount } from './cdr';

export interface IAccountsState {
  loadingAccounts: boolean;
  loadingBalances: boolean;
  list: string[] | null;
  accounts: {
    [accountId: string]: IUIAccount;
  };
  accountsError: string;
  balances: {
    [accountId: string]: IBalance;
  };
}

export interface ITransactionsState {
  error: string;
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

export interface IBanksState {
  isLoading: boolean;
  list: string[] | null;
  banks: {
    [bankId: string]: IBank;
  };
}

export interface IUserState {
  isLoading: boolean;
  user: IUser;
}

export interface IState {
  accounts: IAccountsState;
  transactions: ITransactionsState;
  banks: IBanksState;
  user: IUserState;
}
