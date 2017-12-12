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
package de.gerdiproject.harvest.setup.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import com.atlassian.bamboo.specs.api.builders.BambooKey;

import de.gerdiproject.harvest.setup.HarvesterBambooSpecs;
import de.gerdiproject.harvest.setup.constants.BambooConstants;


/**
 * This class retrieves and generates parameters for the {@linkplain HarvesterBambooSpecs}.
 *
 * @author Robin Weiss
 */
public class ProjectUtils
{
    private final String projectRootDirectory;
    /**
     */
    public ProjectUtils()
    {
        projectRootDirectory = getProjectRootDirectory();
    }


    public String getProviderClassName()
    {
        String providerClassName = null;
        File harvesterDir = new File(String.format(BambooConstants.MAIN_HARVESTER_PATH, projectRootDirectory));

        if (harvesterDir.exists() && harvesterDir.isDirectory()) {
            File[] harvesterFiles;

            // try to get all cache files in the folder
            try {
                harvesterFiles = harvesterDir.listFiles(new HarvesterFilenameFilter());
            } catch (SecurityException e) {
                harvesterFiles = null;
            }

            // only continue if there are matching files
            if (harvesterFiles != null) {
                long oldestFileCreationDate = Long.MAX_VALUE;
                File oldestHarvesterFile = null;

                for (File harvesterFile : harvesterFiles) {
                    try {
                        // read the creation date of the file
                        BasicFileAttributes fileAttributes = Files.readAttributes(harvesterFile.toPath(), BasicFileAttributes.class);
                        long fileCreationDate = fileAttributes.creationTime().toMillis();

                        if (fileCreationDate < oldestFileCreationDate) {
                            oldestFileCreationDate = fileCreationDate;
                            oldestHarvesterFile = harvesterFile;
                        }
                    } catch (IOException e) {
                        // nothing todo here, just skip the file
                    }
                }

                // if we found an oldest harvester file, retrieve the prefix
                if (oldestHarvesterFile != null) {
                    Matcher fileNameMatcher = BambooConstants.HARVESTER_FILE_PATTERN.matcher(oldestHarvesterFile.getName());

                    if (fileNameMatcher.matches())
                        providerClassName = fileNameMatcher.group(1);
                }
            }
        }

        return providerClassName;
    }


    public List<String> getDeveloperEmailAddresses()
    {
        List<String> mailList = new LinkedList<>();

        try {
            // open pom.xml
            String pomPath = String.format(BambooConstants.POM_XML_PATH, projectRootDirectory);
            BufferedReader pomReader = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(pomPath),
                    StandardCharsets.UTF_8));

            // look for developer email addresses in the pom.xml
            String line = pomReader.readLine();

            // skip everything before the <developers> tag
            while (line != null && !line.contains(BambooConstants.DEVELOPERS_OPENING_TAG))
                line = pomReader.readLine();

            // look for <email> tags, abort when the </developers> end-tag is found
            while (line != null && !line.contains(BambooConstants.DEVELOPERS_CLOSING_TAG)) {
                Matcher emailMatcher = BambooConstants.EMAIL_TAG_PATTERN.matcher(line);

                if (emailMatcher.matches())
                    mailList.add(emailMatcher.group(1));

                line = pomReader.readLine();
            }

            pomReader.close();

        } catch (IOException e) {
            // nothing to do here
        }

        return mailList;
    }


    private String getProjectRootDirectory()
    {
        String projectRootDir = null;

        try {
            Process pr = Runtime.getRuntime().exec(BambooConstants.GIT_GET_ROOT_COMMAND);

            BufferedReader gitRootCommandReader = new BufferedReader(
                new InputStreamReader(
                    pr.getInputStream(),
                    StandardCharsets.UTF_8));
            projectRootDir = gitRootCommandReader.readLine();

        } catch (IOException e) {
            // nothing to do here
        }

        return projectRootDir;
    }


    public String getRepositorySlug()
    {
        String repositorySlug = null;

        try {
            // open config file
            String gitConfigPath = String.format(BambooConstants.GIT_CONFIG_PATH, projectRootDirectory);
            BufferedReader gitConfigReader = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(gitConfigPath),
                    StandardCharsets.UTF_8));

            // look for a git URL in the config file
            String line = gitConfigReader.readLine();

            while (line != null) {
                Matcher lineMatcher = BambooConstants.REPOSITORY_SLUG_PATTERN.matcher(line);

                if (lineMatcher.matches()) {
                    repositorySlug = lineMatcher.group(1);
                    break;
                }

                line = gitConfigReader.readLine();
            }

            gitConfigReader.close();
        } catch (IOException e) {
            // nothing to do here
        }

        return repositorySlug;
    }


    public BambooKey createBambooKey(String providerClassName)
    {
        return new BambooKey(providerClassName.replaceAll("[a-z]", "") + "HAR");
    }

}
