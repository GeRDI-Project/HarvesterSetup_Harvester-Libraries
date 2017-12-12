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


import com.atlassian.bamboo.specs.api.BambooSpec;
import com.atlassian.bamboo.specs.api.builders.BambooKey;
import com.atlassian.bamboo.specs.api.builders.Variable;
import com.atlassian.bamboo.specs.api.builders.applink.ApplicationLink;
import com.atlassian.bamboo.specs.api.builders.permission.PermissionType;
import com.atlassian.bamboo.specs.api.builders.permission.Permissions;
import com.atlassian.bamboo.specs.api.builders.permission.PlanPermissions;
import com.atlassian.bamboo.specs.api.builders.plan.Job;
import com.atlassian.bamboo.specs.api.builders.plan.Plan;
import com.atlassian.bamboo.specs.api.builders.plan.Stage;
import com.atlassian.bamboo.specs.api.builders.plan.branches.BranchCleanup;
import com.atlassian.bamboo.specs.api.builders.plan.branches.PlanBranchManagement;
import com.atlassian.bamboo.specs.api.builders.plan.configuration.ConcurrentBuilds;
import com.atlassian.bamboo.specs.api.builders.repository.VcsChangeDetection;
import com.atlassian.bamboo.specs.builders.repository.bitbucket.server.BitbucketServerRepository;
import com.atlassian.bamboo.specs.builders.repository.viewer.BitbucketServerRepositoryViewer;
import com.atlassian.bamboo.specs.builders.task.MavenTask;
import com.atlassian.bamboo.specs.builders.task.ScriptTask;
import com.atlassian.bamboo.specs.builders.trigger.BitbucketServerTrigger;
import com.atlassian.bamboo.specs.model.task.ScriptTaskProperties;
import com.atlassian.bamboo.specs.util.BambooServer;

import de.gerdiproject.harvest.setup.constants.BambooConstants;
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
    public static void main(String[] args)
    {
        final BambooServer bambooServer = getBambooServer();
    	
        final ProjectUtils utils = new ProjectUtils();
        final String repositorySlug = utils.getRepositorySlug();
        final List<String> devEmails = utils.getDeveloperEmailAddresses();
        final String providerClassName = utils.getProviderClassName();
        final BambooKey bambooKey = utils.createBambooKey(providerClassName);
        
        System.out.println("providerClassName: " + providerClassName);
        System.out.println("repositorySlug: " + repositorySlug);
        System.out.println("bambooKey: " + bambooKey);
        System.out.println("emails: ");
        devEmails.forEach((String email) -> System.out.println(email));

        BitbucketServerRepository repository = createRepository( providerClassName, repositorySlug );

        Plan staticAnalysisPlan = createStaticAnalysisPlan(repository, bambooKey, providerClassName);
        publishPlan( bambooServer, staticAnalysisPlan, devEmails );

        Plan deploymentPlan = createDeploymentPlan(repository, bambooKey, providerClassName);
        publishPlan( bambooServer, deploymentPlan, devEmails );
    }
    
    
    private static BambooServer getBambooServer()
    {
    	return new BambooServer("https://ci.gerdi-project.de");
    }
    

    private static BitbucketServerRepository createRepository(String providerClassName, String repositorySlug)
    {
        return new BitbucketServerRepository()
               .name(providerClassName + "-Harvester")
               .repositoryViewer(new BitbucketServerRepositoryViewer())
               .server(new ApplicationLink()
                       .name("Bitbucket")
                       .id("f0c4a002-9d93-3ac9-b18b-296394ec3180"))
               .projectKey("HAR")
               .repositorySlug(repositorySlug)
               .branch("master")
               .shallowClonesEnabled(true)
               .remoteAgentCacheEnabled(false)
               .changeDetection(new VcsChangeDetection());
    }


    private static Plan createDeploymentPlan(BitbucketServerRepository repository, BambooKey bambooKey, String providerClassName)
    {
        final Plan deploymentPlan = new Plan(
            BambooConstants.DEPLOYMENT_PROJECT,
            String.format(BambooConstants.DEPLOY_PLAN_NAME, providerClassName),
            bambooKey
        );

        final Job defaultJob = new Job(
            BambooConstants.DEFAULT_JOB,
            BambooConstants.DEFAULT_JOB_KEY
        );

        // set up plan
        deploymentPlan.description("Builds a Docker Image of the Harvester and registers it at the Docker Registry.");
        deploymentPlan.pluginConfigurations(new ConcurrentBuilds().useSystemWideDefault(false));
        deploymentPlan.planRepositories(repository);
        deploymentPlan.variables(new Variable("passwordGit", ""));
        deploymentPlan.planBranchManagement(BambooConstants.MANUAL_BRANCH_MANAGEMENT);
        deploymentPlan.stages(new Stage(BambooConstants.DEFAULT_STAGE).jobs(defaultJob));

        // set up job
        defaultJob.artifacts(BambooConstants.WAR_FILE_ARTIFACT);
        defaultJob.tasks(BambooConstants.REPOSITORY_CHECKOUT_TASK,
                         new ScriptTask()
                         .interpreter(ScriptTaskProperties.Interpreter.BINSH_OR_CMDEXE)
                         .inlineBody(BambooConstants.MAVEN_DOCKER_PUSH_SCRIPT));

        return deploymentPlan;
    }


    private static Plan createStaticAnalysisPlan(BitbucketServerRepository repository, BambooKey bambooKey, String providerClassName)
    {
        Plan analysisPlan = new Plan(
            BambooConstants.ANALYSIS_PROJECT,
            String.format(BambooConstants.ANALYSIS_PLAN_NAME, providerClassName),
            bambooKey);

        analysisPlan.description("Static Analysis of the ${providerName} Harvester.");
        analysisPlan.pluginConfigurations(new ConcurrentBuilds().useSystemWideDefault(false));
        analysisPlan.planRepositories(repository);
        analysisPlan.triggers(new BitbucketServerTrigger());

        analysisPlan.stages(new Stage("Default Stage")
                            .jobs(new Job("Code Formatting", new BambooKey("JOB1"))
                                  .description("Checks if the committed code compiles and is formatted via AStyle")
                                  .tasks(
                                      BambooConstants.REPOSITORY_CHECKOUT_TASK,
                                      new MavenTask()
                                      .description("Maven: Clean, Test, Verify")
                                      .goal("clean test verify")
                                      .jdk("JDK 1.8")
                                      .executableLabel("Maven 3")
                                      .hasTests(true)
                                      .useMavenReturnCode(true),
                                      new ScriptTask()
                                      .description("AStyle Formatting-Check")
                                      .interpreter(ScriptTaskProperties.Interpreter.BINSH_OR_CMDEXE)
                                      .inlineBody(BambooConstants.ASTYLE_CHECK_SCRIPT))));

        // auto-create plan branches, delete them after 1 day when the branch is removed in the repository
        analysisPlan.planBranchManagement(new PlanBranchManagement()
                                          .createForVcsBranch()
                                          .delete(new BranchCleanup().whenRemovedFromRepositoryAfterDays(1))
                                          .notificationForCommitters());

        return analysisPlan;
    }


    private static void publishPlan( BambooServer bambooServer, Plan plan, List<String> developerEmailAddresses)
    {
    	bambooServer.publish(plan);

        for (String devEmail : developerEmailAddresses) {
            PlanPermissions planPermission = new PlanPermissions(plan.getIdentifier());
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
}