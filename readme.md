About
	This project is used by a Bamboo plan in order to prepare a GeRDI harvester repository, adding all necessary base files
	and creating additional Bamboo jobs.

Creating a new Harvester Project via Bamboo
	In order to set up a new project, follow this link: https://ci.gerdi-project.de/browse/UTIL-CHP
	1. You need to click on the "Run" button at the top right and select "Run customized...".
	2. Click "Override a variable" five times and change the respective keys to have each "Plan Variable" defined once.
	   You are automatically selected as the author of the project. You need to provide your organization and its URL,
	   the data provider name and URL of the provider that is to be harvested, and your Atlassian password.
	3. Click on "Run" and wait for the job to be finished.
	The job will create a repository and commit some basic files to it. Additionally, a Static Analysis plan and a deployment project 
	are created.
	
Adding Bamboo Jobs to an Existing Harvester Project
	If you already have a Maven Project, but it lacks the Bamboo jobs, you can use this plan: https://ci.gerdi-project.de/browse/UTIL-CHPL
	1. You need to click on the "Run" button at the top right and select "Run customized...".
	2. Click "Override a variable" three times and change the respective keys to have each "Plan Variable" defined once.
	   If you define "replacePlans" as "true", existing Bamboo plans and deployment projects are overwritten.
	   You also need to specify your  Atlassian password in the "gitPassword" parameter.
	3. Click on "Run" and wait for the job to be finished.
	The job will create a Static Analysis plan and a deployment project for the harvester project.