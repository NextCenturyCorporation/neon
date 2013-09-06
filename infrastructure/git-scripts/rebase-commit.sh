# After committing to a local branch, this script will attempt to automatically 
# rebase and merge with master. If there are errors, the script will exit.
# Any command line options will be passed through to the git rebase command.

#!/bin/bash

AUTOPUSH=true

# executes the command in $1 with the status message in $2
# if the command fails, the script will exit
executeCommand() {
    echo $2
    eval $1
    local commandStatus=$?
    if [[ $commandStatus -ne 0 ]]
    then
       echo "Error performing automated rebase. See console output."
       exit $commandStatus
    fi
}

handleScriptOption() {
    if [[ $1 == --nopush ]]
    then
      AUTOPUSH=false
    else
      echo "unknown script option $1"
      exit 1
    fi   
}


branch=`git rev-parse --abbrev-ref HEAD`
echo "current branch is $branch"
if [[ $branch == master ]]
then
   echo "cannot rebase from master. did you mean to run it from here?"
   exit 1
fi

REBASE_OPTS=""
# two dash options are script options. single dash get passed to rebase
for var in "$@"
do
  if [[ $var == --* ]]
  then
    handleScriptOption $var
  else    
    spacer=""
    if [[ -n "$var" ]]
    then
      spacer=" "
    fi
    REBASE_OPTS="$REBASE_OPTS$spacer$var"    
  fi
done

executeCommand "git checkout master" "checking out master..."
executeCommand "git pull" "pulling master..."
executeCommand "git checkout $branch" "checking out $branch..."
executeCommand "git rebase master $REBASE_OPTS" "rebasing master..."
executeCommand "git checkout master" "checking out master..."
executeCommand "git merge $branch" "merging branch $branch..."

if $AUTOPUSH
then
  executeCommand "git push" "pushing to remote..."
else
  echo "Autopush disabled"
fi
