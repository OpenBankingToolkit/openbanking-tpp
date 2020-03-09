import { Action, createSelector } from '@ngrx/store';
import _get from 'lodash-es/get';

import { IBanksState, IBank, IState } from '../../models';
import { types as accountsTypes, GetAccountsSuccessAction } from './accounts';
import { types as userTypes, GetUserLogoutSuccessAction } from './user';

export enum types {
  BANKS_REQUEST = 'BANKS_REQUEST',
  BANKS_SUCCESS = 'BANKS_SUCCESS',
  BANKS_ERROR = 'BANKS_ERROR',
  BANKS_REMOVE_ALL = 'BANKS_REMOVE_ALL'
}

export class GetBanksRequestAction implements Action {
  readonly type = types.BANKS_REQUEST;
  constructor() {}
}

export class GetBanksSuccessAction implements Action {
  readonly type = types.BANKS_SUCCESS;
  constructor(public payload: { banks: IBank[] }) {}
}

export class GetBanksErrorAction implements Action {
  readonly type = types.BANKS_ERROR;
  constructor() {}
}

export type ActionsUnion = GetBanksRequestAction | GetBanksSuccessAction | GetBanksErrorAction | GetUserLogoutSuccessAction;

export const DEFAULT_STATE: IBanksState = {
  isLoading: false,
  list: null,
  banks: {}
};

export default function banksReducer(
  state: IBanksState = DEFAULT_STATE,
  action: ActionsUnion | GetAccountsSuccessAction
): IBanksState {
  switch (action.type) {
    case types.BANKS_REQUEST: {
      return {
        ...state,
        isLoading: true
      };
    }
    case types.BANKS_SUCCESS: {
      const { banks } = action.payload;
      return {
        ...state,
        isLoading: false,
        list: banks.map(account => account.id),
        banks: banks.reduce<{ [key: string]: IBank }>((prev, curr) => {
          prev[curr.id] = curr;
          return prev;
        }, {})
      };
    }
    case accountsTypes.ACCOUNTS_SUCCESS: {
      const { banks } = action.payload;
      return {
        ...state,
        banks: banks.reduce<{ [key: string]: IBank }>((prev, curr) => {
          prev[curr.id] = curr;
          return prev;
        }, {})
      };
    }
    case types.BANKS_ERROR: {
      return {
        ...state,
        isLoading: false
      };
    }
    case userTypes.USER_LOGOUT_SUCCESS: {
      return DEFAULT_STATE;
    }
    default:
      return state;
  }
}

export const selectList = (state: IState) => state.banks.list;
export const selectIsLoading = (state: IState) => state.banks.isLoading;
export const selectBanks = (state: IState) => state.banks.banks;
export const selectBank = (state: IState, bankId: string) => state.banks.banks[bankId] || {};

export const selectBanksSelector = createSelector(selectList, selectBanks, (list, banks) => {
  return list ? list.map(bankId => banks[bankId]) : null;
});
