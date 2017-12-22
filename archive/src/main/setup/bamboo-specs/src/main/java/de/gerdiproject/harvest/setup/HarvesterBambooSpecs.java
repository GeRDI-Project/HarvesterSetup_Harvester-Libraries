/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bamboo.specs.api.BambooSpec;
import com.atlassian.bamboo.specs.api.builders.BambooKey;
import com.atlassian.bamboo.specs.api.builders.Variable;
import com.atlassian.bamboo.specs.api.builders.applink.ApplicationLink;
import com.atlassian.bamboo.specs.api.builders.deployment.Deployment;
import com.atlassian.bamboo.specs.api.builders.deployment.Environment;
import com.atlassian.bamboo.specs.api.builders.deployment.ReleaseNaming;
import com.atlassian.bamboo.specs.api.builders.permission.DeploymentPermissions;
import com.atlassian.bamboo.specs.api.builders.permission.EnvironmentPermissions;
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
import com.atlassian.bamboo.specs.api.builders.repository.VcsChangeDetection;
import com.atlassian.bamboo.specs.builders.repository.bitbucket.server.BitbucketServerRepository;
import com.atlassian.bamboo.specs.builders.repository.viewer.BitbucketServerRepositoryViewer;
import com.atlassian.bamboo.specs.builders.task.CleanWorkingDirectoryTask;
import com.atlassian.bamboo.specs.builders.trigger.BitbucketServerTrigger;
import com.atlassian.bamboo.specs.util.BambooServer;

import de.gerdiproject.harvest.setup.constants.BambooConstants;
import de.gerdiproject.harvest.setup.constants.LoggingConstants;
import de.gerdiproject.harvest.setup.utils.ProjectUtils;

/**
 * A static collection of Bamboo Specs constants that are used to create a Plan
 * in the GeRDI Bamboo project.
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

        final BambooServer bambooServer = getBambooServer();
        BitbucketServerRepository repository = createRepository(providerClassName, repositorySlug);

        Plan staticAnalysisPlan = createStaticAnalysisPlan(repository, bambooKey, providerClassName);
        publish(bambooServer, staticAnalysisPlan, devEmails);

        Deployment deploymentProject = createDeploymentProject(repository, staticAnalysisPlan.getIdentifier(), providerClassName);
        publish(bambooServer, deploymentProject, devEmails);
    }


    /**
     * Sets up a connection to a Bamboo server.
     *
     * @return a Bamboo server connection
     */
    private static BambooServer getBambooServer()
    {
        LOGGER.info(String.format(LoggingConstants.CONNECTING_TO_SERVER, BambooConstants.BAMBOO_SERVER));
        return new BambooServer(BambooConstants.BAMBOO_SERVER);
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
               .name(String.format(BambooConstants.BITBUCKET_HARVESTER_NAME, providerClassName))
               .repositoryViewer(new BitbucketServerRepositoryViewer())
               .server(new ApplicationLink()
                       .name(BambooConstants.BITBUCKET)
                       .id(BambooConstants.BITBUCKET_ID))
               .projectKey(BambooConstants.BITBUCKET_HARVESTER_PROJECT)
               .repositorySlug(repositorySlug)
               .branch(BambooConstants.GIT_MASTER_BRANCH)
               .shallowClonesEnabled(true)
               .remoteAgentCacheEnabled(false)
               .changeDetection(new VcsChangeDetection());
    }


    /**
     * Creates a harvester deployment plan.
     *
     * @param repository the repository that is linked to the plan
     * @param bambooKey the bamboo key of the plan
     * @param providerClassName the name of the provider in camel case
     *
     * @return a harvester deployment plan
     */
    private static Plan createDeploymentPlan(BitbucketServerRepository repository, BambooKey bambooKey, String providerClassName)
    {
        // set up plan
        final Plan deploymentPlan = new Plan(
            BambooConstants.DEPLOYMENT_PROJECT,
            String.format(BambooConstants.DEPLOYMENT_PLAN_NAME, providerClassName),
            bambooKey);
        deploymentPlan.description(BambooConstants.DEPLOYMENT_PLAN_DESCRIPTION);
        deploymentPlan.pluginConfigurations(new ConcurrentBuilds().useSystemWideDefault(false));
        deploymentPlan.planRepositories(repository);
        deploymentPlan.variables(new Variable(BambooConstants.PASSWORD_VARIABLE_KEY, ""));
        deploymentPlan.planBranchManagement(BambooConstants.MANUAL_BRANCH_MANAGEMENT);

        // set up job
        final Job defaultJob = new Job(
            BambooConstants.DEFAULT_JOB,
            BambooConstants.DEFAULT_JOB_KEY);
        defaultJob.artifacts(BambooConstants.WAR_FILE_ARTIFACT);
        defaultJob.tasks(BambooConstants.REPOSITORY_CHECKOUT_TASK,
                         BambooConstants.MAVEN_DOCKER_PUSH_TASK);

        // add job to plan
        deploymentPlan.stages(new Stage(BambooConstants.DEFAULT_STAGE).jobs(defaultJob));
        return deploymentPlan;
    }

    /**
     * Creates a harvester deployment project.
     *
     * @param repository the repository that is linked to the plan
     * @param bambooKey the bamboo key of the plan
     * @param providerClassName the name of the provider in camel case
     *
     * @return a harvester deployment plan
     */
    private static Deployment createDeploymentProject(BitbucketServerRepository repository, PlanIdentifier sourcePlanIdentifier, String providerClassName)
    {
        Deployment dep = new Deployment(sourcePlanIdentifier, String.format(BambooConstants.DEPLOYMENT_PLAN_NAME, providerClassName));
        dep.description(BambooConstants.DEPLOYMENT_PLAN_DESCRIPTION);
        dep.releaseNaming(new ReleaseNaming(BambooConstants.DEPLOYMENT_RELEASE_NAMING).autoIncrement(false));

        // add production environment
        Environment productionEnvironment = new Environment(BambooConstants.PRODUCTION_DEPLOYMENT_ENV)
        .tasks(new CleanWorkingDirectoryTask(),
               BambooConstants.REPOSITORY_CHECKOUT_TASK,
               BambooConstants.MAVEN_DOCKER_PUSH_TASK);
        dep.environments(productionEnvironment);

        return dep;
    }


    /**
     * Creates a code analysis plan for the harvester service.
     *
     * @param repository the repository that is linked to the plan
     * @param bambooKey the bamboo key of the plan
     * @param providerClassName the name of the provider in camel case
     *
     * @return a code analysis plan for the harvester service
     */
    private static Plan createStaticAnalysisPlan(BitbucketServerRepository repository, BambooKey bambooKey, String providerClassName)
    {
        // set up plan
        Plan analysisPlan = new Plan(
            BambooConstants.ANALYSIS_PROJECT,
            String.format(BambooConstants.ANALYSIS_PLAN_NAME, providerClassName),
            bambooKey);
        analysisPlan.description(BambooConstants.ANALYSIS_PLAN_DESCRIPTION);
        analysisPlan.pluginConfigurations(new ConcurrentBuilds().useSystemWideDefault(false));
        analysisPlan.planRepositories(repository);
        analysisPlan.triggers(new BitbucketServerTrigger());

        // set up job
        final Job defaultJob = new Job(
            BambooConstants.DEFAULT_JOB,
            BambooConstants.DEFAULT_JOB_KEY);

        defaultJob.tasks(
            BambooConstants.REPOSITORY_CHECKOUT_TASK,
            BambooConstants.MAVEN_INSTALL_STRICT_TASK);

        // add job to plan
        analysisPlan.stages(new Stage(BambooConstants.DEFAULT_STAGE).jobs(defaultJob));

        // auto-create plan branches, delete them after 1 day when the branch is removed in the repository
        analysisPlan.planBranchManagement(new PlanBranchManagement()
                                          .createForVcsBranch()
                                          .delete(new BranchCleanup().whenRemovedFromRepositoryAfterDays(1))
                                          .notificationForCommitters());
        return analysisPlan;
    }


    /**
     * Publishes a plan on a specified Bamboo server.
     *
     * @param bambooServer the server on which the plan is published
     * @param plan the plan that is to be published
     * @param developerEmailAddresses email addresses of developers that will get access rights to the plan
     */
    private static void publish(BambooServer bambooServer, Plan plan, List<String> developerEmailAddresses)
    {
        PlanIdentifier planId = plan.getIdentifier();
        bambooServer.publish(plan);

        for (String devEmail : developerEmailAddresses) {
            PlanPermissions planPermission = new PlanPermissions(planId);
            planPermission.permissions(new Permissions()
                                       .userPermissions(devEmail,
                                                        PermissionType.EDIT,
                                                        PermissionType.VIEW,
                                                        PermissionType.ADMIN,
                                                        PermissionType.CLONE,
                                                        PermissionType.BUILD)
                                       .loggedInUserPermissions(PermissionType.VIEW)
                                       .anonymousUserPermissionView());
            bambooServer.publish(planPermission);
        }
    }

    /**
     * Publishes a deployment project on a specified Bamboo server.
     *
     * @param bambooServer the server on which the plan is published
     * @param deployment the deployment project that is to be published
     * @param developerEmailAddresses email addresses of developers that will get view rights to the plan
     */
    private static void publish(BambooServer bambooServer, Deployment deployment, List<String> developerEmailAddresses)
    {
        String depName = deployment.getName();
        bambooServer.publish(deployment);

        for (String devEmail : developerEmailAddresses) {
            DeploymentPermissions depPermission = new DeploymentPermissions(depName);
            depPermission.permissions(new Permissions()
                                      .userPermissions(devEmail, PermissionType.VIEW)
                                      .loggedInUserPermissions(PermissionType.VIEW)
                                      .anonymousUserPermissionView());
            bambooServer.publish(depPermission);

            EnvironmentPermissions envPermission = new EnvironmentPermissions(depName);
            envPermission.environmentName(BambooConstants.PRODUCTION_DEPLOYMENT_ENV);
            envPermission.permissions(new Permissions()
                                      .userPermissions(devEmail, PermissionType.VIEW)
                                      .loggedInUserPermissions(PermissionType.VIEW)
                                      .anonymousUserPermissionView());
        }
    }
}