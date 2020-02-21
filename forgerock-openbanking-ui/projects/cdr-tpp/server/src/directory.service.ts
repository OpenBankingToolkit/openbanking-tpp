import fetch, { Response } from 'node-fetch';

import { Iconfig } from './models';

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

export default class TppDirectoryService {
  constructor(private conf: Iconfig) {}

  getCurrentSoftwareStatement(MONITORING_UID: string): Promise<ICurrentSoftwareStatement> {
    return fetch(`${this.conf.directoryBackend}/api/software-statement/current/`, {
      method: 'GET',
      headers: {
        'x-ob-monitoring': MONITORING_UID
      }
    }).then((res: Response) => {
      if (res.ok) return res.json();
      throw new Error(res.statusText);
    });
  }

  generateSSA(MONITORING_UID: string): Promise<string> {
    return fetch(`${this.conf.directoryBackend}/api/software-statement/current/ssa`, {
      method: 'POST',
      headers: {
        'x-ob-monitoring': MONITORING_UID
      }
    }).then((res: Response) => {
      if (res.ok) return res.text();
      throw new Error(res.statusText);
    });
  }
}
