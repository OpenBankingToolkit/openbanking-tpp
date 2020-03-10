import mongoose from './init';
import * as passportLocalMongoose from 'passport-local-mongoose';

export interface IUserBankToken extends mongoose.Document {
  _id: string;
  accessToken: string;
}

export interface IUser extends mongoose.Document {
  _id: string;
  email: string;
  firstName: string;
  lastName: string;
  banks: mongoose.Types.DocumentArray<IUserBankToken>;
}

const bankAccessTokenSchema = new mongoose.Schema({
  accessToken: String
});

export const BankAccessToken = mongoose.model('BankAccessToken', bankAccessTokenSchema) as any;

const userSchema = new mongoose.Schema({
  firstName: String,
  lastName: String,
  banks: [bankAccessTokenSchema]
});

userSchema.plugin(passportLocalMongoose, {
  usernameField: 'email', // Use email, not the default 'username'
  usernameLowerCase: true, // Ensure that all emails are lowercase
  session: false // Disable sessions as we'll use JWTs
});

const User = mongoose.model('User', userSchema) as any;

export default User;
