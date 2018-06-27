/*
 *  Copyright © 2018 Robin Weiss (http://www.gerdi-project.de/)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package de.gerdiproject.harvest.setup;

import java.util.List;

import com.atlassian.bamboo.specs.api.builders.deployment.Deployment;
import com.atlassian.bamboo.specs.api.builders.permission.DeploymentPermissions;
import com.atlassian.bamboo.specs.api.builders.permission.EnvironmentPermissions;
import com.atlassian.bamboo.specs.api.builders.permission.PermissionType;
import com.atlassian.bamboo.specs.api.builders.permission.Permissions;
import com.atlassian.bamboo.specs.api.builders.plan.PlanIdentifier;
import com.atlassian.bamboo.specs.builders.repository.bitbucket.server.BitbucketServerRepository;
import com.atlassian.bamboo.specs.util.BambooServer;

import de.gerdiproject.harvest.setup.constants.BambooConstants;

/**
 * This class represents a deployment project for harvesters.
 *
 * @author Robin Weiss
 */
public class HarvesterDeployment extends Deployment
{
    /**
     * Creates a harvester deployment project.
     *
     * @param repository the repository that is linked to the plan
     * @param bambooKey the bamboo key of the plan
     * @param providerClassName the name of the provider in camel case
     *
     * @return a harvester deployment plan
     */
    public HarvesterDeployment(BitbucketServerRepository repository, PlanIdentifier sourcePlanIdentifier, String providerClassName)
    {
        super(sourcePlanIdentifier, String.format(BambooConstants.DEPLOYMENT_PROJECT_NAME, providerClassName));
        description(BambooConstants.DEPLOYMENT_PROJECT_DESCRIPTION);
        releaseNaming(BambooConstants.DEPLOYMENT_RELEASE_NAMING);
        environments(
            BambooConstants.TEST_ENVIRONMENT,
            BambooConstants.STAGE_ENVIRONMENT,
            BambooConstants.PRODUCTION_ENVIRONMENT);
    }


    /**
     * Publishes the deployment project on a specified Bamboo server.
     *
     * @param bambooServer the server on which the plan is published
     * @param harvesterDeploymentSpecs the deployment project that is to be published
     * @param developerEmailAddresses email addresses of developers that will get view rights to the plan
     */
    public void publish(BambooServer bambooServer, List<String> developerEmailAddresses)
    {
        String depName = getName();
        bambooServer.publish(this);

        for (String devEmail : developerEmailAddresses) {
            DeploymentPermissions depPermission = new DeploymentPermissions(depName);
            depPermission.permissions(new Permissions()
                                      .userPermissions(devEmail, PermissionType.VIEW)
                                      .loggedInUserPermissions(PermissionType.VIEW)
                                      .anonymousUserPermissionView());
            bambooServer.publish(depPermission);

            // publish environment permissions
            EnvironmentPermissions testPermission = new EnvironmentPermissions(depName);
            testPermission.environmentName(BambooConstants.TEST_ENVIRONMENT_NAME);
            testPermission.permissions(new Permissions()
                                       .userPermissions(devEmail, PermissionType.VIEW)
                                       .loggedInUserPermissions(PermissionType.VIEW)
                                       .anonymousUserPermissionView());
            bambooServer.publish(testPermission);

            EnvironmentPermissions stagePermission = new EnvironmentPermissions(depName);
            stagePermission.environmentName(BambooConstants.STAGE_ENVIRONMENT_NAME);
            stagePermission.permissions(new Permissions()
                                        .userPermissions(devEmail, PermissionType.VIEW)
                                        .loggedInUserPermissions(PermissionType.VIEW)
                                        .anonymousUserPermissionView());
            bambooServer.publish(stagePermission);

            EnvironmentPermissions productionPermission = new EnvironmentPermissions(depName);
            productionPermission.environmentName(BambooConstants.PRODUCTION_ENVIRONMENT_NAME);
            productionPermission.permissions(new Permissions()
                                             .userPermissions(devEmail, PermissionType.VIEW)
                                             .loggedInUserPermissions(PermissionType.VIEW)
                                             .anonymousUserPermissionView());
            bambooServer.publish(productionPermission);
        }
    }
}
