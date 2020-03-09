import { environment as devDefaultEnv } from './environment.dev.default';

export const environment = {
  ...devDefaultEnv,
  production: false,
  nodeBackend: 'http://localhost:4208/api'
};
