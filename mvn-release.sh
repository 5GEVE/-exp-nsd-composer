#!/usr/bin/env bash

if [[ $(git branch --show-current) != "development" ]]
	then echo "Please move to development branch."
	exit 1
fi

VERSION=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)
BASE=(${VERSION//-/ [0]})

printf "Current version: $VERSION\n"
printf "Releasing to ${BASE}\n"
read -r -p "Proceed? (no push, only commit) [y/N] " response
case "$response" in
    [yY][eE][sS]|[yY])
        mvn versions:set -DnewVersion=${BASE} &&
        git commit -m "Update version to ${BASE} for release." pom.xml &&
        git checkout master &&
        git merge -m "Merge branch 'development' for ${BASE}" development &&
        git tag -a ${BASE} -m "Tag for version ${BASE}"
        git checkout development &&
        read -r -p "New version for development: " new
        mvn versions:set -DnewVersion=${new} &&
        git commit -m "Update version to ${new}." pom.xml
        printf "Remember to push both development and master.\n"
        printf "git push --tags origin master\n"
        printf "git push origin development\n"
        ;;
    *)
        printf "Aborting release.\n"
        ;;
esac
exit 0
