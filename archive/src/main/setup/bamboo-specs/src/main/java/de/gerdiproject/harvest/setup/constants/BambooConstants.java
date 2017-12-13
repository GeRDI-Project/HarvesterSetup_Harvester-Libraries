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
import com.atlassian.bamboo.specs.builders.task.VcsCheckoutTask;



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

    // Maven
    public static final String POM_XML_PATH = "%s/pom.xml";
    public static final Pattern EMAIL_TAG_PATTERN = Pattern.compile("\\s*<email>([\\d\\D]+?)</email>\\s*");
    public static final String DEVELOPERS_OPENING_TAG = "<developers>";
    public static final String DEVELOPERS_CLOSING_TAG = "</developers>";

    // Bamboo Names
    public static final String DEFAULT_STAGE = "Default Stage";
    public static final String DEFAULT_JOB = "Default Job";
    public static final String DEPLOY_PLAN_NAME = "Deploy %s-Harvester";
    public static final String ANALYSIS_PLAN_NAME = "Static Analysis: %s-Harvester";
    public static final String HARVESTER_ABBREVIATION = "HAR";
    public static final String LOWER_CASE_REGEX = "[a-z]";


    public static final BambooKey DEFAULT_JOB_KEY = new BambooKey("JOB1");


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


    // Bamboo Branch Management
    public static final PlanBranchManagement MANUAL_BRANCH_MANAGEMENT  = new PlanBranchManagement()
    .delete(new BranchCleanup())
    .notificationForCommitters();


    // Scripts
    public static final String MAVEN_DOCKER_PUSH_SCRIPT =
        "# for some reason, the Maven 3.x Bamboo Task interprets multiple arguments as one, so we need to use a script instead\n"
        + "mvn clean verify -PdockerPush -Dexec.args=\"<maven> <gerdi>\"";

    public static final String ASTYLE_CHECK_SCRIPT =
        "echo \"\\\\nChecking code formatting:\"\n\n"
        + "formattingStyle=\"kr\"\n"
        + "sourcePath=\"src\\\\\"\n"
        + "astyleLibPath=\"\\\\usr\\lib\\astyle\\file\\\\\"\n\n"
        + "# run AStyle without changing the files\n"
        + "result=$(astyle --options=\"$astyleLibPath$formattingStyle.ini\" --dry-run --recursive --formatted $sourcePath*)\n\n"
        + "# remove all text up until the name of the first unformatted file\n"
        + "newResult=${result#*Formatted  }\n\n"
        + "errorCount=0\n\n"
        + "while [ \"$newResult\" != \"$result\" ]\ndo\n"
        + "  errorCount=$(($errorCount + 1))\n"
        + "  result=\"$newResult\"\n\n"
        + "  # retrieve the name of the unformatted file\n"
        + "  fileName=$(echo $result | sed -e \"s/Formatted .*//gi\")\n\n"
        + "  # log the unformatted file\n"
        + "  echo \"Unformatted File: $fileName\"\n\n"
        + "  # remove all text up until the name of the next unformatted file\n"
        + "  newResult=${result#*Formatted  }\n"
        + "done\n\n"
        + "if [ $errorCount -ne 0 ]; then\n"
        + "  echo \"\\\\nFound $errorCount unformatted files! Please use the AristicStyle formatter before committing your code!\\\\n(see https://wiki.gerdi-project.de/display/GeRDI/%5BWIP%5D+How+to+Format+Code)\"\n"
        + "  exit 1\n"
        + "else\n"
        + "  echo \"All files are properly formatted!\"\n"
        + "  exit 0\n"
        + "fi";


    /**
     * Private Constructor, because this is a static class.
     */
    private BambooConstants()
    {

    }
}
