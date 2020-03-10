import * as express from 'express';
import * as cors from 'cors';
import * as cookieParser from 'cookie-parser';
import * as bodyParser from 'body-parser';
import * as expressSession from 'express-session';
import * as path from 'path';
import * as https from 'https';

import Bank, { IBank } from './db/Bank';
import { IUser, BankAccessToken } from './db/User';
import State, { IState } from './db/State';
import {
  discovery,
  generateRegistrationJWT,
  generateRegistrationRequestParams,
  getCurrentSoftwareStatement,
  dynamicRegistration,
  dynamicRegistrationUpdate,
  generateSSA,
  getRedirectionUrl,
  exchangeCode,
  generateClientCredential,
  getAccountsBalances,
  getAccounts,
  getAccountBalance,
  getAccountTransactions
} from './cdr.service';
import { passportInit, signIn, signJWTForUser, register, requireJWT, getUserFromToken } from './middleware/auth';
import { IAccount } from './models/index';

export interface Iconfig {
  CORSWhitelist: string[];
}

async function run() {
  const config = getDeploymentSetting();
  const { CORSWhitelist = ['https://dev.tpp.ui-integ.forgerock.financial:4207'] } = getDeploymentSetting();

  const PORT = process.env.PORT || 5000;
  const NODE_ENV = process.env.NODE_ENV;
  // if (NODE_ENV !== 'production') {
  //   process.env['NODE_TLS_REJECT_UNAUTHORIZED'] = '0';
  // }

  const app = express();

  app.use(cookieParser());
  app.use(bodyParser.json());
  app.use(
    cors({
      credentials: true,
      methods: 'GET, HEAD',
      origin:
        NODE_ENV === 'production'
          ? (origin: any, callback: (err: Error | null, allow?: boolean) => void) => {
              if (CORSWhitelist.indexOf(origin) !== -1) {
                callback(null, true);
              } else {
                callback(new Error('Not allowed by CORS'));
              }
            }
          : '*'
    })
  );
  app.use(
    expressSession({
      secret: 'cdr-tpp',
      resave: false,
      saveUninitialized: true,
      cookie: { secure: true }
    })
  );
  app.use(passportInit);
  app.set('port', PORT);

  app.post('/register', register, signJWTForUser);
  app.post('/login', signIn, signJWTForUser);

  app.get('/config', (req: express.Request, res: express.Response) =>
    res.send({
      PORT,
      NODE_ENV,
      ...config
    })
  );

  app.get('/user', requireJWT, async (req: express.Request, res: express.Response) => {
    try {
      const { email, firstName, lastName } = <IUser>req.user;
      res.json({ user: { email, firstName, lastName } });
    } catch (error) {
      res.status(500).json({
        message: error.message
      });
    }
  });

  app.get('/transactions/:bankId/:accountId', requireJWT, async (req: express.Request, res: express.Response) => {
    try {
      const user = <IUser>req.user;
      const accountId = req.params.accountId;
      const bankId = req.params.bankId;
      const bankAccessToken = user.banks.id(bankId);

      if (!bankAccessToken) {
        throw new Error('Need bank AccessToken');
      }
      const { accessToken } = bankAccessToken;
      const { cert, key, matlsAddress } = <IBank>await Bank.findById(bankId);
      const agent = new https.Agent({
        rejectUnauthorized: false,
        cert,
        key
      });

      const transactions = await getAccountTransactions(
        matlsAddress,
        {
          accessToken,
          accountId
        },
        agent
      );

      res.json({ transactions: transactions.data.transactions });
    } catch (error) {
      res.status(500).json({
        message: error.message
      });
    }
  });

  app.get('/balance/:bankId/:accountId', requireJWT, async (req: express.Request, res: express.Response) => {
    try {
      const user = <IUser>req.user;
      const accountId = req.params.accountId;
      const bankId = req.params.bankId;
      const bankAccessToken = user.banks.id(bankId);

      if (!bankAccessToken) {
        throw new Error('Need bank AccessToken');
      }
      const { accessToken } = bankAccessToken;
      const { cert, key, matlsAddress } = <IBank>await Bank.findById(bankId);
      const agent = new https.Agent({
        rejectUnauthorized: false,
        cert,
        key
      });

      const balance = await getAccountBalance(
        matlsAddress,
        {
          accessToken,
          accountId
        },
        agent
      );

      res.json({ balance });
    } catch (error) {
      res.status(500).json({
        message: error.message
      });
    }
  });

  app.get('/accounts', requireJWT, async (req: express.Request, res: express.Response) => {
    try {
      const user = <IUser>req.user;

      const accounts: IAccount[] = [];
      const banks: Partial<IBank>[] = [];
      for (let i = 0; i < user.banks.length; i++) {
        const { _id, accessToken } = user.banks[i];
        const bank = <IBank>await Bank.findById(_id);
        const { cert, key, matlsAddress } = bank;
        banks.push(filterBankProperties(bank));
        const agent = new https.Agent({
          rejectUnauthorized: false,
          cert,
          key
        });

        const accountsResponse = await getAccounts(
          matlsAddress,
          {
            accessToken
          },
          agent
        );

        for (let j = 0; j < accountsResponse.data.accounts.length; j++) {
          const account = accountsResponse.data.accounts[j];
          const balance = await getAccountBalance(
            matlsAddress,
            {
              accessToken,
              accountId: account.accountId
            },
            agent
          );

          accounts.push({
            bankId: _id,
            balance: balance.data,
            ...account
          });
        }
      }
      res.json({ banks, accounts });
    } catch (error) {
      res.status(500).json({
        message: error.message
      });
    }
  });

  app.post('/exchange-code', requireJWT, async (req: express.Request, res: express.Response) => {
    try {
      const user = <IUser>req.user;
      const stateId = req.body.state;
      const code = req.body.code;

      const state = <IState>await State.findById(stateId);

      if (!code) {
        throw new Error('Need code');
      }

      if (!state || !state.bankId || !state.userId) {
        throw new Error('cannot find state');
      }

      const { bankId, userId } = state;

      if (user._id.toString() !== userId) {
        throw new Error('Users do not match');
      }

      const { discoveryAddress, directoryAddress, jwkmsAddress, cert, key, clientId } = <IBank>(
        await Bank.findById(bankId)
      );

      if (!clientId) {
        throw new Error(
          'Your tpp needs to be registered. Use /register/:bankId GET with the following properties in the DB: discoveryAddress, directoryAddress, jwkmsAddress, cert, key'
        );
      }

      const { token_endpoint, issuer } = await discovery(discoveryAddress);

      console.log({ token_endpoint, issuer });

      const agent = new https.Agent({
        rejectUnauthorized: false,
        cert,
        key
      });

      const { id: software_statement_id, redirectUris: redirect_uris } = await getCurrentSoftwareStatement(
        directoryAddress,
        agent
      );

      console.log({ redirect_uris, software_statement_id });

      const client_assertion = await generateClientCredential(
        jwkmsAddress,
        {
          clientId,
          issuerId: issuer
        },
        agent
      );
      console.log({ client_assertion });

      const { access_token: access_token_with_consent } = await exchangeCode(
        token_endpoint,
        {
          redirect_uri: redirect_uris[0],
          client_assertion,
          code
        },
        agent
      );
      console.log({ access_token_with_consent });

      const userBankToken = user.banks.id(bankId);
      if (userBankToken) {
        userBankToken.remove();
      }
      user.banks.push(
        new BankAccessToken({
          _id: bankId,
          accessToken: access_token_with_consent
        })
      );
      await user.save();
      res.json({ success: true });
    } catch (error) {
      res.status(500).json({
        message: error.message
      });
    }
  });

  app.get('/register/:bankId', async (req: express.Request, res: express.Response) => {
    try {
      const bankId = req.params.bankId;

      if (!bankId) {
        throw new Error('need a bank ID ');
      }

      const bank = <IBank>await Bank.findById(bankId);
      const { discoveryAddress, directoryAddress, jwkmsAddress, cert, key, clientId, registrationAccessToken } = bank;
      const agent = new https.Agent({
        rejectUnauthorized: false,
        cert,
        key
      });

      // const { id: MONITORING_UID } = await testSS('https://monitoring.cdr.forgerock.financial');
      const { issuer, jwks_uri, authorization_endpoint, registration_endpoint } = await discovery(discoveryAddress);
      console.log({ issuer, jwks_uri, authorization_endpoint, registration_endpoint });
      const { id: software_statement_id, redirectUris: redirect_uris } = await getCurrentSoftwareStatement(
        directoryAddress,
        agent
      );
      console.log({ software_statement_id, redirect_uris });
      const SSA_JWT = await generateSSA(directoryAddress, agent);
      const dynamic_registration_request = await generateRegistrationJWT(
        jwkmsAddress,
        {
          software_statement_id,
          redirect_uris,
          SSA_JWT
        },
        agent
      );
      console.log({ dynamic_registration_request });

      let response;
      if (registrationAccessToken && clientId) {
        console.log('Update registration');
        response = await dynamicRegistrationUpdate(
          registration_endpoint + clientId,
          dynamic_registration_request,
          registrationAccessToken,
          agent
        );
      } else {
        console.log('New registration');
        response = await dynamicRegistration(registration_endpoint, dynamic_registration_request, agent);
      }

      const {
        client_id,
        redirect_uris: [redirect_uri],
        registration_access_token
      } = response;

      console.log({ client_id, redirect_uri, registration_access_token });

      bank.clientId = client_id;
      bank.registrationAccessToken = registration_access_token;
      await bank.save();

      res.json(`Bank ${client_id} registered`);
    } catch (error) {
      res.status(500).json({
        message: error.message
      });
    }
  });

  app.get('/banks/:bankId', async (req: express.Request, res: express.Response) => {
    try {
      const bankId = req.params.bankId;
      const token = req.query.token;

      if (!bankId || !token) {
        throw new Error('need a bank ID and a token');
      }

      // res.json({
      //   cert1: fs.readFileSync(path.join(__dirname, '28dbfa33d2fa10ddbf60f4cf043c4046b6110dd3.pem')).toString(),
      //   key1: fs.readFileSync(path.join(__dirname, '28dbfa33d2fa10ddbf60f4cf043c4046b6110dd3.key')).toString()
      // });

      const user = await getUserFromToken(token);

      if (!user) {
        throw new Error('cannot find user');
      }

      const { discoveryAddress, directoryAddress, jwkmsAddress, cert, key, clientId } = <IBank>(
        await Bank.findById(bankId)
      );

      if (!clientId) {
        throw new Error(
          'Your tpp needs to be registered. Use /register/:bankId GET with the following properties in the DB: discoveryAddress, directoryAddress, jwkmsAddress, cert, key'
        );
      }

      const agent = new https.Agent({
        rejectUnauthorized: false,
        cert,
        key
      });

      // const { id: MONITORING_UID } = await testSS('https://monitoring.cdr.forgerock.financial');
      const { issuer, jwks_uri, authorization_endpoint } = await discovery(discoveryAddress);
      console.log({ issuer, jwks_uri, authorization_endpoint });
      const { id: software_statement_id, redirectUris: redirect_uris } = await getCurrentSoftwareStatement(
        directoryAddress,
        agent
      );
      console.log({ software_statement_id, redirect_uris });
      const SSA_JWT = await generateSSA(directoryAddress, agent);
      const dynamic_registration_request = await generateRegistrationJWT(
        jwkmsAddress,
        {
          software_statement_id,
          redirect_uris,
          SSA_JWT
        },
        agent
      );
      console.log({ dynamic_registration_request });

      const state = <IState>await State.create({
        bankId,
        userId: user._id
      });
      const redirect_uri = redirect_uris[0];
      const scope =
        'openid accounts bank:accounts.basic:read bank:accounts.detail:read bank:payees:read openid bank:transactions:read common:customer.detail:read common:customer.basic:read bank:regular_payments:read';
      const request_parameter = await generateRegistrationRequestParams(
        jwkmsAddress,
        {
          client_id: clientId,
          jwks_uri,
          redirect_uri,
          issuer,
          scope,
          state: state._id
        },
        agent
      );
      console.log({ request_parameter });

      const redirection = getRedirectionUrl(
        authorization_endpoint,
        clientId,
        redirect_uri,
        scope,
        state._id,
        request_parameter
      );
      console.log({ redirection });
      res.redirect(redirection);
    } catch (error) {
      res.status(500).json({
        message: error.message
      });
    }
  });

  // app.post('/banks', requireJWT, async (req: express.Request, res: express.Response) => {
  //   try {
  //     const { id } = req.body;
  //     if (!id) {
  //       throw new Error('need a bank ID');
  //     }
  //     await Bank.findById(id);
  //     const user = <IUser>req.user;

  //     if (user.banks.indexOf(id) !== -1) {
  //       throw new Error('Bank already registered');
  //     } else {
  //       user.banks.push(id);
  //       await user.save();
  //       res.json(user);
  //     }
  //   } catch (error) {
  //     res.status(500).json({
  //       message: error.message
  //     });
  //   }
  // });

  app.get('/banks', requireJWT, async (req: express.Request, res: express.Response) => {
    try {
      Bank.find((error, banks: IBank[]) => {
        if (error) throw error;
        res.json({
          banks: banks.map(filterBankProperties)
        });
      });
    } catch (error) {
      res.send(error.message);
    }
  });

  app.use(function(err: any, req: express.Request, res: express.Response, next: express.NextFunction) {
    res.status(500).json({
      error: err.toString()
    });
  });

  app.listen(app.get('port'), () => {
    console.log(`Web server listening on port ${app.get('port')}`);
  });
}

function getDeploymentSetting(): Iconfig {
  try {
    return require(path.join(__dirname, 'deployment-settings.js'));
  } catch (error) {
    throw new Error('Missing configuration.');
  }
}

const filterBankProperties = ({ _id: id, name, logo }: IBank) => ({
  id,
  name,
  logo
});

run();
