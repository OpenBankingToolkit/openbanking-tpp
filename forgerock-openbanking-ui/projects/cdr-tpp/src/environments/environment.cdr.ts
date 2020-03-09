import { environment as devDefaultEnv } from './environment.dev.default';

export const environment = {
  ...devDefaultEnv,
  production: false,
  nodeBackend: 'https://dev.cdr-tpp.cdr.forgerock.financial:4208/api'
};
