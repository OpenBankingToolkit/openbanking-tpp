import { Action } from '@ngrx/store';

import { IUserState, IUser, IState } from '../../models';

export enum types {
  USER_REQUEST = 'USER_REQUEST',
  USER_SUCCESS = 'USER_SUCCESS',
  USER_ERROR = 'USER_ERROR',
  USER_LOGOUT_REQUEST = 'USER_LOGOUT_REQUEST',
  USER_LOGOUT_SUCCESS = 'USER_LOGOUT_SUCCESS',
  USER_LOGOUT_ERROR = 'USER_LOGOUT_ERROR'
}

export class GetUserRequestAction implements Action {
  readonly type = types.USER_REQUEST;
  constructor() {}
}

export class GetUserSuccessAction implements Action {
  readonly type = types.USER_SUCCESS;
  constructor(public payload: { user: IUser }) {}
}

export class GetUserErrorAction implements Action {
  readonly type = types.USER_ERROR;
  constructor() {}
}

export class GetUserLogoutRequestAction implements Action {
  readonly type = types.USER_LOGOUT_REQUEST;
  constructor() {}
}

export class GetUserLogoutSuccessAction implements Action {
  readonly type = types.USER_LOGOUT_SUCCESS;
  constructor() {}
}

export class GetUserLogoutErrorAction implements Action {
  readonly type = types.USER_LOGOUT_ERROR;
  constructor() {}
}

export type ActionsUnion =
  | GetUserRequestAction
  | GetUserSuccessAction
  | GetUserErrorAction
  | GetUserLogoutRequestAction
  | GetUserLogoutSuccessAction;

export const DEFAULT_STATE: IUserState = {
  isLoading: false,
  user: undefined
};

export default function userReducer(state: IUserState = DEFAULT_STATE, action: ActionsUnion): IUserState {
  switch (action.type) {
    case types.USER_REQUEST: {
      return {
        ...state,
        isLoading: true
      };
    }
    case types.USER_SUCCESS: {
      const { user } = action.payload;
      return {
        ...state,
        isLoading: false,
        user
      };
    }
    case types.USER_ERROR: {
      return {
        ...state,
        isLoading: false
      };
    }
    case types.USER_LOGOUT_SUCCESS: {
      return {
        ...state,
        isLoading: false,
        user: undefined
      };
    }
    default:
      return state;
  }
}

export const selectIsLoading = (state: IState) => state.user.isLoading;
export const selectUser = (state: IState) => state.user.user;
