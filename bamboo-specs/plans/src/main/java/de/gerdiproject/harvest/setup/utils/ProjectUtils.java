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
package de.gerdiproject.harvest.setup.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bamboo.specs.api.builders.BambooKey;

import de.gerdiproject.harvest.setup.HarvesterBambooSpecs;
import de.gerdiproject.harvest.setup.constants.BambooConstants;
import de.gerdiproject.harvest.setup.constants.LoggingConstants;
import de.gerdiproject.harvest.setup.constants.MavenConstants;
import de.gerdiproject.harvest.setup.constants.RepositoryConstants;


/**
 * This class retrieves and generates parameters for the {@linkplain HarvesterBambooSpecs}.
 *
 * @author Robin Weiss
 */
public class ProjectUtils
{
    private static Logger LOGGER = LoggerFactory.getLogger(ProjectUtils.class);

    private final String projectRootDirectory;

    /**
     * Constructor that retrieves the root directory of the project.
     */
    public ProjectUtils()
    {
        projectRootDirectory = getProjectRootDirectory();
        LOGGER.info(LoggingConstants.PROJECT_ROOT_DIR + projectRootDirectory);
    }


    /**
     * Looks for the oldest file called *Harvester.java and retrieves the prefix as the
     * camel case data provider name.
     *
     * @return the camel case data provider name
     */
    public String getProviderClassName()
    {
        return System.getenv("testvar");
        /*String providerClassName = null;
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

        return providerClassName;*/
    }


    /**
     * Reads the project's pom.xml, extracting the email addresses from the &lt;developer&gt; tags.
     *
     * @return a list of email addresses from the developers described in the pom.xml
     */
    public List<String> getDeveloperEmailAddresses()
    {
        List<String> mailList = new LinkedList<>();

        try {
            // open pom.xml
            String pomPath = String.format(MavenConstants.POM_XML_PATH, projectRootDirectory);
            BufferedReader pomReader = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(pomPath),
                    StandardCharsets.UTF_8));

            // look for developer email addresses in the pom.xml
            String line = pomReader.readLine();

            // skip everything before the <developers> tag
            while (line != null && !line.contains(MavenConstants.DEVELOPERS_OPENING_TAG))
                line = pomReader.readLine();

            // look for <email> tags, abort when the </developers> end-tag is found
            while (line != null && !line.contains(MavenConstants.DEVELOPERS_CLOSING_TAG)) {
                Matcher emailMatcher = MavenConstants.EMAIL_TAG_PATTERN.matcher(line);

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


    /**
     * Calls a git command in order to retrieve the project root directory,
     *
     * @return the top-most directory of the project
     */
    private String getProjectRootDirectory()
    {
        String projectRootDir = null;

        try {
            Process pr = Runtime.getRuntime().exec(RepositoryConstants.GIT_GET_ROOT_COMMAND);

            BufferedReader gitRootCommandReader = new BufferedReader(
                new InputStreamReader(
                    pr.getInputStream(),
                    StandardCharsets.UTF_8));
            projectRootDir = gitRootCommandReader.readLine();

        } catch (IOException e) {
            // nothing to do here
        }

        // fallback: get the folder above the bamboo-specs directory
        if (projectRootDir == null) {
            projectRootDir = System.getProperty("user.dir");
            int lastSlash = projectRootDir.lastIndexOf('\\');

            if (lastSlash == -1)
                lastSlash = projectRootDir.lastIndexOf('/');

            if (lastSlash != -1)
                projectRootDir = projectRootDir.substring(0, lastSlash);
        }

        return projectRootDir;
    }


    /**
     * Creates a Bamboo key by reading only the upper-case letters of the provider name and appending 'HAR'.
     *
     * @param providerClassName the name of the provider in camel case
     * @param project the name of the Bitbucket project that is linked to the created jobs
     *
     * @return a Bamboo key for harvester plans
     */
    public BambooKey createBambooKey(String providerClassName, String project)
    {
        return new BambooKey(
                   providerClassName.replaceAll(BambooConstants.LOWER_CASE_REGEX, "")
                   + project);
    }
}
