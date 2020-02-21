import { environment as defaultEnv } from './environment.default';

export const environment = {
  ...defaultEnv,
  production: true,
  nodeBackend: 'https://node.tpp.ob.forgerock.financial'
};
