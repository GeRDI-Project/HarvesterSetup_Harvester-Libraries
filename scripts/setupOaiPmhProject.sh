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
# Creates an OAI-PMH harvester project directory with a set of files that are required
# to run an pre-configured OAI-PMH harvester via Docker.
#
# Arguments:
#  1 an OAI-PMH URL of the repository that is to be harvested
#  2 the metadataPrefix that is used to retrieve the OAI-PMH records
#  3 if "true", the latest nightly version of the OAI-PMH harvester is used
#    otherwise, the latest released version is chosen (default: false)
#  4 parent directory of the project directory (default: current directory)

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


# Checks if a specified metadataPrefix is supported by an OAI-PMH repository.
# 
# Arguments:
#  1 - the OAI-PMH base URL without any query parameters
#  2 - the metadata prefix that is to be checked
#
ExitIfMetadataPrefixIsNotSupported() {
  local oaiPmhBaseUrl="$1"
  local metadataPrefix="$2"
  
  local response=$(curl -sX GET "$oaiPmhBaseUrl?verb=ListMetadataFormats")
  if [ -z "$response" ]; then
    echo "Invalid OAI-PMH URL '$oaiPmhBaseUrl'!">&2
	exit 1
  fi
  
  # check if a corresponding metadataPrefix tag exists
  if ! $(echo "$response" | grep -qP "(?<=\<metadataPrefix>)$metadataPrefix(?=\</metadataPrefix>)"); then
    local validPrefixes
    validPrefixes=$(echo "$response" \
                    | grep -oP "(?<=\<metadataPrefix>).+?(?=\</metadataPrefix>)" \
                    | tr '\n' ',' \
                    | sed -e "s~,~, ~g")
    echo "Unsupported metadataPrefix '$metadataPrefix'! Supported are: ${validPrefixes::-2}">&2
    exit 1
  fi
}


# Retrieves the name of an OAI-PMH repository.
# 
# Arguments:
#  1 - the OAI-PMH base URL without any query parameters
#
GetRepositoryName() {
  local oaiPmhBaseUrl="$1"
  
  curl -sX GET "$oaiPmhBaseUrl?verb=Identify" \
               | grep -oP "(?<=\<repositoryName>).+(?=\</repositoryName>)" \
               | sed -e "s~&amp;~and~g"
}


# Retrieves a simplified name of an OAI-PMH repository from its URL.
# 
# Arguments:
#  1 - the OAI-PMH base URL without any query parameters
#
GetSimplifiedRepositoryName() {
  local oaiPmhBaseUrl="$1"

  local simpleName="${oaiPmhBaseUrl#*//}"
  simpleName="${simpleName%.*}"
  simpleName="${simpleName##*.}"
  
  echo "$simpleName"
}

# Retrieves the highest version number of a list of major.minor.bugfix versions.
# Also takes build versions into consideration e.g. 1.2.3-test4
#  Arguments:
#  1 - a list of versions
GetHighestVersion() {
  local versionList="$1"
  
  local highestMajor
  highestMajor=$(echo "$versionList" | grep -oP "^^\d+" | sort -g | tail -n1)
  
  local highestMinor
  highestMinor=$(echo "$versionList" | grep -oP "(?<=^^$highestMajor\.)\d+" | sort -g | tail -n1)
  
  local highestBugFix
  highestBugFix=$(echo "$versionList" | grep -oP "(?<=^^$highestMajor\.$highestMinor\.)\d+" | sort -g | tail -n1)
  
  local highestBuildNumber
  highestBuildNumber=$(echo "$versionList" \
    | grep -oP "(?<=^^$highestMajor\.$highestMinor\.$highestBugFix).*" \
    | grep -oP "\d+" \
    | sort -g \
    | tail -n1)
  
  local suffix
  suffix=$(echo "$versionList" \
    | grep -oP "(?<=^^$highestMajor\.$highestMinor\.$highestBugFix).*(?=$highestBuildNumber)")
  
  echo "$highestMajor.$highestMinor.$highestBugFix$suffix$highestBuildNumber"
}


# Retrieves the latest tagged  version of the OAI-PMH harvester, which should also be the
# highest Docker image tag.
#
# Arguments:
#  1 - if true, includes release candidates and test builds
#
GetLatestOaiPmhTag() {
 local useNightly="$1"
 
 # retrieve all tags
 local tags
 tags=$(curl -sX GET "https://code.gerdi-project.de/rest/api/1.0/projects/har/repos/oai-pmh/tags/" \
       | grep -oP '(?<={"id":"refs/tags/)[^"]+')

 # filter out non-release tags
 if ! $useNightly; then
   tags=$(echo "$tags" | grep -oP '^\d+\.\d+\.\d+$')
 fi
 
 GetHighestVersion "$tags"
}


# This is the main function to be called when the script is executed.
#
Main() {
  local oaiPmhUrl="$1"
  local metadataPrefix="$2"
  local useNightly="${3:-false}"
  local parentDir="${4:-.}"
  
  # remove potential query parameters
  local oaiPmhBaseUrl="${oaiPmhUrl%%\?*}"
  
  # check if the metadata prefix is valid
  ExitIfMetadataPrefixIsNotSupported "$oaiPmhBaseUrl" "$metadataPrefix"
  
  # retrieve a simplified name of the OAI-PMH repository
  local providerName
  providerName=$(GetSimplifiedRepositoryName "$oaiPmhBaseUrl")
  
  # retrieve the name of the OAI-PMH repository 
  local repositoryName
  repositoryName=$(GetRepositoryName "$oaiPmhBaseUrl")
  
  # determine version of the parent Docker image
  local oaiPmhHarvesterVersion
  oaiPmhHarvesterVersion=$(GetLatestOaiPmhTag "$useNightly")
  
  # create project directory
  local projectDir
  projectDir=$(CreateProjectDirectory "$parentDir" "$providerName")
  
  if [ -z "$projectDir" ]; then
    exit 1
  fi
  
  # determine path to template directory
  local scriptDir=$(dirname "$0")
  local sourceDir="$scriptDir/../placeholderOaiPmhProject"
  
  # copy template project to project directory
  cp -rfT "$sourceDir" "$projectDir"
  chmod -R o+rw "$projectDir"
  
  # replace placeholders
  "$scriptDir/renameOaiPmhSetup.sh" \
    "$repositoryName"\
    "$oaiPmhBaseUrl"\
    "$metadataPrefix"\
    "$oaiPmhHarvesterVersion"\
    "$projectDir" >&2
}


###########################
#  BEGINNING OF EXECUTION #
###########################

Main "$@"