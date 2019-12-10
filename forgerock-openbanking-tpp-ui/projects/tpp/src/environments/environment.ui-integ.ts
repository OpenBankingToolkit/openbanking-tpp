import { environment as devDefaultEnv } from './environment.dev.default';

export const environment = {
  ...devDefaultEnv,
  production: false,
  nodeBackend: 'https://dev.tpp.ui-integ.forgerock.financial:4207/api'
};
