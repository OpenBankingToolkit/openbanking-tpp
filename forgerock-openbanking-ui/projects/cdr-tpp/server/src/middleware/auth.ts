import * as express from 'express';
import * as passport from 'passport';
import * as JWT from 'jsonwebtoken';
import * as PassportJwt from 'passport-jwt';
import User, { IUser } from '../db/User';

// secret (generated using `openssl rand -base64 48` from console)
const jwtSecret = 'lMXxzztc9Abm9FmRbRmY63DCD2a4WlAcbeZJ/IOmhpEfCPiio93QysoM++X9VR5G';
const jwtAlgorithm = 'HS256';
const jwtExpiresIn = '7 days';

passport.use(User.createStrategy());

export function register(req: express.Request, res: express.Response, next: express.NextFunction) {
  const newUser = new User({
    email: req.body.email,
    firstName: req.body.firstName,
    lastName: req.body.lastName
  });
  // Create the user with the specified password
  User.register(newUser, req.body.password, (error: any, user: any) => {
    if (error) {
      next(error.message);
      return;
    }
    // Store user so we can access it in our handler
    req.user = user;
    // Success!
    next();
  });
}

passport.use(
  new PassportJwt.Strategy(
    // Options
    {
      // Where will the JWT be passed in the HTTP request?
      // e.g. Authorization: Bearer xxxxxxxxxx
      jwtFromRequest: PassportJwt.ExtractJwt.fromAuthHeaderAsBearerToken(),
      // What is the secret
      secretOrKey: jwtSecret,
      // What algorithm(s) were used to sign it?
      algorithms: [jwtAlgorithm]
    },
    // When we have a verified token
    (payload, done) => {
      // Find the real user from our database using the `id` in the JWT
      User.findById(payload.sub)
        .then((user: any) => {
          // If user was found with this id
          if (user) {
            done(null, user);
          } else {
            // If not user was found
            done(null, false);
          }
        })
        .catch((error: any) => {
          // If there was failure
          done(error, false);
        });
    }
  )
);

export function getUserFromToken(token: string): Promise<IUser> {
  console.log('getUserFromToken', { token });

  return new Promise((res, rej) => {
    try {
      JWT.verify(token, jwtSecret, { algorithms: [jwtAlgorithm] }, async (error, payload: any) => {
        // console.log({ decoded });
        if (typeof payload !== 'object') {
          throw new Error('');
        }
        const user = await User.findById(payload.sub);
        res(user);
      });
    } catch (error) {
      rej(error);
    }
  });
}

export function signJWTForUser(req: express.Request, res: express.Response) {
  // Get the user (either just signed in or signed up)
  const user = <any>req.user;

  if (!user) {
    res.send('signJWTForUser need user');
    return;
  }
  // Create a signed token
  const token = JWT.sign(
    {
      email: user.email
    },
    jwtSecret,
    {
      algorithm: jwtAlgorithm,
      expiresIn: jwtExpiresIn,
      subject: user._id.toString()
    }
  );
  // Send the token
  res.json({ token });
}

export const passportInit = passport.initialize();
export const signIn = passport.authenticate('local', { session: false });
export const requireJWT = passport.authenticate('jwt', { session: false });
