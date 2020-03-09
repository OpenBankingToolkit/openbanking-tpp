import mongoose from './init';

export interface IBank extends mongoose.Document {
  _id: string;
  name: string;
  logo: string;
  domain: string;
  key: string;
  cert: string;
  discoveryAddress: string;
  directoryAddress: string;
  jwkmsAddress: string;
  matlsAddress: string;
  clientId?: string; // optional if not registered
  registrationAccessToken?: string; // optional if not registered
}

const bankSchema = new mongoose.Schema({
  name: String,
  logo: String,
  domain: String,
  key: String,
  cert: String,
  discoveryAddress: String,
  directoryAddress: String,
  jwkmsAddress: String,
  matlsAddress: String,
  clientId: String, // registration ID
  registrationAccessToken: String
});

const Bank = mongoose.model('Bank', bankSchema);

export default Bank;
