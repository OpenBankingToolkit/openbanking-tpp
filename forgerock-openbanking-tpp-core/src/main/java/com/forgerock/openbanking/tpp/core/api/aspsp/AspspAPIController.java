/**
 * Copyright 2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package com.forgerock.openbanking.tpp.core.api.aspsp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.openbanking.constants.OIDCConstants;
import com.forgerock.openbanking.constants.OpenBankingConstants;
import com.forgerock.openbanking.jwt.services.CryptoApiClient;
import com.forgerock.openbanking.model.SoftwareStatement;
import com.forgerock.openbanking.model.oidc.OIDCRegistrationRequest;
import com.forgerock.openbanking.model.oidc.OIDCRegistrationResponse;
import com.forgerock.openbanking.tpp.config.AspspConfiguration;
import com.forgerock.openbanking.tpp.core.configuration.TppConfiguration;
import com.forgerock.openbanking.tpp.core.model.directory.Aspsp;
import com.forgerock.openbanking.tpp.core.model.directory.Organisation;
import com.forgerock.openbanking.tpp.core.model.directory.User;
import com.forgerock.openbanking.tpp.core.repository.AspspConfigurationMongoRepository;
import com.forgerock.openbanking.tpp.core.services.aspsp.as.ASDiscoveryService;
import com.forgerock.openbanking.tpp.core.services.aspsp.as.ASPSPASRegistrationService;
import com.forgerock.openbanking.tpp.core.services.aspsp.rs.RSDiscoveryService;
import com.forgerock.openbanking.tpp.core.services.directory.DirectoryService;
import com.forgerock.openbanking.tpp.core.services.directory.UserService;
import com.forgerock.openbanking.tpp.model.openbanking.discovery.OIDCDiscoveryResponse;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.discovery.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.Duration;
import java.util.*;

import static com.forgerock.openbanking.constants.OpenBankingConstants.SSAClaims.SOFTWARE_ID;

@Controller
public class AspspAPIController implements AspspAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(AspspAPIController.class);

    @Autowired
    private ASDiscoveryService asDiscoveryService;
    @Autowired
    private ASPSPASRegistrationService aspspAsRegistrationService;
    @Autowired
    private CryptoApiClient cryptoApiClient;
    @Autowired
    private AspspConfigurationMongoRepository aspspConfigurationRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TppConfiguration tppConfiguration;
    @Autowired
    private RSDiscoveryService rsDiscoveryService;
    @Autowired
    private DirectoryService directoryService;


    @Resource(name = "userServiceAsForgeRockApp")
    private UserService userService;
    @Value("${spring.application.name}")
    public String applicationName;

    @Override
    public ResponseEntity<List<AspspConfiguration>> getASPSPs() {
        return ResponseEntity.ok(aspspConfigurationRepository.findAll());
    }

    @Override
    public ResponseEntity registerToAllAspsps() throws ParseException {
        SoftwareStatement softwareStatement = getCurrentSoftwareStatement();
        String ssa = directoryService.generateSSA(softwareStatement);
        SignedJWT ssaJws = SignedJWT.parse(ssa);

        Set<AspspConfiguration> aspspConfigurations = new HashSet<>();
        for(Aspsp aspsp : directoryService.getAPSPSPs()) {
            OIDCDiscoveryResponse oidcDiscoveryResponse = asDiscoveryService.discovery(aspsp.getAsDiscoveryEndpoint());
            aspspAsRegistrationService.unregister(oidcDiscoveryResponse.getRegistrationEndpoint());
            aspspConfigurations.add(registerToAspsp(aspsp, ssaJws));
        }
        aspspConfigurations.remove(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(aspspConfigurations);
    }

    private AspspConfiguration registerToAspsp(
            Aspsp aspsp, SignedJWT ssaJws
    ) throws ParseException {
        LOGGER.debug("Received a registration request for an ASPSP: name '{}', logo '{}', financialID {}," +
                " oidcRootEndpoint {}, discoveryEndpoint {}", aspsp
        );

        LOGGER.debug("Call the OIDC discovery endpoint {}", aspsp.getAsDiscoveryEndpoint());
        OIDCDiscoveryResponse oidcDiscoveryResponse = asDiscoveryService.discovery(aspsp.getAsDiscoveryEndpoint());
        String registrationEndpoint = oidcDiscoveryResponse.getRegistrationEndpoint();
        LOGGER.debug("The OIDC registration endpoint: {}", oidcDiscoveryResponse.getRegistrationEndpoint());


        String registrationRequest = generateRegistrationRequest(oidcDiscoveryResponse, generateOIDCRegistrationRequest(oidcDiscoveryResponse), ssaJws);

        LOGGER.debug("The registration request generated : {}", registrationRequest);

        OIDCRegistrationResponse oidcRegistrationResponse = aspspAsRegistrationService.register(
                registrationEndpoint, registrationRequest);
        LOGGER.debug("We are successfully registered : {}", oidcRegistrationResponse);

        LOGGER.debug("We call the RS discovery endpoint : {}", aspsp.getRsDiscoveryEndpoint());
        OBDiscoveryResponse rsDiscovery = rsDiscoveryService.discovery(aspsp.getRsDiscoveryEndpoint());
        LOGGER.debug("The RS discovery response : {}", rsDiscovery);

        Optional<OBDiscoveryAPI<OBDiscoveryAPILinks>> paymentInitiationAPI = rsDiscovery.getData().getPaymentInitiationAPI(tppConfiguration.getVersion());
        Optional<OBDiscoveryAPI<OBDiscoveryAPILinks>> accountAndTransactionAPI = rsDiscovery.getData().getAccountAndTransactionAPI(tppConfiguration.getVersion());

        if (!paymentInitiationAPI.isPresent()
                || !accountAndTransactionAPI.isPresent()) {
            LOGGER.warn("RS doesn't implement version '{}' of the Open Banking standard.", tppConfiguration.getVersion());
            return null;
        }

        LOGGER.debug("Register the ASPSP configuration");
        AspspConfiguration aspspConfiguration = new AspspConfiguration();
        aspspConfiguration.setDiscoveryEndpoint(aspsp.getRsDiscoveryEndpoint());
        aspspConfiguration.setOidcDiscoveryResponse(oidcDiscoveryResponse);
        aspspConfiguration.setName(aspsp.getName());
        aspspConfiguration.setLogo(aspsp.getLogoUri());
        aspspConfiguration.setFinancialId(aspsp.getFinancialId());
        aspspConfiguration.setSsa(ssaJws.serialize());
        aspspConfiguration.setRegistrationEndpoint(registrationEndpoint);
        aspspConfiguration.setOidcRegistrationResponse(oidcRegistrationResponse);
        aspspConfiguration.setDiscoveryAPILinksPayment((OBDiscoveryAPILinksPayment1) paymentInitiationAPI.get().getLinks());
        aspspConfiguration.setDiscoveryAPILinksAccount((OBDiscoveryAPILinksAccount1) accountAndTransactionAPI.get().getLinks());

        return aspspConfigurationRepository.save(aspspConfiguration);
    }

    @Override
    public ResponseEntity unregisterAll(){
        for (AspspConfiguration aspspConfiguration : aspspConfigurationRepository.findAll()) {
            aspspAsRegistrationService.unregister(aspspConfiguration);
        }
        return ResponseEntity.ok().body("ASPSPs unregistered successfully");
    }

    private OIDCRegistrationRequest generateOIDCRegistrationRequest(OIDCDiscoveryResponse oidcDiscoveryResponse) {

        //TODO verify that the OIDC provider supports the features we are asking for
        OIDCRegistrationRequest oidcRegistrationRequest = new OIDCRegistrationRequest();

        oidcRegistrationRequest.setScopes(
                Arrays.asList(OpenBankingConstants.Scope.OPENID,
                        OpenBankingConstants.Scope.ACCOUNTS,
                        OpenBankingConstants.Scope.PAYMENTS,
                        OpenBankingConstants.Scope.FUNDS_CONFIRMATIONS));
        oidcRegistrationRequest.setRedirectUris(Arrays.asList(tppConfiguration.getAispRedirectUri(), tppConfiguration.getPispRedirectUri()));

        oidcRegistrationRequest.setGrantTypes(Arrays.asList(OIDCConstants.GrantType.AUTHORIZATION_CODE.type,
                OIDCConstants.GrantType.REFRESH_TOKEN.type,
                OIDCConstants.GrantType.CLIENT_CREDENTIAL.type));

        oidcRegistrationRequest.setResponseTypes(Arrays.asList(
                OIDCConstants.ResponseType.CODE + " " + OIDCConstants.ResponseType.ID_TOKEN));
        oidcRegistrationRequest.setApplicationType(OpenBankingConstants.RegistrationTppRequestClaims.APPLICATION_TYPE_WEB);

        oidcRegistrationRequest.setRedirectUris(Arrays.asList(tppConfiguration.getPispRedirectUri(),
                tppConfiguration.getAispRedirectUri()));

        oidcRegistrationRequest.setClientName("ForgeRock TPP");
        oidcRegistrationRequest.setTokenEndpointAuthMethod(OIDCConstants.TokenEndpointAuthMethods.PRIVATE_KEY_JWT.type);
        oidcRegistrationRequest.setTokenEndpointAuthSigningAlg(JWSAlgorithm.PS256.getName());
        oidcRegistrationRequest.setIdTokenSignedResponseAlg(JWSAlgorithm.PS256.getName());
        //TODO until MIT has not fixed the encryption issue, we can't use encryption in OB
        //oidcRegistrationRequest.setIdTokenEncryptedResponseAlg(JWEAlgorithm.RSA_OAEP_256.getName());
        oidcRegistrationRequest.setSubjectType(OIDCConstants.SubjectType.PUBLIC);
        oidcRegistrationRequest.setRequestObjectSigningAlg(JWSAlgorithm.PS256.getName());
        oidcRegistrationRequest.setRequestObjectEncryptionAlg(JWEAlgorithm.RSA_OAEP_256.getName());
        oidcRegistrationRequest.setRequestObjectEncryptionEnc(EncryptionMethod.A128CBC_HS256.getName());
        return oidcRegistrationRequest;
    }

    /**
     * Generate registration request
     *
     * @return a JWT that can be used to register the TPP
     */
    private String generateRegistrationRequest(OIDCDiscoveryResponse oidcDiscoveryResponse,
                                              OIDCRegistrationRequest oidcRegistrationRequest, SignedJWT ssa) throws ParseException {
        //Convert in json for convenience
        JWTClaimsSet ssaClaims = ssa.getJWTClaimsSet();
        String asIssuerId = oidcDiscoveryResponse.getIssuer();
        JWTClaimsSet.Builder requestParameterClaims;
        requestParameterClaims = new JWTClaimsSet.Builder();
        requestParameterClaims.audience(asIssuerId);
        requestParameterClaims.expirationTime(new Date(new Date().getTime() + Duration.ofDays(7).toMillis()));
        Map<String, Object> requestAsClaims = objectMapper.convertValue(oidcRegistrationRequest, Map.class);
        for(Map.Entry<String, Object> entry : requestAsClaims.entrySet()) {
            requestParameterClaims.claim(entry.getKey(), entry.getValue());
        }
        requestParameterClaims.claim(OpenBankingConstants.RegistrationTppRequestClaims.SOFTWARE_STATEMENT, ssa.serialize());
        return cryptoApiClient.signClaims(ssaClaims.getStringClaim(SOFTWARE_ID), requestParameterClaims.build());
    }

    private SoftwareStatement getCurrentSoftwareStatement() {
        SoftwareStatement softwareStatement = directoryService.getCurrentSoftwareStatement();
        User user = userService.getUser();
        Organisation organisation = directoryService.getOrganisation(user.getOrganisationId());
        if (!organisation.getSoftwareStatementIds().contains(softwareStatement.getId())) {
            organisation.getSoftwareStatementIds().add(softwareStatement.getId());
            organisation.setName("ForgeRock");
            directoryService.updateOrganisation(organisation);
            softwareStatement.setName(applicationName);
            softwareStatement = directoryService.updateSoftwareStatement(softwareStatement);
        }
        return softwareStatement;
    }
}
