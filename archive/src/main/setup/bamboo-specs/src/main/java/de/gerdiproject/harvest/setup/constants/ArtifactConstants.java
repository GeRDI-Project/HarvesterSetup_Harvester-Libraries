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

import com.atlassian.bamboo.specs.api.builders.plan.artifact.Artifact;
import com.atlassian.bamboo.specs.api.builders.task.Task;
import com.atlassian.bamboo.specs.builders.task.ArtifactDownloaderTask;
import com.atlassian.bamboo.specs.builders.task.DownloadItem;



/**
 * A static collection of Bamboo Specs constants that are used to create and share
 * artifacts.
 *
 * @author Robin Weiss
 */
public class ArtifactConstants
{
    //////////////
    // PRIVATE  //
    //////////////

    // Directories
    private static final String TARGET_DIR = "target";
    private static final String SCRIPTS_DIR = "scripts";
    private static final String WORKING_DIR = "";

    // Names
    private static final String WAR_ARTIFACT_NAME = "warFile";
    private static final String SCRIPTS_ARTIFACT_NAME = "scripts";
    private static final String DOCKERFILE_ARTIFACT_NAME = "dockerfile";

    // Copy Patterns
    private static final String WAR_PATTERN = "*.war";
    private static final String SCRIPTS_PATTERN = "**/*";
    private static final String DOCKERFILE_PATTERN = "Dockerfile";


    /////////////
    // PUBLIC  //
    /////////////

    // Plan Definitions
    public static final Artifact WAR_ARTIFACT = new Artifact()
    .name(WAR_ARTIFACT_NAME)
    .copyPattern(WAR_PATTERN)
    .location(TARGET_DIR)
    .shared(true);

    public static final Artifact SCRIPT_ARTIFACTS = new Artifact()
    .name(SCRIPTS_ARTIFACT_NAME)
    .copyPattern(SCRIPTS_PATTERN)
    .location(SCRIPTS_DIR)
    .shared(true);

    public static final Artifact DOCKERFILE_ARTIFACT = new Artifact()
    .name(DOCKERFILE_ARTIFACT_NAME)
    .copyPattern(DOCKERFILE_PATTERN)
    .location(WORKING_DIR)
    .shared(true);


    // Tasks
    public static final Task<?, ?> DOWNLOAD_TASK = new ArtifactDownloaderTask()
    .artifacts(new DownloadItem()
               .artifact(WAR_ARTIFACT_NAME)
               .path(TARGET_DIR),
               new DownloadItem()
               .artifact(SCRIPTS_ARTIFACT_NAME)
               .path(SCRIPTS_DIR),
               new DownloadItem()
               .artifact(DOCKERFILE_ARTIFACT_NAME));


    /**
     * Private Constructor, because this is a static class.
     */
    private ArtifactConstants()
    {

    }
}
