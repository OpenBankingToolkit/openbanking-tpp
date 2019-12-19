module.exports = {
  docker: {
    version: 'BUILD_VERSION',
    template: 'TEMPLATE'
  },
  tppAppAddress: 'https://tpp.DOMAIN',
  bankAppAddress: 'https://bank.DOMAIN',
  CORSWhitelist: [
    'https://tpp.DOMAIN',
    'http://native.tpp.DOMAIN',
    'http://localhost',
    'capacitor://native.tpp.DOMAIN',
    'capacitor://localhost'
  ],
  cookieDomain: '.DOMAIN',
  monitoringBackend: 'https://monitoring.DOMAIN',
  nodeBackend: 'https://node.tpp.DOMAIN',
  rsBackend: 'https://rs.aspsp.DOMAIN',
  asBackend: 'https://as.aspsp.DOMAIN',
  directoryBackend: 'https://matls.service.directory.DOMAIN',
  jwkmsBackend: 'https://jwkms.DOMAIN',
  matlsBackend: 'https://matls.as.aspsp.DOMAIN',
  enableCustomization: 'ENABLE_CUSTOMIZATION'
};
