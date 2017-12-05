echo "Renaming setup files"

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
providerClassName=$(echo "$providerName" | sed -e "s/[$illegalChars]\(\\w\)/\U\1/g" -e "s/[$illegalChars]//g" -e "s/^\(\\w\)/\U\1/g")
providerPackageName=$(echo ${providerClassName} | tr '[:upper:]' '[:lower:]')


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
 fi
}


RenameDirectory  () {
 originalDir=$(realpath $1)
 renamedDir=$(realpath -q $(echo $1 | sed -e "s/\${providerPackageName}/${providerPackageName}/g"))
 if [ "$originalDir" != "$renamedDir" ]; then
  mv -f $originalDir $renamedDir
 fi
 echo "$renamedDir"
}


RenameFileContent  () {
 sed --in-place=.tmp -e "s/\${providerPackageName}/${providerPackageName}/g" \
      --in-place=.tmp -e "s/\${providerClassName}/${providerClassName}/g" \
      --in-place=.tmp -e "s/\${providerUrl}/${providerUrl}/g" \
      --in-place=.tmp -e "s/\${authorFullName}/${authorFullName}/g" \
      --in-place=.tmp -e "s/\${authorEmail}/${authorEmail}/g" \
      --in-place=.tmp -e "s/\${authorOrganization}/${authorOrganization}/g" \
      --in-place=.tmp -e "s/\${authorOrganizationUrl}/${authorOrganizationUrl}/g" \
     --in-place=.tmp -e "s/\${parentHarvesterVersion}/${parentHarvesterVersion}/g" $1 && rm -f $1.tmp
}


ProcessFilesInDir ${PWD}