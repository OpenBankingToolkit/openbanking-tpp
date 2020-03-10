import fetch, { Response } from 'node-fetch';
import * as https from 'https';
import * as qs from 'qs';
import { IAccountsResponse, ITransactionsResponse, IBalancesResponse, IBalanceResponse } from './models/cdr';

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

function getExpirationTimestamp() {
  return new Date().getTime() / 1000 + 60 * 5;
}

export function discovery(authServerAddress: string): Promise<IDiscoveryResponse> {
  return fetch(`${authServerAddress}/oauth2/.well-known/openid-configuration`).then((res: Response) => {
    if (res.ok) return res.json();
    throw new Error(res.statusText);
  });
}

export function generateRegistrationJWT(
  jwkmsBackend: string,
  options: {
    redirect_uris: string[];
    software_statement_id: string;
    SSA_JWT: string;
  },
  agent: https.Agent
): Promise<string> {
  return fetch(`${jwkmsBackend}/api/crypto/signClaims`, {
    method: 'POST',
    agent,
    body: JSON.stringify({
      exp: getExpirationTimestamp(),
      scope: 'openid accounts',
      redirect_uris: options.redirect_uris,
      grant_types: ['authorization_code', 'refresh_token', 'client_credentials'],
      response_types: ['code id_token'],
      subject_type: 'pairwise',
      software_statement: options.SSA_JWT,
      token_endpoint_auth_method: 'private_key_jwt',
      token_endpoint_auth_signing_alg: 'PS256',
      id_token_signed_response_alg: 'PS256',
      request_object_signing_alg: 'PS256',
      request_object_encryption_alg: 'RSA-OAEP-256',
      request_object_encryption_enc: 'A128CBC-HS256'
    }),
    headers: {
      issuerId: options.software_statement_id
    }
  }).then((res: Response) => {
    if (res.ok) return res.text();
    throw new Error(res.statusText);
  });
}

export function generateRegistrationRequestParams(
  jwkmsBackend: string,
  options: {
    redirect_uri: string;
    issuer: string;
    client_id: string;
    jwks_uri: string;
    scope: string;
    state: string;
  },
  agent: https.Agent
): Promise<string> {
  console.log(
    options,
    JSON.stringify({
      exp: getExpirationTimestamp(),
      aud: options.issuer,
      scope: options.scope,
      iss: options.client_id,
      claims: {
        id_token: {
          acr: {
            value: 'urn:openbanking:psd2:sca',
            essential: true
          }
        }
      },
      response_type: 'code id_token',
      redirect_uri: options.redirect_uri,
      state: options.state,
      nonce: options.state,
      client_id: options.client_id
    })
  );
  return fetch(`${jwkmsBackend}/api/crypto/signClaims`, {
    method: 'POST',
    agent,
    body: JSON.stringify({
      exp: getExpirationTimestamp(),
      aud: options.issuer,
      scope: options.scope,
      iss: options.client_id,
      claims: {
        id_token: {
          acr: {
            value: 'urn:openbanking:psd2:sca',
            essential: true
          }
        }
      },
      response_type: 'code id_token',
      redirect_uri: options.redirect_uri,
      state: options.state,
      nonce: options.state,
      client_id: options.client_id
    }),
    headers: {
      issuerId: options.client_id,
      jwkUri: options.jwks_uri
    }
  }).then((res: Response) => {
    if (res.ok) return res.text();
    throw new Error(res.statusText);
  });
}

export function getRedirectionUrl(
  authorization_endpoint: string,
  CLIENT_ID: string,
  CLIENT_REDIRECT_URI: string,
  scope: string,
  state: string,
  request_parameter: string
) {
  return `${authorization_endpoint}?response_type=code%20id_token&client_id=${CLIENT_ID}&state=${state}&nonce=${state}&redirect_uri=${CLIENT_REDIRECT_URI}&scope=${encodeURI(
    scope
  )}&request=${request_parameter}`;
}

interface IInitResponse {
  id: string;
  name: string;
  logoUri: string;
  mode: string;
  roles: string[];
  status: string;
  redirectUris: string[];
  applicationId: string;
}

export function testSS(monitoringAddress: string): Promise<IInitResponse> {
  return fetch(`${monitoringAddress}/api/test/software-statement/`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  }).then((res: Response) => {
    if (res.ok) return res.json();
    throw new Error(res.statusText);
  });
}

interface ICurrentSoftwareStatement {
  id: string;
  name: string;
  logoUri: string;
  mode: string;
  roles: string[];
  status: string;
  redirectUris: string[];
  applicationId: string;
}

export function getCurrentSoftwareStatement(
  directoryAddress: string,
  agent: https.Agent
): Promise<ICurrentSoftwareStatement> {
  return fetch(`${directoryAddress}/api/software-statement/current/`, {
    method: 'GET',
    agent
  }).then((res: Response) => {
    if (res.ok) return res.json();
    throw new Error(res.statusText);
  });
}

export function generateSSA(directoryAddress: string, agent: https.Agent): Promise<string> {
  return fetch(`${directoryAddress}/api/software-statement/current/ssa`, {
    method: 'POST',
    agent
  }).then((res: Response) => {
    if (res.ok) return res.text();
    throw new Error(res.statusText);
  });
}

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
export function dynamicRegistrationUpdate(
  registrationAddress: string,
  dynamic_registration_request: string,
  registration_access_token: string,
  agent: https.Agent
): Promise<IDynamicResponse> {
  console.log({ registrationAddress });
  return fetch(registrationAddress, {
    method: 'PUT',
    body: dynamic_registration_request,

    agent,
    headers: {
      'Content-Type': 'application/jwt',
      Authorization: `Bearer ${registration_access_token}`
    }
  }).then((res: Response) => {
    if (res.ok) return res.json();
    throw new Error(res.statusText);
  });
}

export function dynamicRegistration(
  registrationAddress: string,
  dynamic_registration_request: string,
  agent: https.Agent
): Promise<IDynamicResponse> {
  return fetch(registrationAddress, {
    method: 'POST',
    body: dynamic_registration_request,
    agent,
    headers: {
      'Content-Type': 'application/jwt'
    }
  }).then((res: Response) => {
    if (res.ok) return res.json();
    throw new Error(res.statusText);
  });
}

export function exchangeCode(
  tokenEndpoint: string,
  options: {
    redirect_uri: string;
    client_assertion: string;
    code: string;
  },
  agent: https.Agent
): Promise<{
  access_token: string;
}> {
  console.log(
    qs.stringify({
      grant_type: 'authorization_code',
      client_assertion_type: 'urn:ietf:params:oauth:client-assertion-type:jwt-bearer',
      code: options.code,
      redirect_uri: options.redirect_uri,
      client_assertion: options.client_assertion
    })
  );
  return fetch(tokenEndpoint, {
    method: 'POST',
    agent,
    body: qs.stringify({
      grant_type: 'authorization_code',
      client_assertion_type: 'urn:ietf:params:oauth:client-assertion-type:jwt-bearer',
      code: options.code,
      redirect_uri: options.redirect_uri,
      client_assertion: options.client_assertion
    }),
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  }).then((res: Response) => {
    if (res.ok) return res.json();
    console.log(res);
    throw new Error(res.statusText);
  });
}

export function generateClientCredential(
  jwkmsBackend: string,
  options: {
    clientId: string;
    issuerId: string;
  },
  agent: https.Agent
): Promise<string> {
  console.log(
    JSON.stringify({
      sub: options.clientId,
      aud: options.issuerId,
      exp: getExpirationTimestamp()
    })
  );
  return fetch(`${jwkmsBackend}/api/crypto/signClaims`, {
    method: 'POST',
    agent,
    body: JSON.stringify({
      sub: options.clientId,
      aud: options.issuerId,
      exp: getExpirationTimestamp()
    }),
    headers: {
      'Content-Type': 'application/json',
      issuerId: options.clientId
    }
  }).then((res: Response) => {
    if (res.ok) return res.text();
    throw new Error(res.statusText);
  });
}

export function getAccounts(
  jwkmsBackend: string,
  options: {
    accessToken: string;
  },
  agent: https.Agent
): Promise<IAccountsResponse> {
  return fetch(`${jwkmsBackend}/banking/accounts?page=0`, {
    method: 'GET',
    agent,
    headers: {
      'Content-Type': 'application/json',
      'x-v': '1',
      Accept: 'application/json',
      Authorization: `Bearer ${options.accessToken}`
    }
  }).then((res: Response) => {
    if (res.ok) return res.json();
    throw new Error(res.statusText);
  });
}

export function getAccountTransactions(
  jwkmsBackend: string,
  options: {
    accessToken: string;
    accountId: string;
  },
  agent: https.Agent
): Promise<ITransactionsResponse> {
  return fetch(`${jwkmsBackend}/banking/accounts/${options.accountId}/transactions?page=0`, {
    method: 'GET',
    agent,
    headers: {
      'Content-Type': 'application/json',
      'x-v': '1',
      Accept: 'application/json',
      Authorization: `Bearer ${options.accessToken}`
    }
  }).then((res: Response) => {
    if (res.ok) return res.json();
    throw new Error(res.statusText);
  });
}

export function getAccountBalance(
  jwkmsBackend: string,
  options: {
    accessToken: string;
    accountId: string;
  },
  agent: https.Agent
): Promise<IBalanceResponse> {
  return fetch(`${jwkmsBackend}/banking/accounts/${options.accountId}/balance?page=0`, {
    method: 'GET',
    agent,
    headers: {
      'Content-Type': 'application/json',
      'x-v': '1',
      Accept: 'application/json',
      Authorization: `Bearer ${options.accessToken}`
    }
  }).then((res: Response) => {
    console.log(res);
    if (res.ok) return res.json();
    throw new Error(res.statusText);
  });
}

export function getAccountsBalances(
  jwkmsBackend: string,
  options: {
    accessToken: string;
  },
  agent: https.Agent
): Promise<IBalancesResponse> {
  return fetch(`${jwkmsBackend}/banking/accounts/balances?page=0`, {
    method: 'GET',
    agent,
    headers: {
      'Content-Type': 'application/json',
      'x-v': '1',
      Accept: 'application/json',
      Authorization: `Bearer ${options.accessToken}`
    }
  }).then((res: Response) => {
    console.log(res);
    if (res.ok) return res.json();
    throw new Error(res.statusText);
  });
}
