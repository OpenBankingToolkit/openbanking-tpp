import fetch, { Response } from 'node-fetch';

import { Iconfig } from './models';

interface IDiscoveryResponse {
  Data: {
    FinancialId: string;
    PaymentInitiationAPI: any[];
    AccountAndTransactionAPI: any[];
    FundsConfirmationAPI: any[];
    EventNotificationAPI: any[];
  };
}

interface createDomesticPaymentConsentResponse {
  Data: Data;
  Risk: Risk;
  Links: Links;
  Meta: {};
}

interface Links {
  Self: string;
}

interface Risk {
  PaymentContextCode: string;
  MerchantCategoryCode: string;
  MerchantCustomerIdentification: string;
  DeliveryAddress: DeliveryAddress;
}

interface DeliveryAddress {
  AddressLine: string[];
  StreetName: string;
  BuildingNumber: string;
  PostCode: string;
  TownName: string;
  CountrySubDivision: string[];
  Country: string;
}

interface Data {
  ConsentId: string;
  CreationDateTime: string;
  Status: string;
  StatusUpdateDateTime: string;
  Initiation: Initiation;
}

interface Initiation {
  InstructionIdentification: string;
  EndToEndIdentification: string;
  InstructedAmount: InstructedAmount;
  CreditorAccount: CreditorAccount;
  RemittanceInformation: RemittanceInformation;
}

interface RemittanceInformation {
  Unstructured: string;
  Reference: string;
}

interface CreditorAccount {
  SchemeName: string;
  Identification: string;
  Name: string;
  SecondaryIdentification: string;
}

interface InstructedAmount {
  Amount: string;
  Currency: string;
}

export default class TppRsService {
  constructor(private conf: Iconfig) {}

  discovery(): Promise<IDiscoveryResponse> {
    return fetch(`${this.conf.rsBackend}/open-banking/discovery`).then((res: Response) => {
      if (res.ok) return res.json();
      throw new Error(res.statusText);
    });
  }

  createDomesticPaymentConsent(
    MONITORING_UID: string,
    detachedSignature: string,
    access_token: string,
    ASPSP_FINANCIAL_ID: string
  ): Promise<createDomesticPaymentConsentResponse> {
    return fetch(`${this.conf.rsBackend}/open-banking/v3.1.1/pisp/domestic-payment-consents`, {
      method: 'POST',
      body: JSON.stringify({
        Data: {
          Initiation: {
            InstructionIdentification: 'ACME412',
            EndToEndIdentification: 'FRESCO.21302.GFX.20',
            InstructedAmount: {
              Amount: '165.88',
              Currency: 'GBP'
            },
            CreditorAccount: {
              SchemeName: 'UK.OBIE.SortCodeAccountNumber',
              Identification: '08080021325698',
              Name: 'ACME Inc',
              SecondaryIdentification: '0002'
            },
            RemittanceInformation: {
              Reference: 'FRESCO-101',
              Unstructured: 'Internal ops code 5120101'
            }
          }
        },
        Risk: {
          PaymentContextCode: 'EcommerceGoods',
          MerchantCategoryCode: '5967',
          MerchantCustomerIdentification: '053598653254',
          DeliveryAddress: {
            AddressLine: ['Flat 7', 'Acacia Lodge'],
            StreetName: 'Acacia Avenue',
            BuildingNumber: '27',
            PostCode: 'GU31 2ZZ',
            TownName: 'Sparsholt',
            CountrySubDivision: ['Wessex'],
            Country: 'UK'
          }
        }
      }),
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${access_token}`,
        'x-idempotency-key': String(Date.now()),
        'x-jws-signature': detachedSignature,
        'x-fapi-financial-id': ASPSP_FINANCIAL_ID,
        'x-fapi-customer-last-logged-time': 'Sun, 10 Sep 2017 19:43:31 UTC',
        'x-fapi-customer-ip-address': '104.25.212.99',
        'x-fapi-interaction-id': '93bac548-d2de-4546-b106-880a5018460d',
        Accept: 'application/json',
        'x-ob-monitoring': MONITORING_UID
      }
    }).then((res: Response) => {
      console.log(res.headers);
      if (res.ok) return res.json();
      throw new Error(res.statusText);
    });
  }
}
