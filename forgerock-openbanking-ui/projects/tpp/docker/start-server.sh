#!/usr/bin/env bash
set -e

if [[ -z "${DOMAIN}" ]]; then
  echo "DOMAIN environment variable should be set"
  exit 1
fi

sed -i "s@TPP_URL@${TPP_URL}@g" /src/dist/deployment-settings.js
sed -i "s@BANK_URL@${BANK_URL}@g" /src/dist/deployment-settings.js
sed -i "s@MONITORING_BACKEND_URL@${MONITORING_BACKEND_URL}@g" /src/dist/deployment-settings.js
sed -i "s@RS_BACKEND_URL@${RS_BACKEND_URL}@g" /src/dist/deployment-settings.js
sed -i "s@AUTHORIZATION_URL@${AUTHORIZATION_URL}@g" /src/dist/deployment-settings.js
sed -i "s@DIRECTORY_BACKEND_URL@${DIRECTORY_BACKEND_URL}@g" /src/dist/deployment-settings.js
sed -i "s@JWKMS_BACKEND_URL@${JWKMS_BACKEND_URL}@g" /src/dist/deployment-settings.js
sed -i "s@MATLS_BACKEND_URL@${MATLS_BACKEND_URL}@g" /src/dist/deployment-settings.js
sed -i "s/DOMAIN/${DOMAIN}/g" /src/dist/deployment-settings.js
cat /src/dist/deployment-settings.js
npm start
