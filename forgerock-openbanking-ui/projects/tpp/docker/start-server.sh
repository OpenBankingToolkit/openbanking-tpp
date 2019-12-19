#!/usr/bin/env sh
set -e

if [[ -z "${DOMAIN}" ]]; then
  echo "DOMAIN environment variable should be set"
  exit 1
fi

sed -i "s/DOMAIN/${DOMAIN}/g" /src/dist/deployment-settings.js
sed -i "s/ENABLE_CUSTOMIZATION/${ENABLE_CUSTOMIZATION}/g" /src/dist/deployment-settings.js
cat /src/dist/deployment-settings.js
npm start