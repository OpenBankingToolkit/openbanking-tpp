export interface ShopItem {
  id: string;
  img: string;
  title: string;
  description: string;
  price: number;
  currency: string;
}

export interface IShopState {
  selected: number[];
  items: {
    [key: string]: ShopItem;
  };
}

export interface IState {
  shop: IShopState;
}
