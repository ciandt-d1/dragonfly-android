#!/bin/sh

major=$1
minor=$2
patch=$3
version=$major.$minor.$patch

FILE=../versions.gradle

VERSION=$(cat <<-END
ext {
\n\tappVersionMajor = $major
\n\tappVersionMinor = $minor
\n\tappVersionPatch = $patch
\n}
END
)

echo $VERSION > $FILE

if [ -n "$(git diff $FILE)" ]; then
    echo "git add $FILE"
    git add $FILE

    echo "git commit -m 'Increase version'"
    git commit -m 'Increase version'

    echo "git tag -a $version -m $version"
    git tag -a $version -m $version

    echo "git push origin $version"
    git push origin $version
fi