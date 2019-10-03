[<img src="https://raw.githubusercontent.com/ForgeRock/forgerock-logo-dev/master/forgerock-logo-dev.png" align="right" width="220px"/>](https://developer.forgerock.com/)

ForgeRock OpenBanking Analytics
========================

Analytics is a high level monitoring application dedicated to Open Banking APIs.
It gives you insight of what is happening in your Open Banking eco-system from a API usage and performance.

It provides high level Key Point Indicators (KPIs). What we mean by this is that it won't provide low level KPIs like Grafana and Prometheus.
Analytics uses basic metrics to compute high level KPIs.
You can see this Open Banking analytics at the same level of KPIs than Google analytics.

With this application, you will be able to take decisions base on how your Open Banking APIs are evolving.

# Demo 
Probably the best is that you test it by yourself.
We got an instance available here:

https://analytics.demo.forgerock.financial/

You can use:
* login: `demo`
* password: `changeit`


# Features

* PDF exports of specific metrics
* Real time KPIs rendering
* Date filtering
* Export raw data in CSV
* Export list of TPPs in CSV

## KPIs

Analytics display KPIs, which we organised in 5 mains category:
* General Service Usage
* TPP
* Security
* Read/Write APIs
* Directory
* JWKMS

### General Service Usage

It's the dashboard, providing global KPIs from the overall Open Banking eco-system

#### KPIs

* Number of new PSUs
* Number of new TPPs
* Number of user sessions
* Number of endpoint calls by response status
* Number of endpoint calls by Open Banking API versions
* Number of endpoint calls by Open Banking API types
* Number of requests to any endpoints of the sandbox, distributed by week days
* Response times performances to any endpoints
* APIs performances of the overall sandbox for this period


### TPP

KPIs usages from a TPP point of view. You will get the list of TPPs and how they use/experience the Open Banking APIs

* TPP Origin directory
* TPP Roles
* Number of requests to the Dynamic registration endpoint
* List of TPPs
* APIs performances per TPP


### Security
Some APIs are more related to the security aspect of Open Banking. 

#### KPIs

* Number of access token generated
* Number of Id Token generated
* Number of requests to the /authorise endpoint
* Response times performances of the /authorise endpoint
* Number of requests to the /access_token endpoint
* Response times performances of the /access_token endpoint
* Security APIs performances


### Read/Write APIs
The KPIs organised as per the Open Banking UK standard.

#### Accounts KPIs
* Number of endpoint calls by Open Banking API versions
* New account consents by consent type
* Account access consent activities by status
* Accounts APIs performances


#### Payments KPIs

* Number of endpoint calls by Open Banking API versions
* New payments consents by consent type
* Number of payments confirmation of funds by API
* Each different consent status by type of payments
* Payments APIs performances


#### Confirmation of funds KPIs

* Number of endpoint calls by Open Banking API versions
* New confirmation of funds consents by consent type
* Confirmation of Funds consents activities by status
* Confirmation of funds APIs performances


#### Event notifications

* Number of endpoint calls by Open Banking API versions
* Number of events notification created
* Number of notifications send to TPPss
* Events APIs performances

### Directory

KPIs related to your directory. You may not have one and therefore this section is not relevant for you.
We do also provide a directory capability, that you can find here: https://github.com/ForgeCloud/ob-directory

#### KPIs

* Number of new organisations
* Number of new software statements
* Number of new SSA
* Number of downloading keys


### JWKMS
KPIs related to your centralised JWKMS. The JWKMS application is available here: https://github.com/ForgeCloud/ob-jwkms
The JWKMS is an optional application. This section of analytics is therefore not relevant if you don't have a centralised JWKMS


#### KPIs
* Number of JWTS generated
* Number of JWTS/Detached signature validated by the JWKMS
* JWKMS APIs performances


# Deployment

We provide two docker images: One of the UI and one for the backend.

If you like, we offer the backend as a spring service side and client side. 

## Service side Spring dependency

By doing your own spring boot wrapper, you will be able to customise the APIs or even add new one.

### Include the dependencies

For Apache Maven:

```
<dependency>
    <groupId>com.forgerock.openbanking.analytics</groupId>
    <artifactId>forgerock-openbanking-analytics-server</artifactId>
</dependency>
```

For Gradle:

```
compile 'com.forgerock.openbanking.jwkms:forgerock-openbanking-analytics-server'
```

## Client side Spring dependency

Client side SDK to push metrics to the analytics server. Use this dependency into your RS, AS or gateway, to push metrics
easily to the analytics server.


### Include the dependencies

For Apache Maven:

```
<dependency>
    <groupId>com.forgerock.openbanking.analytics</groupId>
    <artifactId>forgerock-openbanking-analytics-client</artifactId>
</dependency>
```

For Gradle:

```
compile 'com.forgerock.openbanking.analytics:forgerock-openbanking-analytics-client'
```



### How to push metrics to the analytics server?

The analytics server has REST APIs, allowing your other services to push metrics directly to it. 
Those APIs are convenient as you can easily integrate them inside your existent Open Banking eco-system.

### Do I need to use all the ForgeRock Open Banking platform to have analytics?

No, analytics is a separated apps that offers REST APIs. It can therefore integrate with any kind of Open Banking solutions.