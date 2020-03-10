import { AccountsEffects } from './accounts';
import { TransactionsEffects } from './transactions';
import { BanksEffects } from './banks';
import { UserEffects } from './user';

export const RootEffects = [AccountsEffects, TransactionsEffects, BanksEffects, UserEffects];
