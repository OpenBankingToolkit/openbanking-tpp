module.exports = {
  docker: {
    version: 'BUILD_VERSION',
    template: 'TEMPLATE'
  },
  CORSWhitelist: [
    'TPP_URL',
    'http://native.tpp.DOMAIN',
    'http://localhost',
    'capacitor://native.tpp.DOMAIN',
    'capacitor://localhost'
  ],
  tppAppAddress: 'TPP_URL',
  bankAppAddress: 'BANK_URL',
  cookieDomain: '.DOMAIN',
  monitoringBackend: 'MONITORING_BACKEND_URL',
  nodeBackend: 'NODE_BACKEND_URL',
  rsBackend: 'RS_BACKEND_URL',
  asBackend: 'AUTHORIZATION_URL',
  directoryBackend: 'DIRECTORY_BACKEND_URL',
  jwkmsBackend: 'JWKMS_BACKEND_URL',
  matlsBackend: 'MATLS_BACKEND_URL'
};
