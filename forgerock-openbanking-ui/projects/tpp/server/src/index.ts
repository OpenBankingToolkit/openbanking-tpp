import * as express from 'express';
import * as cors from 'cors';
import * as cookieParser from 'cookie-parser';
import * as bodyParser from 'body-parser';
import * as path from 'path';

import { Iconfig } from './models';
import TppAsService from './as.service';
import TppMonitoringService from './monitoring.service';
import TppRsService from './rs.service';
import TppDirectoryService from './directory.service';
import TppJWKMSService from './jwkms.service';
import { TppMatlsBackendService } from './matls.service';

async function run() {
  const config = getDeploymentSetting();
  const { CORSWhitelist = ['https://dev.tpp.ui-integ.forgerock.financial:4207'] } = getDeploymentSetting();

  const PORT = process.env.PORT || 5000;
  const NODE_ENV = process.env.NODE_ENV;
  if (NODE_ENV !== 'production') {
    process.env['NODE_TLS_REJECT_UNAUTHORIZED'] = '0';
  }

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
  app.set('port', PORT);

  const monitoringService = new TppMonitoringService(config);
  const asService = new TppAsService(config);
  const rsService = new TppRsService(config);
  const directoryService = new TppDirectoryService(config);
  const jwkmsService = new TppJWKMSService(config);
  const matlsService = new TppMatlsBackendService(config);

  app.get('/config', (req: express.Request, res: express.Response) =>
    res.send({
      PORT,
      NODE_ENV,
      ...config
    })
  );

  app.get('/rest-domestic-payment', async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    try {
      const { CLIENT_ID, CLIENT_REDIRECT_URI, request_parameter } = await test();
      const redirection = await asService.getBankRedirectionUrl(CLIENT_ID, CLIENT_REDIRECT_URI, request_parameter);
      console.log({ redirection });
      res.json({
        redirection
      });
    } catch (error) {
      next(error);
    }
  });

  app.get('/domestic-payment', async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    try {
      const { CLIENT_ID, CLIENT_REDIRECT_URI, request_parameter } = await test();
      const redirection = await asService.getAMRedirectionUrl(CLIENT_ID, CLIENT_REDIRECT_URI, request_parameter);
      console.log({ redirection });
      res.json({
        redirection
      });
    } catch (error) {
      next(error);
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

  async function test() {
    const { id: MONITORING_UID } = await monitoringService.init();
    await monitoringService.registerUser();
    const {
      Data: { FinancialId: ASPSP_FINANCIAL_ID }
    } = await rsService.discovery();
    const { issuer: AS_ISSUER_ID, jwks_uri: AS_JWK_URI } = await asService.discovery();

    const { id: software_statement_id } = await directoryService.getCurrentSoftwareStatement(MONITORING_UID);
    const SSA_JWT = await directoryService.generateSSA(MONITORING_UID);
    // ICI
    const dynamic_registration_request = await jwkmsService.generateRegistrationJWT(
      MONITORING_UID,
      software_statement_id,
      SSA_JWT
    );
    const {
      client_id: CLIENT_ID,
      client_secret: CLIENT_SECRET,
      redirect_uris: [CLIENT_REDIRECT_URI]
    } = await matlsService.dynamicRegistration(MONITORING_UID, dynamic_registration_request);
    const { access_token } = await matlsService.clientCredential(MONITORING_UID, CLIENT_ID, CLIENT_SECRET);
    const { detachedSignature } = await jwkmsService.getDetachedSignature(MONITORING_UID, CLIENT_ID);
    const {
      Data: { ConsentId }
    } = await rsService.createDomesticPaymentConsent(
      MONITORING_UID,
      detachedSignature,
      access_token,
      ASPSP_FINANCIAL_ID
    );
    const request_parameter = await jwkmsService.generateRequestParamsForAuthRedirect(
      MONITORING_UID,
      AS_ISSUER_ID,
      CLIENT_ID,
      ConsentId,
      CLIENT_REDIRECT_URI,
      AS_JWK_URI
    );
    return {
      CLIENT_ID,
      CLIENT_REDIRECT_URI,
      request_parameter
    };
  }
}

function getDeploymentSetting(): Iconfig {
  try {
    return require(path.join(__dirname, 'deployment-settings.js'));
  } catch (error) {
    throw new Error('Missing configuration.');
  }
}

run();
