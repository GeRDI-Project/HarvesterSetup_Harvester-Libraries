# GeRDI Harvester Image for '${providerName}'

FROM jetty:9.4.7-alpine

COPY \target\*.war $JETTY_BASE\webapps\${providerPackageName}.war

EXPOSE 8080