[<img src="https://raw.githubusercontent.com/ForgeRock/forgerock-logo-dev/master/Logo-fr-dev.png" align="right" width="220px"/>](https://developer.forgerock.com/)

| |Current Status|
|---|---|
|Build|[![Build Status](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Factions-badge.atrox.dev%2FOpenBankingToolkit%2Fopenbanking-tpp%2Fbadge%3Fref%3Dmaster&style=flat)](https://actions-badge.atrox.dev/OpenBankingToolkit/openbanking-tpp/goto?ref=master)|
|Code coverage|[![codecov](https://codecov.io/gh/OpenBankingToolkit/openbanking-tpp/branch/master/graph/badge.svg)](https://codecov.io/gh/OpenBankingToolkit/openbanking-tpp)
|Bintray|[![Bintray](https://img.shields.io/bintray/v/openbanking-toolkit/OpenBankingToolKit/openbanking-tpp.svg?maxAge=2592000)](https://bintray.com/openbanking-toolkit/OpenBankingToolKit/openbanking-tpp)|
|License|![license](https://img.shields.io/github/license/ACRA/acra.svg)|

**_This repository is part of the Open Banking Tool kit. If you just landed to that repository looking for our tool kit,_
_we recommend having a first read to_ https://github.com/OpenBankingToolkit/openbanking-toolkit**

ForgeRock OpenBanking TPP
=========================


## CDR demo

![Mar-09-2020 13-22-51](https://user-images.githubusercontent.com/1388706/76212677-4a5c1d80-6209-11ea-9446-f072f693e261.gif)

### Run locally

```
cd forgerock-openbanking-ui 
docker-compose up
```

Open `http://localhost:4208/` to use the app.

### Register moneywatch app with a new bank

The moneywatch app is already registered at `https://directory.cdr.forgerock.financial/` (to use this directory you will need [“Fake LE Intermediate X1”](https://letsencrypt.org/docs/staging-environment/) certificate installed). 

The default tpp data lives in `forgerock-openbanking-ui/mongo/banks.json`.

To connect the moneywatch app to another bank, you will need the following information:

```
  "_id": "<YOUR_ID>", 
  "name": "<YOUR_BANK_NAME>", 
  "directoryAddress": "<directoryAddress>", // e.g "https://matls.service.directory.cdr.forgerock.financial"
  "discoveryAddress": "<discoveryAddress>", // e.g "https://as.aspsp.cdr.forgerock.financial"
  "jwkmsAddress": "<jwkmsAddress>", // e.g "https://jwkms.cdr.forgerock.financial"
  "matlsAddress": "<matlsAddress>", // e.g "https://matls.rs.aspsp.cdr.forgerock.financial"
  "key": "<TRANSPORT_KEY>", // needs to be in one line using `\n` see `forgerock-openbanking-ui/mongo/banks.json`
  "cert": "<TRANSPORT_CERT>", // needs to be in one line using `\n` see `forgerock-openbanking-ui/mongo/banks.json`
  "registrationAccessToken": "", // if registered (optional)
  "clientId": "", // if registered (optional)
```

Open the DB with `https://robomongo.org/` or similar and add the new item in the `banks` collection.

if you are missing `registrationAccessToken` and `clientId` you can run `http://localhost:5000/register/<YOUR_ID>` to populate those properties for you.

### Register

`http://localhost:4208/register`

### Login

`http://localhost:4208/login`

A default account is created (`demo@forgerock.com:changeme`) but you can create your own through the app or change the user in directly the db ``forgerock-openbanking-ui/mongo/users.json``




