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

import com.atlassian.bamboo.specs.api.builders.plan.branches.BranchCleanup;
import com.atlassian.bamboo.specs.api.builders.plan.branches.PlanBranchManagement;
import com.atlassian.bamboo.specs.api.builders.task.Task;
import com.atlassian.bamboo.specs.builders.task.CheckoutItem;
import com.atlassian.bamboo.specs.builders.task.VcsCheckoutTask;



/**
 * A static collection of Bamboo Specs constants that concern repositories and Bitbucket.
 *
 * @author Robin Weiss
 */
public class RepositoryConstants
{
    public static final Pattern REPOSITORY_SLUG_PATTERN = Pattern.compile("\\s*url\\s?=[\\d\\D]+?/([^/]+?).git");

    public static final String GIT_CONFIG_PATH = "%s/.git/config";
    public static final String GIT_GET_ROOT_COMMAND = "git rev-parse --show-toplevel";
    public static final String GIT_MASTER_BRANCH = "master";

    public static final String BITBUCKET_HARVESTER_NAME = "%s-Harvester";
    public static final String BITBUCKET = "Bitbucket";
    public static final String BITBUCKET_ID = "f0c4a002-9d93-3ac9-b18b-296394ec3180";
    public static final String BITBUCKET_HARVESTER_PROJECT = "HAR";


    // Tasks
    public static final Task<?, ?> CHECKOUT_TASK = new VcsCheckoutTask()
    .description("Checkout Default Repository")
    .checkoutItems(new CheckoutItem().defaultRepository());


    // Branch Management
    public static final PlanBranchManagement MANUAL_BRANCH_MANAGEMENT  = new PlanBranchManagement()
    .delete(new BranchCleanup())
    .notificationForCommitters();


    /**
     * Private Constructor, because this is a static class.
     */
    private RepositoryConstants()
    {

    }
}
