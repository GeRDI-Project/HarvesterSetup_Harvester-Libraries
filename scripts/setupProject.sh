#!/bin/bash

# Copyright © 2019 Robin Weiss (http://www.gerdi-project.de)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

# Description:
# Creates a harvester project directory with a basic set of files that are required
# to start developing a harvester.
#
# Arguments:
#  1 human readable provider name
#  2 provider website URL
#  3 the developer's full name
#  4 the developer's email address
#  5 the organization to which the project developer belongs
#  6 the URL of the organization to which the project developer belongs
#  7 if "true", the latest SNAPSHOT version of the harvester parent pom is used
#    otherwise, the latest released version is chosen
#  8 parent directory of the project directory (default: current directory)

# treat unset variables as an error when substituting
set -u

#########################
#  FUNCTION DEFINITIONS #
#########################

# Returns a Java naming compliant class name by removing illegal characters from a 
# specified string and setting the characters that follow the removed characters to upper case.
#  Arguments:
#  1 - the string that is to be processed
#  2 - a string containing all characters that are to be removed
#
GetProviderClassName () {
  echo "$1" | sed -e "s~[$2]\(\\w\)~\U\1~g" -e "s~[$2]~~g" -e "s~^\(\\w\)~\U\1~g"
}


# This function creates a project directory for the harvester project.
#
# Arguments:
#  1 - the parent directory of the project directory to be created
#  2 - the name of the data provider that is to be harvested
#
CreateProjectDirectory() {
  local parentDir="$1"
  local providerName="$2"
  
  local illegalChars="  _-\\/{}()"
  local dirName="$parentDir/"$(GetProviderClassName "$providerName" "$illegalChars")"-Harvester"
  
  if [ -e "$dirName" ]; then
    local filesInDir=$(ls "$dirName" -A1 | wc -l)
    if [ "$filesInDir" != "0" ]; then
      echo "Could not create directory '$dirName', because it already exists and is not empty!" >&2
      exit 1
    fi
  else
    mkdir -p "$dirName" >&2
  fi
  
  echo "$dirName"
}


# Returns the latest version of a specified GeRDI Maven project.
#
# Arguments:
#  1 - the artifact identifier of the GeRDI Maven project
#  2 - if true, also the versions in the Sonatype repository are checked
#
GetLatestMavenVersion() {
  local artifactId="$1"
  local includeSnapshots="$2"
  local metaData
  
  local releaseVersion=""
  metaData=$(curl -fsX GET http://central.maven.org/maven2/de/gerdi-project/$artifactId/maven-metadata.xml)
  if [ $? -eq 0 ]; then
    releaseVersion=${metaData%</versions>*}
    releaseVersion=${releaseVersion##*<version>}
    releaseVersion=${releaseVersion%</version>*}
  fi
  
  local snapshotVersion=""
  if [ "$includeSnapshots" = true ]; then
    metaData=$(curl -fsX GET https://oss.sonatype.org/content/repositories/snapshots/de/gerdi-project/$artifactId/maven-metadata.xml)
    if [ $? -eq 0 ]; then
	    snapshotVersion=${metaData%</versions>*}
      snapshotVersion=${snapshotVersion##*<version>}
      snapshotVersion=${snapshotVersion%</version>*}
	  fi
  fi
  
  if [ -z "$snapshotVersion" ] || [ "$releaseVersion" \> "$snapshotVersion" ]; then
    echo "$releaseVersion"
  else  
    echo "$snapshotVersion"
  fi
}


# This is the main function to be called when the script is executed.
#
# Arguments: -
#
Main() {
  local providerName="$1"
  local providerUrl="$2"
  local authorFullName="$3"
  local authorEmail="$4"
  local authorOrganization="$5"
  local authorOrganizationUrl="$6"
  local useSnapshots="${7:-false}"
  local parentDir="${8:-.}"
  
  # create project directory
  local projectDir
  projectDir=$(CreateProjectDirectory "$parentDir" "$providerName")
  
  if [ -z "$projectDir" ]; then
    exit 1
  fi
  
  # determine path to template directory
  local scriptDir=$(dirname "$0")
  local sourceDir="$scriptDir/../placeholderProject"
  
  # copy template project to project directory
  cp -rfT "$sourceDir" "$projectDir"
  chmod -R o+rw "$projectDir"
  
  # determine version of parent pom
  local parentPomVersion=$(GetLatestMavenVersion "GeRDI-parent-harvester" "$useSnapshots")
  
  # replace placeholders
  "$scriptDir/renameSetup.sh" \
    "$providerName"\
    "$providerUrl"\
    "$authorFullName"\
    "$authorEmail"\
    "$authorOrganization"\
    "$authorOrganizationUrl"\
    "$parentPomVersion"\
    "$projectDir" >&2
  
  # compile Maven
  mvn compile -Dcheck=disabled -f "$projectDir"
}


###########################
#  BEGINNING OF EXECUTION #
###########################

Main "$@"