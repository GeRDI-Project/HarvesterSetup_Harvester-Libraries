echo "Renaming setup files"

# function definitions

GetProviderClassName () {
 echo "$1" | sed -e "s/[$2]\(\\w\)/\U\1/g" -e "s/[$2]//g" -e "s/^\(\\w\)/\U\1/g"
}


GetProviderPackageName () {
 echo "$1" | tr '[:upper:]' '[:lower:]'
}


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


ProcessFilesInDir () {
 for file in $1/*
 do
  if [ -d $file ]; then
    newDirName=$(RenameDirectory $file)
    ProcessFilesInDir $newDirName
  elif [ -f $file ]; then
    RenameFileContent $file
    RenameFile $file
  fi
 done
}


RenameFile  () {
 renamedFile=$(echo $1 | sed -e "s/\${providerClassName}/${providerClassName}/g" -e "s/\${void}//g")
 if [ "$renamedFile" != "$1" ]; then
   mv -f $1 $renamedFile
   echo "Renamed $1 to $renamedFile" >&2
 fi
}


RenameDirectory  () {
 originalDir=$(realpath $1)
 renamedDir=$(realpath -q $(echo $1 | sed -e "s/\${providerPackageName}/${providerPackageName}/g"))
 if [ "$originalDir" != "$renamedDir" ]; then
   mv -f $originalDir $renamedDir
   echo "Renamed $1 to $renamedDir" >&2
 fi
 echo "$renamedDir"
}


RenameFileContent  () {
 sed --in-place=.tmp -e "s/\${providerPackageName}/${providerPackageName}/g" \
     --in-place=.tmp -e "s/\${providerClassName}/${providerClassName}/g" \
     --in-place=.tmp -e "s/\${providerName}/${providerName}/g" \
     --in-place=.tmp -e "s/\${providerUrl}/${providerUrl}/g" \
     --in-place=.tmp -e "s/\${authorFullName}/${authorFullName}/g" \
     --in-place=.tmp -e "s/\${authorEmail}/${authorEmail}/g" \
     --in-place=.tmp -e "s/\${authorOrganization}/${authorOrganization}/g" \
     --in-place=.tmp -e "s/\${authorOrganizationUrl}/${authorOrganizationUrl}/g" \
     --in-place=.tmp -e "s/\${parentHarvesterVersion}/${parentHarvesterVersion}/g" $1 && rm -f $1.tmp
}


# convert arguments to readable names
providerName=$1
providerUrl=$2
authorFullName=$3
authorEmail=$4
authorOrganization=$5
authorOrganizationUrl=$6
parentHarvesterVersion=$7

# create class and package names by removing illegal chars and forcing camel-case
illegalChars="  _-\\/{}()"
providerClassName=$(GetProviderClassName "$providerName" "$illegalChars")
providerPackageName=$(GetProviderPackageName "$providerClassName")

# run the main function on the current folder
ProcessFilesInDir ${PWD}