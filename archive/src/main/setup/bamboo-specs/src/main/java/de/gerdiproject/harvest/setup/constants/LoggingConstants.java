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



/**
 * A static collection of constants for logging purposes.
 *
 * @author Robin Weiss
 */
public class LoggingConstants
{
    public static final String PROVIDER_CLASS_NAME = "ProviderClassName: ";
    public static final String REPOSITORY_SLUG = "RepositorySlug: ";
    public static final String BAMBOO_KEY = "BambooKey: ";
    public static final String PROJECT_ROOT_DIR = "ProjectDirectory: ";
    public static final String DEVELOPER_EMAILS = "DeveloperEmails:";
    public static final String CONNECTING_TO_SERVER = "Connecting to Bamboo Server: %s";

    /**
     * Private Constructor, because this is a static class.
     */
    private LoggingConstants()
    {

    }
}
