import fetch, { Response } from 'node-fetch';
import * as qs from 'qs';

import { Iconfig } from './models';

interface IDynamicResponse {
  scopes: string[];
  scope: string;
  client_id: string;
  redirect_uris: string[];
  response_types: string[];
  grant_types: string[];
  application_type: string;
  client_name: string;
  jwks_uri: string;
  subject_type: string;
  id_token_signed_response_alg: string;
  id_token_encrypted_response_alg: string;
  id_token_encrypted_response_enc: string;
  userinfo_signed_response_alg: string;
  userinfo_encrypted_response_alg: string;
  userinfo_encrypted_response_enc: string;
  request_object_signing_alg: string;
  request_object_encryption_alg: string;
  request_object_encryption_enc: string;
  token_endpoint_auth_method: string;
  token_endpoint_auth_signing_alg: string;
  default_max_age: string;
  software_statement: string;
  client_secret: string;
  registration_access_token: string;
  registration_client_uri: string;
  client_secret_expires_at: string;
}

export class TppMatlsBackendService {
  constructor(private conf: Iconfig) {}

  dynamicRegistration(MONITORING_UID: string, dynamic_registration_request: string): Promise<IDynamicResponse> {
    return fetch(`${this.conf.matlsBackend}/open-banking/register/`, {
      method: 'POST',
      body: dynamic_registration_request,
      headers: {
        'Content-Type': 'application/jwt',
        'x-ob-monitoring': MONITORING_UID
      }
    }).then((res: Response) => {
      if (res.ok) return res.json();
      throw new Error(res.statusText);
    });
  }

  clientCredential(
    MONITORING_UID: string,
    CLIENT_ID: string,
    CLIENT_SECRET: string
  ): Promise<{
    access_token: string;
    expires_in: number;
    token_type: string;
    scope: string;
  }> {
    return fetch(`${this.conf.matlsBackend}/oauth2/access_token`, {
      method: 'POST',
      body: qs.stringify({ grant_type: 'client_credentials', scope: 'accounts payments' }),
      headers: {
        Authorization: `Basic ${Buffer.from(CLIENT_ID + ':' + CLIENT_SECRET).toString('base64')}`,
        'Content-Type': 'application/x-www-form-urlencoded',
        'x-ob-monitoring': MONITORING_UID
      }
    }).then((res: Response) => {
      if (res.ok) return res.json();
      throw new Error(res.statusText);
    });
  }
}
