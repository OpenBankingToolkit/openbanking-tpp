import { Action, createSelector } from '@ngrx/store';

import { ITransactionsState, ITransaction, IState } from '../../models';

export enum types {
  TRANSACTIONS_REQUEST = 'TRANSACTIONS_REQUEST',
  TRANSACTIONS_SUCCESS = 'TRANSACTIONS_SUCCESS',
  TRANSACTIONS_ERROR = 'TRANSACTIONS_ERROR',
  TRANSACTIONS_REMOVE_ALL = 'TRANSACTIONS_REMOVE_ALL'
}

export class GetTransactionsRequestAction implements Action {
  readonly type = types.TRANSACTIONS_REQUEST;
  constructor(public payload: { bankId: string; accountId: string }) {}
}

export class GetTransactionsSuccessAction implements Action {
  readonly type = types.TRANSACTIONS_SUCCESS;
  constructor(public payload: { accountId: string; transactions: ITransaction[] }) {}
}

export class GetTransactionsErrorAction implements Action {
  readonly type = types.TRANSACTIONS_ERROR;
  constructor(public payload: { accountId: string; error: string }) {}
}

export type ActionsUnion = GetTransactionsRequestAction | GetTransactionsSuccessAction | GetTransactionsErrorAction;

export const DEFAULT_STATE: ITransactionsState = {
  error: '',
  isLoading: {},
  list: {},
  transactions: {}
};

export default function transactionsReducer(
  state: ITransactionsState = DEFAULT_STATE,
  action: ActionsUnion
): ITransactionsState {
  switch (action.type) {
    case types.TRANSACTIONS_REQUEST: {
      const { accountId } = action.payload;
      return {
        ...state,
        error: '',
        isLoading: {
          ...state.isLoading,
          [accountId]: true
        }
      };
    }
    case types.TRANSACTIONS_SUCCESS: {
      const { transactions, accountId } = action.payload;
      return {
        ...state,
        error: '',
        isLoading: {
          ...state.isLoading,
          [accountId]: false
        },
        list: {
          ...state.list,
          [accountId]: state.list[accountId]
            ? Array.from(new Set([...state.list[accountId], ...transactions.map(account => account.transactionId)]))
            : transactions.map(account => account.transactionId)
        },
        transactions: {
          ...state.transactions,
          ...transactions.reduce<{ [key: string]: ITransaction }>((prev, curr) => {
            prev[curr.transactionId] = curr;
            return prev;
          }, {})
        }
      };
    }
    case types.TRANSACTIONS_ERROR: {
      const { accountId, error } = action.payload;
      return {
        ...state,
        error,
        isLoading: {
          ...state.isLoading,
          [accountId]: false
        }
      };
    }
    default:
      return state;
  }
}

export const selectList = (state: IState, accountId: string) => state.transactions.list[accountId];
export const selectIsLoading = (state: IState, accountId: string) => state.transactions.isLoading[accountId];
export const selectTransactions = (state: IState) => state.transactions.transactions;
export const selectError = (state: IState) => state.transactions.error;
export const selectTransaction = (state: IState, accountId: string) => state.transactions.transactions[accountId];

export const selectTransactionsSelector = createSelector(
  selectList,
  selectTransactions,
  (list, transactions, accountId) => (list ? list.map(transactionId => transactions[transactionId]) : null)
);
