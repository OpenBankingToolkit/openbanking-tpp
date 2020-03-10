import * as mongoose from 'mongoose';

mongoose
  .connect(`mongodb://${process.env.MONGO_HOST || '127.0.0.1'}:27017/${process.env.MONGO_DB || 'test'}`, {
    useNewUrlParser: true,
    useUnifiedTopology: true
  })
  .then(() => {
    console.log('Successfully connected to database');
  })
  .catch(error => {
    //   If there was an error connecting to the database
    if (error) console.log('Error connecting to MongoDB database', error);
  });

export default mongoose;
