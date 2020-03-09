interface Meta {
  totalPages: number;
}

interface Links {
  self: string;
  first?: string;
  next?: string;
  last?: string;
}

export enum ProductCategory {
  TRANS_AND_SAVINGS_ACCOUNTS = 'TRANS_AND_SAVINGS_ACCOUNTS',
  TERM_DEPOSITS = 'TERM_DEPOSITS',
  TRAVEL_CARDS = 'TRAVEL_CARDS',
  REGULATED_TRUST_ACCOUNTS = 'REGULATED_TRUST_ACCOUNTS',
  RESIDENTIAL_MORTGAGES = 'RESIDENTIAL_MORTGAGES',
  CRED_AND_CHRG_CARDS = 'CRED_AND_CHRG_CARDS',
  PERS_LOANS = 'PERS_LOANS',
  MARGIN_LOANS = 'MARGIN_LOANS',
  LEASES = 'LEASES',
  TRADE_FINANCE = 'TRADE_FINANCE',
  OVERDRAFTS = 'OVERDRAFTS',
  BUSINESS_LOANS = 'BUSINESS_LOANS'
}

export interface ILoginResponse {
  token: string;
}

export interface IUser {
  email: string;
  firstName: string;
  lastName: string;
}

export interface IUserResponse {
  user: IUser;
}

export interface IAccount {
  accountId: string;
  creationDate: string;
  displayName: string;
  nickname: string;
  openStatus: string;
  isOwned: boolean;
  maskedNumber: string;
  productCategory: ProductCategory;
  productName: string;
}

export interface IUIAccount extends IAccount, Partial<Omit<IBalance, 'accountId'>> {
  bank: IBank; // this is non standard
  bankId: string; // this is non standard and used frontend side to match a bank
  balance: IBalance; // this is non standard
  currentBalance: string; // this is non standard
  currency: string; // this is non standard
}

export interface IAccountsResponse {
  accounts: IUIAccount[];
  banks: IBank[];
}

export interface ITransaction {
  accountId: string;
  transactionId: string;
  isDetailAvailable: boolean;
  type: string;
  status: string;
  description: string;
  postingDateTime: string;
  valueDateTime: string;
  executionDateTime: string;
  amount: string;
  currency: string;
  reference: string;
  merchantName: string;
  merchantCategoryCode: string;
  billerCode: string;
  billerName: string;
  crn: string;
  apcaNumber: string;
}

export interface ITransactionsResponse {
  transactions: ITransaction[];
}

export interface IBalance {
  accountId: string;
  currentBalance: string;
  availableBalance: string;
  currency: string;
  creditLimit?: string;
  amortisedLimit?: string;
  purses?: BankingBalancePurse[];
}

interface BankingBalancePurse {
  amount: string;
  currency?: string;
}

export interface IBalanceResponse {
  data: IBalance;
  links: Links;
  meta: Meta;
}

export interface IBalancesResponse {
  data: { balances: IBalance[] };
  links: Links;
  meta: Meta;
}

export interface IBank {
  id: string;
  name: string;
  logo: string;
}

export interface IBanksResponse {
  banks: IBank[];
}
