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
package de.gerdiproject.harvest.setup.constants;

import java.util.regex.Pattern;

import com.atlassian.bamboo.specs.api.builders.BambooKey;
import com.atlassian.bamboo.specs.api.builders.BambooOid;
import com.atlassian.bamboo.specs.api.builders.plan.artifact.Artifact;
import com.atlassian.bamboo.specs.api.builders.plan.branches.BranchCleanup;
import com.atlassian.bamboo.specs.api.builders.plan.branches.PlanBranchManagement;
import com.atlassian.bamboo.specs.api.builders.project.Project;
import com.atlassian.bamboo.specs.api.builders.task.Task;
import com.atlassian.bamboo.specs.builders.task.CheckoutItem;
import com.atlassian.bamboo.specs.builders.task.MavenTask;
import com.atlassian.bamboo.specs.builders.task.ScriptTask;
import com.atlassian.bamboo.specs.builders.task.VcsCheckoutTask;
import com.atlassian.bamboo.specs.model.task.ScriptTaskProperties;



/**
 * A static collection of Bamboo Specs constants that are used to create a Plan
 * in the GeRDI Bamboo project.
 *
 * @author Robin Weiss
 */
public class BambooConstants
{
    // Files
    public static final String MAIN_HARVESTER_PATH = "%s/src/main/java/de/gerdiproject/harvest/harvester/";
    public static final Pattern HARVESTER_FILE_PATTERN = Pattern.compile("(\\w+)Harvester.java");

    // GIT
    public static final String GIT_CONFIG_PATH = "%s/.git/config";
    public static final String GIT_GET_ROOT_COMMAND = "git rev-parse --show-toplevel";
    public static final Pattern REPOSITORY_SLUG_PATTERN = Pattern.compile("\\s*url\\s?=[\\d\\D]+?/([^/]+?).git");

    public static final String BITBUCKET_HARVESTER_NAME = "%s-Harvester";
    public static final String BITBUCKET = "Bitbucket";
    public static final String BITBUCKET_ID = "f0c4a002-9d93-3ac9-b18b-296394ec3180";
    public static final String BITBUCKET_HARVESTER_PROJECT = "HAR";
    public static final String GIT_MASTER_BRANCH = "master";


    // Maven
    public static final String POM_XML_PATH = "%s/pom.xml";
    public static final Pattern EMAIL_TAG_PATTERN = Pattern.compile("\\s*<email>([\\d\\D]+?)</email>\\s*");
    public static final String DEVELOPERS_OPENING_TAG = "<developers>";
    public static final String DEVELOPERS_CLOSING_TAG = "</developers>";

    // Bamboo Names
    public static final BambooKey DEFAULT_JOB_KEY = new BambooKey("JOB1");
    public static final String DEFAULT_STAGE = "Default Stage";
    public static final String DEFAULT_JOB = "Default Job";
    public static final String HARVESTER_ABBREVIATION = "HAR";
    public static final String LOWER_CASE_REGEX = "[a-z]";

    public static final String PASSWORD_VARIABLE_KEY = "passwordGit";

    public static final String ANALYSIS_PLAN_NAME = "%s-Harvester Static Analysis";
    public static final String ANALYSIS_PLAN_DESCRIPTION = "Static Analysis of the ${providerName} Harvester.";

    public static final String DEPLOYMENT_PLAN_NAME = "%s-Harvester Deployment";
    public static final String DEPLOYMENT_PLAN_DESCRIPTION = "Builds a Docker Image of the Harvester and registers it at the Docker Registry.";

    // GeRDI Bamboo Projects
    public static final String BAMBOO_SERVER = "https://ci.gerdi-project.de";

    public static final Project ANALYSIS_PROJECT = new Project()
    .oid(new BambooOid("tfn4xj9wxfcx"))
    .key(new BambooKey("CA"))
    .name("Code Analysis");

    public static final Project DEPLOYMENT_PROJECT = new Project()
    .oid(new BambooOid("tfn4xj9wxczl"))
    .key(new BambooKey("DEP"))
    .name("Deployment");


    // GeRDI Bamboo Artifacts
    public static final Artifact WAR_FILE_ARTIFACT = new Artifact()
    .name("war")
    .copyPattern("*.war")
    .location("target")
    .shared(true);


    // GeRDI Bamboo Tasks
    public static final Task<?, ?> REPOSITORY_CHECKOUT_TASK = new VcsCheckoutTask()
    .description("Checkout Default Repository")
    .checkoutItems(new CheckoutItem().defaultRepository());

    public static final Task<?, ?> MAVEN_INSTALL_STRICT_TASK = new MavenTask()
    .description("Maven: Code Style Check")
    .goal("clean install -Pstrict")
    .jdk("JDK 1.8")
    .executableLabel("Maven 3")
    .hasTests(true)
    .useMavenReturnCode(true);

    public static final Task<?, ?> MAVEN_DOCKER_PUSH_TASK = new ScriptTask()
    .interpreter(ScriptTaskProperties.Interpreter.BINSH_OR_CMDEXE)
    .inlineBody(
        "# for some reason, the Maven 3.x Bamboo Task interprets multiple arguments as one, so we need to use a script instead\n"
        + "mvn clean verify -PdockerPush -Dexec.args=\"<maven> <gerdi>\"");


    // Bamboo Branch Management
    public static final PlanBranchManagement MANUAL_BRANCH_MANAGEMENT  = new PlanBranchManagement()
    .delete(new BranchCleanup())
    .notificationForCommitters();


    /**
     * Private Constructor, because this is a static class.
     */
    private BambooConstants()
    {

    }
}
