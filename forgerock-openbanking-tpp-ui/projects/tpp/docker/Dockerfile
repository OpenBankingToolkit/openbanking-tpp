# Stage 1 - the build process
FROM r.cfcr.io/openbanking/obri/build-tpp-ui as build-ui
RUN npm run build.tpp.all

# Stage 2 - the production environment
FROM nginx:1.13.9-alpine

EXPOSE 80

ARG BUILD_VERSION
ENV BUILD_VERSION ${BUILD_VERSION}
ENV DOMAIN ""
ENV TEMPLATE ""
ENV ENABLE_CUSTOMIZATION "false"
ENV ANDROID_PKG_NAME "financial.forgerock.ob.tpp"
ENV IOS_APP_ID "to-be-defined"

RUN rm -rf /etc/nginx/conf.d
COPY --from=build-ui /src/dist /usr/share/nginx
COPY projects/tpp/docker/nginx.conf /etc/nginx/conf.d/default.conf
# Android, iOS specific files (only for forgerock customer)
COPY projects/tpp/docker/.well-known /usr/share/nginx/html/.well-known
COPY projects/tpp/docker/start.sh /opt/start.sh

CMD ["/opt/start.sh"]
