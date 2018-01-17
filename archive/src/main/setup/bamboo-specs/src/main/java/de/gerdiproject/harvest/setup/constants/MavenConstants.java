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

import com.atlassian.bamboo.specs.api.builders.task.Task;
import com.atlassian.bamboo.specs.builders.task.MavenTask;




/**
 * A static collection of Maven related constants.
 *
 * @author Robin Weiss
 */
public class MavenConstants
{
    public static final String POM_XML_PATH = "%s/pom.xml";
    public static final Pattern EMAIL_TAG_PATTERN = Pattern.compile("\\s*<email>([\\d\\D]+?)</email>\\s*");
    public static final String DEVELOPERS_OPENING_TAG = "<developers>";
    public static final String DEVELOPERS_CLOSING_TAG = "</developers>";


    // Tasks
    public static final Task<?, ?> MAVEN_INSTALL_STRICT_TASK = new MavenTask()
    .description("Maven: Code Style Check")
    .goal("clean install -Pstrict")
    .jdk("JDK 1.8")
    .executableLabel("Maven 3")
    .hasTests(true)
    .useMavenReturnCode(true);


    /**
     * Private Constructor, because this is a static class.
     */
    private MavenConstants()
    {

    }
}
