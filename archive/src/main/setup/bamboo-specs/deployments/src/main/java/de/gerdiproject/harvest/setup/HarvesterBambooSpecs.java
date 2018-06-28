/**
 * Copyright © ${creationYear} ${authorFullName} (http://www.gerdi-project.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.gerdiproject.harvest.setup;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bamboo.specs.api.BambooSpec;
import com.atlassian.bamboo.specs.api.builders.BambooKey;
import com.atlassian.bamboo.specs.api.builders.plan.PlanIdentifier;
import com.atlassian.bamboo.specs.api.builders.repository.VcsChangeDetection;
import com.atlassian.bamboo.specs.builders.repository.bitbucket.server.BitbucketServerRepository;
import com.atlassian.bamboo.specs.builders.repository.viewer.BitbucketServerRepositoryViewer;
import com.atlassian.bamboo.specs.util.BambooServer;
import com.atlassian.bamboo.specs.util.SimpleUserPasswordCredentials;
import com.atlassian.bamboo.specs.util.UserPasswordCredentials;

import de.gerdiproject.harvest.setup.constants.BambooConstants;
import de.gerdiproject.harvest.setup.constants.LoggingConstants;
import de.gerdiproject.harvest.setup.constants.RepositoryConstants;
import de.gerdiproject.harvest.setup.utils.ProjectUtils;

/**
 * A static collection of Bamboo Specs methods that are used to create a plans
 * and deployment plans for a harvester project.
 *
 * @author Robin Weiss
 */
@BambooSpec
public class HarvesterBambooSpecs
{
    private static Logger LOGGER = LoggerFactory.getLogger(HarvesterBambooSpecs.class);


    /**
     * The main function that is called via 'mvn -Ppublish-specs' from the command line.
     *
     * @param args arguments for the function. If you know how to pass them via Maven, give me a call!
     */
    public static void main(String[] args)
    {
        final UserPasswordCredentials adminUser = new SimpleUserPasswordCredentials(args[0], args[1]);

        final ProjectUtils utils = new ProjectUtils();

        final String providerClassName = utils.getProviderClassName();
        LOGGER.info(LoggingConstants.PROVIDER_CLASS_NAME + providerClassName);

        final String repositorySlug = utils.getRepositorySlug();
        LOGGER.info(LoggingConstants.REPOSITORY_SLUG + repositorySlug);

        final BambooKey bambooKey = utils.createBambooKey(providerClassName);
        LOGGER.info(LoggingConstants.BAMBOO_KEY + bambooKey);

        final List<String> devEmails = utils.getDeveloperEmailAddresses();
        final StringBuilder sb = new StringBuilder(LoggingConstants.DEVELOPER_EMAILS);
        devEmails.forEach((String email) -> sb.append(' ').append(email));
        LOGGER.info(sb.toString());

        final BambooServer bambooServer = getBambooServer(adminUser);
        BitbucketServerRepository repository = createRepository(providerClassName, repositorySlug);

        final PlanIdentifier staticAnalysisPlanId = new PlanIdentifier(BambooConstants.HARVESTER_ABBREVIATION, bambooKey.toString());
        final HarvesterDeployment deploymentProject = new HarvesterDeployment(repository, staticAnalysisPlanId, providerClassName);
        deploymentProject.publish(bambooServer, devEmails);
    }


    /**
     * Sets up a connection to a Bamboo server.
     *
     * @param credentials a Bamboo administrator user name and password
     * @return a Bamboo server connection
     */
    private static BambooServer getBambooServer(UserPasswordCredentials credentials)
    {
        LOGGER.info(String.format(LoggingConstants.CONNECTING_TO_SERVER, BambooConstants.BAMBOO_SERVER));
        return new BambooServer(BambooConstants.BAMBOO_SERVER, credentials);
    }


    /**
     * Creates a BitBucket repository for a harvester service.
     *
     * @param providerClassName the name of the provider in camel case
     * @param repositorySlug the name of the harvester service in a BitBucket URL
     *
     * @return a BitBucket repository for a harvester service
     */
    private static BitbucketServerRepository createRepository(String providerClassName, String repositorySlug)
    {
        return new BitbucketServerRepository()
               .name(String.format(RepositoryConstants.BITBUCKET_HARVESTER_NAME, providerClassName))
               .repositoryViewer(new BitbucketServerRepositoryViewer())
               .server(RepositoryConstants.BITBUCKET_SERVER)
               .projectKey(RepositoryConstants.BITBUCKET_HARVESTER_PROJECT)
               .repositorySlug(repositorySlug)
               .branch(RepositoryConstants.GIT_MASTER_BRANCH)
               .remoteAgentCacheEnabled(false)
               .changeDetection(new VcsChangeDetection().quietPeriodEnabled(true));
    }
}