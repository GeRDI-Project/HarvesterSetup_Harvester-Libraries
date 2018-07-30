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

import com.atlassian.bamboo.specs.api.builders.BambooKey;
import com.atlassian.bamboo.specs.api.builders.plan.branches.BranchCleanup;
import com.atlassian.bamboo.specs.api.builders.plan.branches.PlanBranchManagement;
import com.atlassian.bamboo.specs.api.builders.project.Project;
import com.atlassian.bamboo.specs.api.builders.task.Task;
import com.atlassian.bamboo.specs.builders.task.InjectVariablesTask;
import com.atlassian.bamboo.specs.builders.task.MavenTask;
import com.atlassian.bamboo.specs.builders.task.ScriptTask;
import com.atlassian.bamboo.specs.model.task.InjectVariablesScope;
import com.atlassian.bamboo.specs.model.task.ScriptTaskProperties;

/**
 * A static collection of Bamboo Specs constants that are used to create Plans
 * and Deployment Projects on the GeRDI Bamboo.
 *
 * @author Robin Weiss
 */
public class BambooConstants
{
    // Variable Injection
    public static final String VERSION_VARIABLE_FILE = "injectedVersions.ini";
    public static final String VARIABLE_INJECTION_NAMESPACE = "inject";

    public static final PlanBranchManagement REMOVE_PLAN_BRANCH_AFTER_ONE_DAY =
        new PlanBranchManagement()
    .createForVcsBranch()
    .delete(new BranchCleanup()
            .whenRemovedFromRepositoryAfterDays(1))
    .notificationForCommitters();

    // Generic Bamboo Text
    public static final String BAMBOO_SERVER = "https://ci.gerdi-project.de";

    public static final String DEFAULT_JOB = "Static Analysis Job";
    public static final BambooKey DEFAULT_JOB_KEY = new BambooKey("JOB1");
    public static final String DEFAULT_JOB_STAGE = "Default Stage";
    public static final String LOWER_CASE_REGEX = "[a-z]";

    public static final String ANALYSIS_PLAN_NAME = "%s-Harvester Static Analysis";
    public static final String ANALYSIS_PLAN_DESCRIPTION = "Static Analysis of the %s-Harvester.";

    // Projects
    public static final Project ANALYSIS_PROJECT = new Project().key(new BambooKey("CA"));

    // Tasks
    public static final Task<?, ?> PREPARE_VERSION_VARIABLES_TASK = new ScriptTask()
    .description("Prepare Export of Version Variables")
    .location(ScriptTaskProperties.Location.FILE)
    .fileFromPath(RepositoryConstants.BAMBOO_SCRIPTS_WORKING_DIR + "/plans/codeAnalysis/exportVersionVariables.sh")
    .argument(String.format("\"%s\" \"%s\" \"%s\"",
                            RepositoryConstants.HARVESTER_WORKING_DIR,
                            RepositoryConstants.HARVESTER_WORKING_DIR,
                            VERSION_VARIABLE_FILE));

    public static final Task<?, ?> EXPORT_VERSION_VARIABLES_TASK = new InjectVariablesTask()
    .description("Export Version Variables")
    .path(VERSION_VARIABLE_FILE)
    .namespace(VARIABLE_INJECTION_NAMESPACE)
    .scope(InjectVariablesScope.RESULT);

    public static final Task<?, ?> MAVEN_INSTALL_STRICT_TASK = new MavenTask()
    .description("Code Analysis")
    .goal("clean install -Dcheck=strict")
    .jdk("JDK 1.8")
    .executableLabel("Maven 3")
    .hasTests(true)
    .workingSubdirectory(RepositoryConstants.HARVESTER_WORKING_DIR)
    .useMavenReturnCode(true);

    public static final Task<?, ?> CHECK_MAVEN_SNAPSHOTS_TASK = new ScriptTask()
    .description("Check Snapshot Versions")
    .location(ScriptTaskProperties.Location.FILE)
    .fileFromPath(RepositoryConstants.BAMBOO_SCRIPTS_WORKING_DIR + "/plans/codeAnalysis/fail-if-has-snapshots.sh")
    .argument("\"" + RepositoryConstants.HARVESTER_WORKING_DIR + "/pom.xml\"");


    /**
     * Private Constructor, because this is a static class.
     */
    private BambooConstants()
    {

    }
}
