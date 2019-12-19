import fetch, { Response } from 'node-fetch';

import { Iconfig } from './models';

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

interface IRegisterUserResponse {
  username: string;
  userPassword: string;
}

export default class TppMonitoringService {
  constructor(private conf: Iconfig) {}

  init(): Promise<IInitResponse> {
    return fetch(`${this.conf.monitoringBackend}/api/test/software-statement/`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    }).then((res: Response) => {
      if (res.ok) return res.json();
      throw new Error(res.statusText);
    });
  }

  registerUser(): Promise<IRegisterUserResponse> {
    return fetch(`${this.conf.monitoringBackend}/api/test/user/`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    }).then((res: Response) => {
      if (res.ok) return res.json();
      throw new Error(res.statusText);
    });
  }
}
