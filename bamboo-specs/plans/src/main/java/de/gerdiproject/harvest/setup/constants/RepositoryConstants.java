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

import com.atlassian.bamboo.specs.api.builders.applink.ApplicationLink;
import com.atlassian.bamboo.specs.api.builders.repository.VcsChangeDetection;
import com.atlassian.bamboo.specs.api.builders.repository.VcsRepository;
import com.atlassian.bamboo.specs.api.builders.task.Task;
import com.atlassian.bamboo.specs.builders.repository.bitbucket.server.BitbucketServerRepository;
import com.atlassian.bamboo.specs.builders.repository.viewer.BitbucketServerRepositoryViewer;
import com.atlassian.bamboo.specs.builders.task.CheckoutItem;
import com.atlassian.bamboo.specs.builders.task.VcsCheckoutTask;



/**
 * A static collection of Bamboo Specs constants that concern repositories and Bitbucket.
 *
 * @author Robin Weiss
 */
public class RepositoryConstants
{
    public static final String GIT_MASTER_BRANCH = "master";

    public static final String BITBUCKET_HARVESTER_NAME = "%s-Harvester";
    public static final String HARVESTER_WORKING_DIR = "sourceCode";
    public static final String BAMBOO_SCRIPTS_WORKING_DIR = "scripts";

    public static final ApplicationLink BITBUCKET_SERVER = new ApplicationLink()
    .name("Bitbucket")
    .id("f0c4a002-9d93-3ac9-b18b-296394ec3180");

    public static final String BITBUCKET_HARVESTER_PROJECT = "HAR";

    // Repositories
    public static final VcsRepository<?, ?> BAMBOO_SCRIPTS_REPOSITORY = new BitbucketServerRepository()
    .name("BambooScripts")
    .repositoryViewer(new BitbucketServerRepositoryViewer())
    .server(RepositoryConstants.BITBUCKET_SERVER)
    .projectKey("UTILS")
    .repositorySlug("bamboo-scripts")
    .branch(RepositoryConstants.GIT_MASTER_BRANCH)
    .changeDetection(new VcsChangeDetection());


    // Tasks
    public static final Task<?, ?> CHECKOUT_HARVESTER_REPO_TASK = new VcsCheckoutTask()
    .description("Checkout Harvester")
    .checkoutItems(new CheckoutItem()
                   .defaultRepository()
                   .path(HARVESTER_WORKING_DIR));

    public static final Task<?, ?> CHECKOUT_BAMBOO_SCRIPTS_REPO_TASK = new VcsCheckoutTask()
    .description("Checkout Bamboo Scripts")
    .checkoutItems(new CheckoutItem()
                   .repository(BAMBOO_SCRIPTS_REPOSITORY.getIdentifier())
                   .path(BAMBOO_SCRIPTS_WORKING_DIR));


    /**
     * Private Constructor, because this is a static class.
     */
    private RepositoryConstants()
    {

    }
}
