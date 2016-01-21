#!/bin/bash -e

DEPLOY=false
DOC=false
FINAL=false # including rc and called dev releases
BUILD_TAG=false # for rc

if [[ ${TRAVIS_BRANCH} =~ ^(master|[0-9]+.[0-9]+)$ ]] && [ ${TRAVIS_PULL_REQUEST} == false ]
then
    DOC=true
fi

if [[ ${TRAVIS_TAG} =~ ^[0-9]+.[0-9]+.[0-9]+(dev[0-9]+|rc[0-9]|\.[0-9]+)?$ ]]
then
    if [ ${TRAVIS_TAG} != $(python setup.py -V) ]
    then
        echo "The tag name doesn't match with the egg version."
        exit 1
    fi
    DEPLOY=true
    FINAL=true
fi

if [[ ${TRAVIS_TAG} =~ ^[0-9]+.[0-9]+.[0-9]+(rc[0-9]+|dev[0-9]+)$ ]]
then
    if [ ${TRAVIS_TAG} != $(python setup.py -V) ]
    then
        echo "The tag name doesn't match with the egg version."
        exit 1
    fi
    DEPLOY=true
    FINAL=true
fi
if [[ ${TRAVIS_TAG} =~ ^0.0.[0-9a-f]*$ ]]
then
    DEPLOY=true
fi


if [ ${DEPLOY} == true  ] && [ ${TRAVIS_PYTHON_VERSION} == "2.7" ]
then
    echo == Do the release ==

    set -x

    .build/venv/bin/pip install wheel
    if [ ${FINAL} == true ]
    then
        .build/venv/bin/python setup.py egg_info --no-date --tag-build "" bdist_wheel
    else
        .build/venv/bin/python setup.py bdist_wheel
    fi
fi

echo == Build the doc ==
if [ ${DOC} == false ]
then
    TRAVIS_BRANCH=master
fi

GIT_REV=`git log | head -n 1 | awk '{{print $2}}'`
git fetch origin gh-pages:gh-pages
git checkout gh-pages

mkdir -p ${TRAVIS_BRANCH}
rm -rf ${TRAVIS_BRANCH}/*
mv doc/_build/html/* ${TRAVIS_BRANCH}

if [ ${DOC} == true ]
then
    git add --all ${TRAVIS_BRANCH}
    git commit -m "Update documentation for the revision ${TRAVIS_COMMIT}"
    git push origin gh-pages
else
    git checkout master/searchindex.js
    git status
    git diff
    git reset --hard
fi

# back to the original branch
git checkout ${GIT_REV}