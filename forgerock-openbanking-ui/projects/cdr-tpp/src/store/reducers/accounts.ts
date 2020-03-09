import { Action, createSelector } from '@ngrx/store';

import { IAccountsState, IState, IBalance, IUIAccount, IBank } from '../../models';
import { selectBanks } from './banks';

export enum types {
  ACCOUNTS_REQUEST = 'ACCOUNTS_REQUEST',
  ACCOUNTS_SUCCESS = 'ACCOUNTS_SUCCESS',
  ACCOUNTS_ERROR = 'ACCOUNTS_ERROR',
  ACCOUNTS_BALANCES_REQUEST = 'ACCOUNTS_BALANCES_REQUEST',
  ACCOUNTS_BALANCES_SUCCESS = 'ACCOUNTS_BALANCES_SUCCESS',
  ACCOUNTS_BALANCES_ERROR = 'ACCOUNTS_BALANCES_ERROR',
  ACCOUNTS_REMOVE_ALL = 'ACCOUNTS_REMOVE_ALL'
}

export class GetAccountsRequestAction implements Action {
  readonly type = types.ACCOUNTS_REQUEST;
  constructor() {}
}

export class GetAccountsSuccessAction implements Action {
  readonly type = types.ACCOUNTS_SUCCESS;
  constructor(public payload: { banks: IBank[]; accounts: IUIAccount[] }) {}
}

export class GetAccountsErrorAction implements Action {
  readonly type = types.ACCOUNTS_ERROR;
  constructor(public payload: { error: string }) {}
}

export class GetAccountsBalancesRequestAction implements Action {
  readonly type = types.ACCOUNTS_BALANCES_REQUEST;
  constructor(public payload: { accountIds: string[] }) {}
}

export class GetAccountsBalancesSuccessAction implements Action {
  readonly type = types.ACCOUNTS_BALANCES_SUCCESS;
  constructor(public payload: { page: number; balances: IBalance[] }) {}
}

export class GetAccountsBalancesErrorAction implements Action {
  readonly type = types.ACCOUNTS_BALANCES_ERROR;
  constructor() {}
}

export class GetAccountsRemoveAllAction implements Action {
  readonly type = types.ACCOUNTS_REMOVE_ALL;
  constructor() {}
}

export type ActionsUnion =
  | GetAccountsRequestAction
  | GetAccountsSuccessAction
  | GetAccountsErrorAction
  | GetAccountsBalancesRequestAction
  | GetAccountsBalancesSuccessAction
  | GetAccountsBalancesErrorAction
  | GetAccountsRemoveAllAction;

export const DEFAULT_STATE: IAccountsState = {
  loadingAccounts: false,
  loadingBalances: false,
  accountsError: '',
  list: null,
  accounts: {},
  balances: {}
};

export default function accountsReducer(state: IAccountsState = DEFAULT_STATE, action: ActionsUnion): IAccountsState {
  switch (action.type) {
    case types.ACCOUNTS_REQUEST: {
      return {
        ...state,
        accountsError: '',
        loadingAccounts: true
      };
    }
    case types.ACCOUNTS_SUCCESS: {
      const { accounts } = action.payload;
      return {
        ...state,
        accountsError: '',
        loadingAccounts: false,
        list: <string[]>Array.from(new Set([...(state.list || []), ...accounts.map(account => account.accountId)])),
        ...accounts.reduce<any>(
          (prev, curr) => {
            const { balance, ...rest } = curr;
            prev.accounts[curr.accountId] = rest;
            prev.balances[curr.accountId] = balance;
            return prev;
          },
          { accounts: {}, balances: {} }
        )
      };
    }
    case types.ACCOUNTS_ERROR: {
      const { error } = action.payload;
      return {
        ...state,
        loadingAccounts: false,
        accountsError: error
      };
    }
    case types.ACCOUNTS_BALANCES_REQUEST: {
      return {
        ...state,
        loadingBalances: true
      };
    }
    case types.ACCOUNTS_BALANCES_SUCCESS: {
      return {
        ...state,
        loadingBalances: false,
        balances: action.payload.balances.reduce<{ [key: string]: IBalance }>((prev, curr) => {
          prev[curr.accountId] = curr;
          return prev;
        }, {})
      };
    }
    case types.ACCOUNTS_BALANCES_ERROR: {
      return {
        ...state,
        loadingBalances: false
      };
    }
    case types.ACCOUNTS_REMOVE_ALL: {
      return DEFAULT_STATE;
    }
    default:
      return state;
  }
}

export const selectLoadingAccounts = (state: IState) => state.accounts.loadingAccounts;
export const selectLoadingBalances = (state: IState) => state.accounts.loadingBalances;
export const selectAccountsError = (state: IState) => state.accounts.accountsError;
export const selectList = (state: IState) => state.accounts.list;
export const selectAccounts = (state: IState) => state.accounts.accounts;
export const selectBalances = (state: IState) => state.accounts.balances;

export const selectAccountSelector = createSelector(
  selectAccounts,
  selectBalances,
  (state: IState, accountId: string) => accountId,
  selectBanks,
  (accounts, balances, accountId, banks): IUIAccount | null => {
    return accounts[accountId]
      ? { ...accounts[accountId], ...balances[accountId], bank: banks[accounts[accountId].bankId] }
      : null;
  }
);

export const selectAccountsSelector = createSelector(
  selectList,
  selectAccounts,
  selectBalances,
  selectBanks,
  (list, accounts, balances, banks): IUIAccount[] | null => {
    console.log(banks);
    return list
      ? list.map(accountId => {
          const account = accounts[accountId];
          return { ...account, ...balances[accountId], bank: banks[account.bankId] };
        })
      : null;
  }
);
