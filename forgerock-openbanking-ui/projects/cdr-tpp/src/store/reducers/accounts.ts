import { Action, createSelector } from '@ngrx/store';

import { IAccountsState, IAccount, IState, IBalance, IUIAccount } from '../../models';

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
  constructor(public payload: { accounts: IAccount[] }) {}
}

export class GetAccountsErrorAction implements Action {
  readonly type = types.ACCOUNTS_ERROR;
  constructor() {}
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
  list: null,
  accounts: {},
  balances: {}
};

export default function accountsReducer(state: IAccountsState = DEFAULT_STATE, action: ActionsUnion): IAccountsState {
  switch (action.type) {
    case types.ACCOUNTS_REQUEST: {
      return {
        ...state,
        loadingAccounts: true
      };
    }
    case types.ACCOUNTS_SUCCESS: {
      return {
        ...state,
        loadingAccounts: false,
        list: <string[]>(
          Array.from(new Set([...(state.list || []), ...action.payload.accounts.map(account => account.accountId)]))
        ),
        accounts: action.payload.accounts.reduce((prev, curr) => {
          prev[curr.accountId] = curr;
          return prev;
        }, {})
      };
    }
    case types.ACCOUNTS_ERROR: {
      return {
        ...state,
        loadingAccounts: false
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
        balances: action.payload.balances.reduce((prev, curr) => {
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
export const selectList = (state: IState) => state.accounts.list;
export const selectAccounts = (state: IState) => state.accounts.accounts;
export const selectBalances = (state: IState) => state.accounts.balances;

export const selectAccountSelector = createSelector(
  selectAccounts,
  selectBalances,
  (state: IState, accountId: string) => accountId,
  (accounts, balances, accountId): IUIAccount => {
    return accounts[accountId] ? { ...accounts[accountId], ...balances[accountId] } : null;
  }
);

export const selectAccountsSelector = createSelector(
  selectList,
  selectAccounts,
  selectBalances,
  (list, accounts, balances): IUIAccount[] => {
    return list ? list.map(accountId => ({ ...accounts[accountId], ...balances[accountId] })) : null;
  }
);
