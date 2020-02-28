import { Action, createSelector } from '@ngrx/store';

import _get from 'lodash-es/get';
import _remove from 'lodash-es/remove';
import { IShopState, IState } from '../models';

export const types = {
  SHOP_ADD: 'SHOP_ADD',
  SHOP_REMOVE: 'SHOP_REMOVE',
  SHOP_REMOVE_ALL: 'SHOP_REMOVE_ALL'
};

export class GetShopAddAction implements Action {
  readonly type = types.SHOP_ADD;
  constructor(public payload: { id: string }) {}
}

export class GetShopRemoveAction implements Action {
  readonly type = types.SHOP_REMOVE;
  constructor(public payload: { id: string }) {}
}

export class GetShopRemoveAllAction implements Action {
  readonly type = types.SHOP_REMOVE_ALL;
  constructor() {}
}

export type ActionsUnion = GetShopAddAction | GetShopRemoveAction | GetShopRemoveAllAction;

export const DEFAULT_STATE: IShopState = {
  selected: [],
  items: [
    {
      description:
        'This dress is made from made from pastel yellow and dark gray fabrics.  It has a medium-length poofy skirt with an inverted V-line waist and strap sleeves.  It is accented with a belt.'
    },
    {
      description:
        'This dress is made from made from yellow and black fabrics.  It has a short poofy skirt with an inverted V-line waist and short narrow sleeves.'
    },
    {
      description:
        'This dress is made from made from magenta fabric.  It has a long narrow skirt with an empire waist and bell-shaped sleeves.'
    },
    {
      description:
        'This dress is made from made from dark aqua fabric.  It has a medium-length narrow skirt that flares at the bottom with an empire waist and spaghetti straps.  It is accented with lace.'
    },
    {
      description:
        'This dress is made from made from dark blue fabric and dark green and dark gray zebra-stripe print fabric.  It has a long flared skirt with a loose waist and elbow-length narrow sleeves.'
    },
    {
      description:
        'This dress is made from made from dark pink and vivid purple fabrics.  It has a short poofy skirt with a gathered waist and short gathered sleeves.'
    },
    {
      description:
        'This dress is made from made from dark turquoise, dark blue, and vivid orange fabrics.  It has a medium-length flared skirt with a loose waist and leg-of-mutton sleeves.  It is accented with lace.'
    },
    {
      description:
        'This dress is made from made from vivid yellow and dark purple zebra-stripe print fabric.  It has a short poofy skirt with an empire waist and spaghetti straps.  It is accented with lace.'
    },
    {
      description:
        'This dress is made from made from gray fabric.  It has a short flared skirt with an inverted V-line waist and long wide sleeves.'
    },
    {
      description: ''
    }
  ].reduce((prev, curr, index) => {
    prev[String(index)] = {
      id: String(index),
      title: `Dress ${index}`,
      currency: 'GBP',
      price: Math.floor(Math.random() * 1000) + 1,
      img: `assets/images/shop-dress${Math.floor(Math.random() * 3) + 1}.jpg`,
      ...curr
    };
    return prev;
  }, {})
};

export default function chartsReducer(state: IShopState = DEFAULT_STATE, action: any): IShopState {
  switch (action.type) {
    case types.SHOP_ADD: {
      return {
        ...state,
        selected: [...state.selected, action.payload.id]
      };
    }
    case types.SHOP_REMOVE: {
      const indexToRemove = state.selected.indexOf(action.payload.id);

      if (indexToRemove < 0) return state;

      const selected = [...state.selected];

      selected.splice(indexToRemove, 1);

      return {
        ...state,
        selected
      };
    }
    case types.SHOP_REMOVE_ALL: {
      return DEFAULT_STATE;
    }
    default:
      return state;
  }
}

export const selectShopSelected = (state: IState) => state.shop.selected;
export const selectShopItems = (state: IState) => state.shop.items;
export const selectShopItem = (state: IState, id: string) => state.shop.items[id];

export const selectShopSelectedItems = createSelector(
  selectShopItems,
  selectShopSelected,
  (items, selected) => selected.map(selectedId => items[selectedId])
);
