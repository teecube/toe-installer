#!/bin/sh

chmod $RELEASE_SH_CHMOD ./release.sh

. release.properties

# configure repository and checkout master instead of current release branch
git config --global user.name "Mathieu Debove"
git config --global user.email "mad@teecu.be"
git config --global push.default upstream
git branch -d master
git checkout -b multi-installs remotes/origin/multi-installs
git branch --set-upstream-to=origin/multi-installs multi-installs
