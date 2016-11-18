# TOE Installer

## Release trigger branch

This branch is a release trigger. It means that **whenever a commit is pushed on this branch**, a release job will be launched, based on properties set in *release.properties* file.

## How to trigger a release ?

To trigger a new release of TOE Installer, follow these steps:

* checkout this branch (after cloning the repository):
```shell
git checkout release
```

* edit Release Version:
```shell
RELEASE_VERSION=0.0.1 && sed -i "s/\(RELEASE_VERSION=\).*\$/\1${RELEASE_VERSION}/" release.properties
```

* edit Development Version:
```shell
DEV_VERSION=0.0.2-SNAPSHOT && sed -i "s/\(DEV_VERSION=\).*\$/\1${DEV_VERSION}/" release.properties
```

* commit the release information:
```shell
git add release.properties && git commit -m "Triggering release"
```

* trigger the release by pushing to the release branch:
```shell
git push origin release
```

## How to trigger a full release with parent modules ?

Simply run:
```shell
T3_RELEASE_VERSION=0.0.1 && \
T3_DEV_VERSION=0.0.2-SNAPSHOT && \
TOE_RELEASE_VERSION=0.0.1 && \
TOE_DEV_VERSION=0.0.2-SNAPSHOT && \
RELEASE_VERSION=0.0.1 && \
DEV_VERSION=0.0.2-SNAPSHOT && \
./fullRelease.sh $T3_RELEASE_VERSION $T3_DEV_VERSION $TOE_RELEASE_VERSION $TOE_DEV_VERSION $RELEASE_VERSION $DEV_VERSION
```
