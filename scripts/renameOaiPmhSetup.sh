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
# This script renames certain placeholders in OAI-PMH template files.
#
# Arguments:
#  1 human readable OAI-PMH repository name
#  2 the OAI-PMH base URL without any query parameters
#  3 the metadata prefix that is used to retrieve records
#  4 the version of the OAI-PMH harvester Docker image

# treat unset variables as an error when substituting
set -u

#########################
#  FUNCTION DEFINITIONS #
#########################


Main() {
  # convert arguments to readable variables, replace all ~ with -, because ~ is used for escaping the sed command
  local repositoryName=$(echo "$1" | tr '~' '-')
  local oaiPmhBaseUrl=$(echo "$2" | tr '~' '-')
  local metadataPrefix=$(echo "$3" | tr '~' '-')
  local oaiPmhHarvesterVersion=$(echo "$4" | tr '~' '-')
  local targetDir="${5:-.}"
  
  # within the specified folder, replace all placeholders inside file content
  echo "Renaming OAI-PMH harvester project files in: $targetDir" >&2
  for file in "$targetDir"/*
  do
    if [ -f "$file" ]; then
      sed --in-place=.tmp -e "s~\${repositoryName}~${repositoryName}~g" \
          --in-place=.tmp -e "s~\${oaiPmhBaseUrl}~${oaiPmhBaseUrl}~g" \
          --in-place=.tmp -e "s~\${metadataPrefix}~${metadataPrefix}~g" \
          --in-place=.tmp -e "s~\${oaiPmhHarvesterVersion}~${oaiPmhHarvesterVersion}~g" \
	  "$file" && rm -f "$file.tmp"
    fi
  done
}


###########################
#  BEGINNING OF EXECUTION #
###########################

Main "$@"