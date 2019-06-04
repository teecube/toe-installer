# T³ - TOE - Products Installer

This branch (*release-trigger-minor*) is a [**release trigger branch**](#release-trigger-branch) for the **T³ - TOE - Products Installer** project.

## Release trigger branch

This branch is a release trigger. It means that **whenever a commit is pushed on this branch**, a release job, defined in [*.gitlab-ci.yml*](./.gitlab-ci.yml) file, will be launched, based on properties set in [*release.properties*](./release.properties) file.

## How to trigger a release ?

There are several methods to trigger a release.
They will all commit & push a change on this *release trigger branch* which will trigger a build on Gitlab CI.

### automatically from Gitlab

To trigger a new release with *minor* increment policy, simply [run a new pipeline](https://git.teecu.be/teecube/toe-installer/pipelines/new) after selecting *release-trigger-minor* in the list.
That's it !

### automatically from this repository

* clone this repository:
```shell
git clone https://git.teecu.be/teecube/toe-installer.git
cd toe-installer
```

* checkout this branch:
```shell
git checkout release-trigger-minor
```

* simply run:
```shell
chmod u+x ./release.sh
. ./release.sh && triggerRelease
```

This script will update the [*release.properties*](./release.properties) file with next versions (based on ```INCREMENT_POLICY``` set in [*release.properties*](./release.properties) and current version set in POM of branch master) then commit and push this file on this *release trigger branch*, hence triggering a release.

### manually

* clone this repository:
```shell
git clone https://git.teecu.be/teecube/toe-installer.git
cd toe-installer
```

* checkout this branch:
```shell
git checkout release-trigger-minor
```

* edit Release Version (*0.0.1* is an example):
```shell
RELEASE_VERSION=0.0.1 && sed -i "s/\(RELEASE_VERSION=\).*\$/\1${RELEASE_VERSION}/" release.properties
```

* edit Development Version (*0.0.2-SNAPSHOT* is an example):
```shell
DEV_VERSION=0.0.2-SNAPSHOT && sed -i "s/\(DEV_VERSION=\).*\$/\1${DEV_VERSION}/" release.properties
```

* commit the release information:
```shell
git add release.properties && git commit -m "Triggering release $RELEASE_VERSION, next development version will be $DEV_VERSION"
```

* trigger the release by pushing to the *release trigger branch*:
```shell
git push origin release-trigger-minor
```

These versions numbers can also be [edited directly in Gitlab](https://git.teecu.be/teecube/toe-installer/edit/release-trigger-minor/release.properties).

## Full documentation

The full documentation for the Maven auto releaser v1.2.0 can be found at https://github.com/debovema/maven-auto-releaser/blob/master/README.md.
