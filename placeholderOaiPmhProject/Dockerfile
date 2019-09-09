# GeRDI OAI-PMH-Harvester Image for harvesting:
# ${repositoryName}

FROM docker-registry.gerdi.research.lrz.de:5043/harvest/oai-pmh:${oaiPmhHarvesterVersion}

# copy config file
COPY config.json /var/lib/jetty/config/OaiPmhHarvesterService/config.json

# add permissions
USER root
RUN chown -R jetty:jetty /var/lib/jetty/
USER jetty

EXPOSE 8080


