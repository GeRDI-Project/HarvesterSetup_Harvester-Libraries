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

import com.atlassian.bamboo.specs.api.builders.BambooKey;
import com.atlassian.bamboo.specs.api.builders.permission.PermissionType;
import com.atlassian.bamboo.specs.api.builders.permission.Permissions;
import com.atlassian.bamboo.specs.api.builders.permission.PlanPermissions;
import com.atlassian.bamboo.specs.api.builders.plan.Job;
import com.atlassian.bamboo.specs.api.builders.plan.Plan;
import com.atlassian.bamboo.specs.api.builders.plan.PlanIdentifier;
import com.atlassian.bamboo.specs.api.builders.plan.Stage;
import com.atlassian.bamboo.specs.api.builders.plan.branches.BranchCleanup;
import com.atlassian.bamboo.specs.api.builders.plan.branches.PlanBranchManagement;
import com.atlassian.bamboo.specs.api.builders.plan.configuration.ConcurrentBuilds;
import com.atlassian.bamboo.specs.builders.repository.bitbucket.server.BitbucketServerRepository;
import com.atlassian.bamboo.specs.builders.trigger.BitbucketServerTrigger;
import com.atlassian.bamboo.specs.util.BambooServer;

import de.gerdiproject.harvest.setup.constants.ArtifactConstants;
import de.gerdiproject.harvest.setup.constants.BambooConstants;
import de.gerdiproject.harvest.setup.constants.MavenConstants;
import de.gerdiproject.harvest.setup.constants.RepositoryConstants;

/**
 * This class represents a static analysis plan for harvesters.
 *
 * @author Robin Weiss
 */
public class HarvesterCiPlan extends Plan
{
    /**
     * Creates a code analysis plan for the harvester service.
     *
     * @param repository the repository that is linked to the plan
     * @param bambooKey the bamboo key of the plan
     * @param providerClassName the name of the provider in camel case
     *
     * @return a code analysis plan for the harvester service
     */
    @SuppressWarnings("unchecked")
    public HarvesterCiPlan(BitbucketServerRepository repository, BambooKey bambooKey, String providerClassName)
    {
        // set up plan
        super(
            BambooConstants.ANALYSIS_PROJECT,
            String.format(BambooConstants.ANALYSIS_PLAN_NAME, providerClassName),
            bambooKey);
        description(BambooConstants.ANALYSIS_PLAN_DESCRIPTION);
        pluginConfigurations(new ConcurrentBuilds().useSystemWideDefault(false));
        planRepositories(repository);
        triggers(new BitbucketServerTrigger());

        // set up job
        final Job defaultJob = new Job(
            BambooConstants.DEFAULT_JOB,
            BambooConstants.DEFAULT_JOB_KEY);

        defaultJob.tasks(
            RepositoryConstants.CHECKOUT_TASK,
            MavenConstants.MAVEN_INSTALL_STRICT_TASK);

        defaultJob.artifacts(
            ArtifactConstants.WAR_ARTIFACT,
            ArtifactConstants.DOCKERFILE_ARTIFACT,
            ArtifactConstants.SCRIPT_ARTIFACTS
        );

        // add job to plan
        stages(new Stage(BambooConstants.DEFAULT_JOB_STAGE).jobs(defaultJob));

        // auto-create plan branches, delete them after 1 day when the branch is removed in the repository
        planBranchManagement(new PlanBranchManagement()
                             .createForVcsBranch()
                             .delete(new BranchCleanup().whenRemovedFromRepositoryAfterDays(1))
                             .notificationForCommitters());
    }


    /**
     * Publishes the plan on a specified Bamboo server.
     *
     * @param bambooServer the server on which the plan is published
     * @param plan the plan that is to be published
     * @param developerEmailAddresses email addresses of developers that will get access rights to the plan
     */
    public void publish(BambooServer bambooServer, List<String> developerEmailAddresses)
    {
        PlanIdentifier planId = getIdentifier();
        bambooServer.publish(this);

        for (String devEmail : developerEmailAddresses) {
            PlanPermissions planPermission = new PlanPermissions(planId);
            planPermission.permissions(new Permissions()
                                       .userPermissions(devEmail,
                                                        PermissionType.VIEW,
                                                        PermissionType.CLONE)
                                       .loggedInUserPermissions(PermissionType.VIEW)
                                       .anonymousUserPermissionView());
            bambooServer.publish(planPermission);
        }
    }
}
