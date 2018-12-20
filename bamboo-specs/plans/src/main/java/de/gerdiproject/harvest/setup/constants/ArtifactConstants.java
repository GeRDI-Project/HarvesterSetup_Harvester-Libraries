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

import com.atlassian.bamboo.specs.api.builders.plan.artifact.Artifact;

/**
 * A static collection of Bamboo Specs constants that are used to create and share
 * artifacts.
 *
 * @author Robin Weiss
 */
public class ArtifactConstants
{
    private static final String SCRIPTS_PATTERN = RepositoryConstants.BAMBOO_SCRIPTS_WORKING_DIR + "/**/*";

    // Plan Definitions
    public static final Artifact WAR_ARTIFACT = new Artifact()
    .name("warFile")
    .copyPattern("target/*.war")
    .location(RepositoryConstants.HARVESTER_WORKING_DIR)
    .shared(true);

    public static final Artifact DOCKERFILE_ARTIFACT = new Artifact()
    .name("dockerfile")
    .copyPattern("Dockerfile")
    .location(RepositoryConstants.HARVESTER_WORKING_DIR)
    .shared(true);

    public static final Artifact SCRIPT_ARTIFACTS = new Artifact()
    .name("scripts")
    .copyPattern(SCRIPTS_PATTERN)
    .shared(true);


    /**
     * Private Constructor, because this is a static class.
     */
    private ArtifactConstants()
    {

    }
}
