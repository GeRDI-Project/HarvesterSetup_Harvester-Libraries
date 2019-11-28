#!/bin/bash

# Copyright © 2017 Robin Weiss (http://www.gerdi-project.de)
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
# This script renames certain placeholders in files and file names within the pom.xml and the src
# directory.
#
# Arguments:
#  1 human readable provider name
#  2 provider website URL
#  3 the developer's full name
#  4 the developer's email address
#  5 the organization to which the project developer belongs
#  6 the URL of the organization to which the project developer belongs
#  7 parent pom version
#  8 harvester library version
#  9 renamed root directory (default: current directory)
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


# Returns a Java naming compliant package name by setting a specified string to lower case.
#  Arguments:
#  1 - the string that is to be processed
#
GetProviderPackageName () {
  echo "$1" | tr '[:upper:]' '[:lower:]'
}


# Returns the current year.
#  Arguments: -
#
GetCreationYear () {
  date +"%Y"
}


# Returns the slug of the current git repository.
#  Arguments: -
#
GetRepositorySlug () {
  # get project root directory
  local projectRoot
  projectRoot=$(git rev-parse --show-toplevel)
  if [ "$projectRoot" = "" ]; then
    projectRoot="."
  fi
  
  # get image name from the remote origin URL of the git config file
  local gitConfig
  gitConfig=$(cat projectRoot/.git/config)
  
  if [ "$gitConfig" != "" ]; then
    echo "Reading .git/config" >&2
	local repoSlug
    repoSlug=${gitConfig#*\[remote \"origin\"\]}
    repoSlug=${repoSlug#*url = }
    repoSlug=${repoSlug%.git*}
    repoSlug=${repoSlug##*/}
    echo "$repoSlug"
  else
    echo "Could not read .git/config" >&2
  fi
}


# Within a specified folder and all sub-folders, replaces all placeholders in 
# file- and directory names and inside file content.
#  Arguments:
#  1 - the directory that is to be processed
#
RenameFilesInDirectory () {
  local file
  for file in "$1"/*
  do
    if [ -d "$file" ]; then
      local newDirName=$(RenameDirectory "$file")
      RenameFilesInDirectory "$newDirName"
    elif [ -f "$file" ]; then
      RenameFileContent "$file"
      RenameFile "$file"
    fi
  done
}


# Replaces the ${providerClassName} placeholder of a specified file.
#  Arguments:
#  1 - the file that is to be renamed
#
RenameFile  () {
  local filePath="$1"
  local renamedFilePath=$(echo "$filePath" | sed -e "s~\${providerClassName}~${CLASS_NAME}~g" \
                                -e "s~\${void}~~g")
  if [ "$renamedFilePath" != "$filePath" ]; then
    mv -f "$filePath" "$renamedFilePath"
    echo "Renamed $filePath to $renamedFilePath" >&2
  fi
}


# Replaces the ${providerPackageName} placeholder of a specified directory.
#  Arguments:
#  1 - the directory that is to be renamed
#
RenameDirectory  () {
  local originalDir=$(realpath "$1")
  local renamedDir=$(realpath -q $(echo "$1" | sed -e "s~\${providerPackageName}~${PACKAGE_NAME}~g" \
                                             -e "s~\${providerClassName}~${CLASS_NAME}~g"))
  if [ "$originalDir" != "$renamedDir" ]; then
    mv -f "$originalDir" "$renamedDir"
    echo "Renamed $1 to $renamedDir" >&2
  fi
  echo "$renamedDir"
}


# Replaces all placeholders within a specified text file.
#  Arguments:
#  1 - the file of which the content is to be replaced
#
RenameFileContent  () {
  sed --in-place=.tmp -e "s~\${providerPackageName}~${PACKAGE_NAME}~g" \
      --in-place=.tmp -e "s~\${providerClassName}~${CLASS_NAME}~g" \
      --in-place=.tmp -e "s~\${providerName}~${PROVIDER_NAME}~g" \
      --in-place=.tmp -e "s~\${providerUrl}~${PROVIDER_URL}~g" \
      --in-place=.tmp -e "s~\${authorFullName}~${AUTHOR_NAME}~g" \
      --in-place=.tmp -e "s~\${authorEmail}~${AUTHOR_EMAIL}~g" \
      --in-place=.tmp -e "s~\${authorOrganization}~${AUTHOR_ORGA}~g" \
      --in-place=.tmp -e "s~\${authorOrganizationUrl}~${AUTHOR_ORGA_URL}~g" \
      --in-place=.tmp -e "s~\${creationYear}~${CREATION_YEAR}~g" \
      --in-place=.tmp -e "s~\${parentPomVersion}~${PARENT_POM_VERSION}~g" \
      --in-place=.tmp -e "s~\${harvesterLibraryVersion}~${HAR_LIB_VERSION}~g" \
	  $1 && rm -f $1.tmp
}

Main() {
  # convert arguments to readable variables, replace all ~ with -, because ~ is used for escaping the sed command
  PROVIDER_NAME=$(echo "$1" | tr '~' '-')
  PROVIDER_URL=$(echo "$2" | tr '~' '-')
  AUTHOR_NAME=$(echo "$3" | tr '~' '-')
  AUTHOR_EMAIL=$(echo "$4" | tr '~' '-')
  AUTHOR_ORGA=$(echo "$5" | tr '~' '-')
  AUTHOR_ORGA_URL=$(echo "$6" | tr '~' '-')
  PARENT_POM_VERSION=$(echo "$7" | tr '~' '-')
  HAR_LIB_VERSION=$(echo "$8" | tr '~' '-')
  CREATION_YEAR=$(GetCreationYear)
  
  # create class and package names by removing illegal chars and forcing camel-case
  CLASS_NAME=$(GetProviderClassName "$PROVIDER_NAME" "  _-\\/{}()")
  PACKAGE_NAME=$(GetProviderPackageName "$CLASS_NAME")
  
  # run the main function on the current or selected folder
  local targetDir="${9:-.}"
  echo "Renaming harvester project files in: $targetDir" >&2
  RenameFilesInDirectory "$targetDir"
}


###########################
#  BEGINNING OF EXECUTION #
###########################

Main "$@"