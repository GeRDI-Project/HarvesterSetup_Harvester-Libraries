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

import com.atlassian.bamboo.specs.api.builders.deployment.Environment;
import com.atlassian.bamboo.specs.api.builders.deployment.ReleaseNaming;
import com.atlassian.bamboo.specs.api.builders.task.Task;
import com.atlassian.bamboo.specs.builders.task.ArtifactDownloaderTask;
import com.atlassian.bamboo.specs.builders.task.CleanWorkingDirectoryTask;
import com.atlassian.bamboo.specs.builders.task.DownloadItem;
import com.atlassian.bamboo.specs.builders.task.ScriptTask;
import com.atlassian.bamboo.specs.builders.trigger.AfterSuccessfulBuildPlanTrigger;
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
    public static final String TAG_VERSION_VARIABLE = "${bamboo." + VARIABLE_INJECTION_NAMESPACE + ".tag.version}";

    // Generic Bamboo Text
    public static final String BAMBOO_SERVER = "https://ci.gerdi-project.de";
    public static final String LOWER_CASE_REGEX = "[a-z]";

    public static final String DEPLOYMENT_PROJECT_NAME = "%s-Harvester";
    public static final String DEPLOYMENT_PROJECT_DESCRIPTION = "Builds a Docker Image of the Harvester and registers it at the Docker Registry.";
    
    public static final ReleaseNaming DEPLOYMENT_RELEASE_NAMING =
        new ReleaseNaming(TAG_VERSION_VARIABLE)
    .applicableToBranches(true)
    .autoIncrement(false);


    // Projects
    public static final String ANALYSIS_PROJECT_KEY = "CA";

    // Tasks
    public static final Task<?, ?> DOWNLOAD_ALL_TASK = new ArtifactDownloaderTask()
            .description("Download Artifacts")
            .artifacts(new DownloadItem()
                       .allArtifacts(true));
    
    public static final Task<?, ?> DOCKER_PUSH_TASK = new ScriptTask()
    .location(ScriptTaskProperties.Location.FILE)
    .description("Docker Push")
    .fileFromPath(RepositoryConstants.BAMBOO_SCRIPTS_WORKING_DIR + "/docker-push.sh")
    .description("Create and add an image to the Docker registry")
    .argument("\"<maven>\" \"" + TAG_VERSION_VARIABLE + "\" \"<gerdi>\"");

    public static final Task<?, ?> BITBUCKET_TAG_TASK = new ScriptTask()
    .description("Tag Git Repository")
    .location(ScriptTaskProperties.Location.FILE)
    .fileFromPath("./scripts/deployment/tag-bitbucket-repository.sh")
    .argument(TAG_VERSION_VARIABLE);

    // Environments
    public static final String PRODUCTION_ENVIRONMENT_NAME = "Production";
    public static final Environment PRODUCTION_ENVIRONMENT = new Environment(PRODUCTION_ENVIRONMENT_NAME)
    .tasks(new CleanWorkingDirectoryTask(),
           DOWNLOAD_ALL_TASK,
           DOCKER_PUSH_TASK,
           BITBUCKET_TAG_TASK)
    .triggers(new AfterSuccessfulBuildPlanTrigger()
              .triggerByBranch("production"));

    public static final String STAGE_ENVIRONMENT_NAME = "Stage";
    public static final Environment STAGE_ENVIRONMENT = new Environment(STAGE_ENVIRONMENT_NAME)
    .tasks(new CleanWorkingDirectoryTask(),
           DOWNLOAD_ALL_TASK,
           DOCKER_PUSH_TASK,
           BITBUCKET_TAG_TASK)
    .triggers(new AfterSuccessfulBuildPlanTrigger()
              .triggerByBranch("stage"));

    public static final String TEST_ENVIRONMENT_NAME = "Test";
    public static final Environment TEST_ENVIRONMENT = new Environment(TEST_ENVIRONMENT_NAME)
    .tasks(new CleanWorkingDirectoryTask(),
           DOWNLOAD_ALL_TASK,
           DOCKER_PUSH_TASK,
           BITBUCKET_TAG_TASK)
    .triggers(new AfterSuccessfulBuildPlanTrigger());

    /**
     * Private Constructor, because this is a static class.
     */
    private BambooConstants()
    {

    }
}
