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
#  1 providerName
#  2 providerUrl
#  3 authorFullName
#  4 authorEmail
#  5 authorOrganization
#  6 authorOrganizationUrl
#  7 parentHarvesterVersion


#########################
#  FUNCTION DEFINITIONS #
#########################

# Returns a Java naming compliant class name by removing illegal characters from a 
# specified string and seting the characters that follow the removed characters to upper case.
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
 projectRoot=$(git rev-parse --show-toplevel)
 if [ "$projectRoot" = "" ]; then
   projectRoot="."
 fi
 # get image name from the remote origin URL of the git config file
 gitConfig=$(cat projectRoot/.git/config)
 if [ "$gitConfig" != "" ]; then
   echo "Reading .git/config" >&2
   repoSlug=${gitConfig#*\[remote \"origin\"\]}
   repoSlug=${repoSlug#*url = }
   repoSlug=${repoSlug%.git*}
   repoSlug=${repoSlug##*/}
   echo "$repoSlug"
 else
   echo "Could not read .git/config" >&2
 fi
}


# Recursively replaced placeholders in files, directories and file content of a specified directory.
#  Arguments:
#  1 - the directory that is to be processed
#
RenameFilesInDirectory () {
 for file in $1/*
 do
  if [ -d "$file" ]; then
    newDirName=$(RenameDirectory "$file")
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
 renamedFile=$(echo "$1" | sed -e "s~\${providerClassName}~${providerClassName}~g" -e "s~\${void}~~g")
 if [ "$renamedFile" != "$1" ]; then
   mv -f "$1" "$renamedFile"
   echo "Renamed $1 to $renamedFile" >&2
 fi
}


# Replaces the ${providerPackageName} placeholder of a specified directory.
#  Arguments:
#  1 - the directory that is to be renamed
#
RenameDirectory  () {
 originalDir=$(realpath "$1")
 renamedDir=$(realpath -q $(echo "$1" | sed -e "s~\${providerPackageName}~${providerPackageName}~g"))
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
 sed --in-place=.tmp -e "s~\${providerPackageName}~${providerPackageName}~g" \
     --in-place=.tmp -e "s~\${providerClassName}~${providerClassName}~g" \
     --in-place=.tmp -e "s~\${providerName}~${providerName}~g" \
     --in-place=.tmp -e "s~\${providerUrl}~${providerUrl}~g" \
     --in-place=.tmp -e "s~\${authorFullName}~${authorFullName}~g" \
     --in-place=.tmp -e "s~\${authorEmail}~${authorEmail}~g" \
     --in-place=.tmp -e "s~\${authorOrganization}~${authorOrganization}~g" \
     --in-place=.tmp -e "s~\${authorOrganizationUrl}~${authorOrganizationUrl}~g" \
     --in-place=.tmp -e "s~\${creationYear}~${creationYear}~g" \
     --in-place=.tmp -e "s~\${parentHarvesterVersion}~${parentHarvesterVersion}~g" $1 && rm -f $1.tmp
}


###########################
#  BEGINNING OF EXECUTION #
###########################

echo "Renaming setup files" >&2

# convert arguments to readable variables, replace all ~ with -, because ~ is used for escaping the sed command
providerName=$(echo "$1" | tr '~' '-')
providerUrl=$(echo "$2" | tr '~' '-')
authorFullName=$(echo "$3" | tr '~' '-')
authorEmail=$(echo "$4" | tr '~' '-')
authorOrganization=$(echo "$5" | tr '~' '-')
authorOrganizationUrl=$(echo "$6" | tr '~' '-')
parentHarvesterVersion=$(echo "$7" | tr '~' '-')
creationYear=$(GetCreationYear)

# create class and package names by removing illegal chars and forcing camel-case
illegalChars="  _-\\/{}()"
providerClassName=$(GetProviderClassName "$providerName" "$illegalChars")
providerPackageName=$(GetProviderPackageName "$providerClassName")

# run the main function on the current folder
RenameFilesInDirectory "$PWD"