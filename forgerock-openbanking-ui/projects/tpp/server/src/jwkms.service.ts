import fetch, { Response } from 'node-fetch';

import { Iconfig } from './models';

export default class TppJWKMSService {
  constructor(private conf: Iconfig) {}

  generateRegistrationJWT(MONITORING_UID: string, software_statement_id: string, SSA_JWT: string): Promise<string> {
    return fetch(`${this.conf.jwkmsBackend}/api/crypto/signClaims`, {
      method: 'POST',
      body: JSON.stringify({
        exp: new Date().getTime() / 1000 + 60 * 5,
        scope: 'openid accounts payments fundsconfirmations',
        redirect_uris: [this.conf.tppAppAddress + '/success'],
        grant_types: ['authorization_code', 'refresh_token', 'client_credentials'],
        response_types: ['code id_token'],
        subject_type: 'pairwise',
        software_statement: SSA_JWT,
        token_endpoint_auth_method: 'client_secret_basic',
        token_endpoint_auth_signing_alg: 'PS256',
        id_token_signed_response_alg: 'PS256',
        request_object_signing_alg: 'PS256',
        request_object_encryption_alg: 'RSA-OAEP-256',
        request_object_encryption_enc: 'A128CBC-HS256'
      }),
      headers: {
        issuerId: software_statement_id,
        'x-ob-monitoring': MONITORING_UID
      }
    }).then((res: Response) => {
      if (res.ok) return res.text();
      throw new Error(res.statusText);
    });
  }

  generateRequestParamsForAuthRedirect(
    MONITORING_UID: string,
    AS_ISSUER_ID: string,
    CLIENT_ID: string,
    ConsentId: string,
    CLIENT_REDIRECT_URI: string,
    AS_JWK_URI: string
  ) {
    return fetch(`${this.conf.jwkmsBackend}/api/crypto/signClaims`, {
      method: 'POST',
      body: JSON.stringify({
        aud: AS_ISSUER_ID,
        scope: 'openid accounts',
        iss: CLIENT_ID,
        claims: {
          id_token: {
            acr: {
              value: 'urn:openbanking:psd2:sca',
              essential: true
            },
            openbanking_intent_id: {
              value: ConsentId,
              essential: true
            }
          },
          userinfo: {
            openbanking_intent_id: {
              value: ConsentId,
              essential: true
            }
          }
        },
        response_type: 'code id_token',
        redirect_uri: CLIENT_REDIRECT_URI,
        state: '10d260bf-a7d9-444a-92d9-7b7a5f088208',
        exp: new Date().getTime() / 1000 + 60 * 5,
        nonce: '10d260bf-a7d9-444a-92d9-7b7a5f088208',
        client_id: CLIENT_ID
      }),
      headers: {
        'Content-Type': 'application/json',
        jwkUri: AS_JWK_URI,
        issuerId: CLIENT_ID,
        'x-ob-monitoring': MONITORING_UID
      }
    }).then((res: Response) => {
      if (res.ok) return res.text();
      throw new Error(res.statusText);
    });
  }

  getDetachedSignature(
    MONITORING_UID: string,
    CLIENT_ID: string
  ): Promise<{
    detachedSignature: string;
  }> {
    return fetch(`${this.conf.jwkmsBackend}/api/crypto/signPayloadToDetachedJwt`, {
      method: 'POST',
      body: '{}',
      headers: {
        'x-ob-monitoring': MONITORING_UID,
        issuerId: CLIENT_ID
      }
    }).then((res: Response) => {
      console.log(res);
      console.log(res.headers);
      if (res.ok) return res.json();
      throw new Error(res.statusText);
    });
  }
}
