import mongoose from './init';

export interface IState extends mongoose.Document {
  _id: string;
  bankId: string;
  userId: string;
}

const StateSchema = new mongoose.Schema({
  bankId: String,
  userId: String
});

const State = mongoose.model('State', StateSchema);

export default State;
