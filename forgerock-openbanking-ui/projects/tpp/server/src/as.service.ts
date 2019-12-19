import fetch, { Response } from 'node-fetch';

import { Iconfig } from './models';

interface IDiscoveryResponse {
  version: string;
  issuer: string;
  authorization_endpoint: string;
  token_endpoint: string;
  userinfo_endpoint: string;
  introspection_endpoint: string;
  jwks_uri: string;
  registration_endpoint: string;
  scopes_supported: string[];
  response_types_supported: string[];
  grant_types_supported: string[];
  acr_values_supported: string[];
  subject_types_supported: string[];
  id_token_signing_alg_values_supported: string[];
  id_token_encryption_alg_values_supported: string[];
  id_token_encryption_enc_values_supported: string[];
  userinfo_signing_alg_values_supported: string[];
  userinfo_encryption_alg_values_supported: string[];
  userinfo_encryption_enc_values_supported: string[];
  request_object_signing_alg_values_supported: string[];
  request_object_encryption_alg_values_supported: string[];
  request_object_encryption_enc_values_supported: string[];
  token_endpoint_auth_methods_supported: string[];
  token_endpoint_auth_signing_alg_values_supported: string[];
  claims_supported: string[];
  claims_parameter_supported: boolean;
  request_parameter_supported: boolean;
  request_uri_parameter_supported: boolean;
  require_request_uri_registration: boolean;
}

export default class TppAsService {
  constructor(private conf: Iconfig) {}

  discovery(): Promise<IDiscoveryResponse> {
    return fetch(`${this.conf.asBackend}/oauth2/.well-known/openid-configuration`).then((res: Response) => {
      if (res.ok) return res.json();
      throw new Error(res.statusText);
    });
  }

  getAMRedirectionUrl(CLIENT_ID: string, CLIENT_REDIRECT_URI: string, request_parameter: string) {
    return `${
      this.conf.asBackend
    }/oauth2/authorize?response_type=code id_token&client_id=${CLIENT_ID}&state=10d260bf-a7d9-444a-92d9-7b7a5f088208&redirect_uri=${CLIENT_REDIRECT_URI}&nonce=10d260bf-a7d9-444a-92d9-7b7a5f088208&scope=openid accounts&request=${request_parameter}`;
  }

  getBankRedirectionUrl(CLIENT_ID: string, CLIENT_REDIRECT_URI: string, request_parameter: string) {
    return `${
      this.conf.bankAppAddress
    }/oauth2/authorize?response_type=code id_token&client_id=${CLIENT_ID}&state=10d260bf-a7d9-444a-92d9-7b7a5f088208&redirect_uri=${CLIENT_REDIRECT_URI}&nonce=10d260bf-a7d9-444a-92d9-7b7a5f088208&scope=openid accounts&request=${request_parameter}`;
  }
}
