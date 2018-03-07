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
package de.gerdiproject.harvest.setup.constants;

import java.util.regex.Pattern;

import com.atlassian.bamboo.specs.api.builders.BambooKey;
import com.atlassian.bamboo.specs.api.builders.BambooOid;
import com.atlassian.bamboo.specs.api.builders.project.Project;
import com.atlassian.bamboo.specs.api.builders.task.Task;
import com.atlassian.bamboo.specs.builders.task.ScriptTask;
import com.atlassian.bamboo.specs.model.task.ScriptTaskProperties;




/**
 * A static collection of Bamboo Specs constants that are used to create Plans
 * and Deployment Projects on the GeRDI Bamboo.
 *
 * @author Robin Weiss
 */
public class BambooConstants
{
    // Files
    public static final String MAIN_HARVESTER_PATH = "%s/src/main/java/de/gerdiproject/harvest/harvester/";
    public static final Pattern HARVESTER_FILE_PATTERN = Pattern.compile("(\\w+)Harvester.java");


    // Generic Bamboo Text
    public static final String BAMBOO_SERVER = "https://ci.gerdi-project.de";

    public static final String DEFAULT_JOB = "Default Job";
    public static final BambooKey DEFAULT_JOB_KEY = new BambooKey("JOB1");
    public static final String DEFAULT_JOB_STAGE = "Default Stage";
    public static final String HARVESTER_ABBREVIATION = "HAR";
    public static final String LOWER_CASE_REGEX = "[a-z]";

    public static final String PASSWORD_VARIABLE_KEY = "passwordGit";

    public static final String ANALYSIS_PLAN_NAME = "%s-Harvester Static Analysis";
    public static final String ANALYSIS_PLAN_DESCRIPTION = "Static Analysis of the ${providerName} Harvester.";

    public static final String DEPLOYMENT_PROJECT_NAME = "%s-Harvester";
    public static final String DEPLOYMENT_PROJECT_DESCRIPTION = "Builds a Docker Image of the Harvester and registers it at the Docker Registry.";
    public static final String PRODUCTION_DEPLOYMENT_ENV = "Production";
    public static final String DEPLOYMENT_PROJECT_RELEASE_NAMING = "${bamboo.RELEASE_VERSION}";


    // Projects
    public static final Project ANALYSIS_PROJECT = new Project()
    .oid(new BambooOid("tfn4xj9wxfcx"))
    .key(new BambooKey("CA"))
    .name("Code Analysis");


    // Tasks
    public static final Task<?, ?> DOCKER_PUSH_TASK = new ScriptTask()
    .interpreter(ScriptTaskProperties.Interpreter.BINSH_OR_CMDEXE)
    .description("Create and add image to Docker registry")
    .inlineBody("./scripts/docker-push.sh \"<maven>\" \"${bamboo.deploy.version}\" \"<gerdi>\"");


    /**
     * Private Constructor, because this is a static class.
     */
    private BambooConstants()
    {

    }
}
