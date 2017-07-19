#!/usr/bin/env python

import sys
import subprocess
import re


def run(args):
    print "\n", '-' * 80
    print '> ', args
    print '-' * 80
    try:
        print subprocess.check_output(args, shell=True)
    except subprocess.CalledProcessError, e:
        print "Error:\n", e.output


def find_versions():
    versions = [0, 0, 0]

    with open('versions.gradle') as file:
        data = file.read()

    majorRE = re.search('appVersionMajor = (\d+)', data)
    if majorRE:
        versions[0] = int(majorRE.group(1))

    minorRE = re.search('appVersionMinor = (\d+)', data)
    if minorRE:
        versions[1] = int(minorRE.group(1))

    patchRE = re.search('appVersionPatch = (\d+)', data)
    if patchRE:
        versions[2] = int(patchRE.group(1))

    return versions


def generate_version(versions):

    content = """ext {
\tappVersionMajor = %d
\tappVersionMinor = %d
\tappVersionPatch = %d
}
""" % (versions[0], versions[1], versions[2])

    with open('versions.gradle', 'w') as file:
        file.write(content)

    return content


def commit(versions):

    version = '.'.join(map(str, versions))

    GIT_STATUS = 'git status --branch'
    GIT_ADD = 'git add versions.gradle'
    GIT_COMMIT = 'git commit -m \'Increase version\''
    GIT_TAG = 'git tag -a %s -m %s' % (version, version)
    GIT_PUSH_TAG = "git push origin %s" % (version)
    GIT_PUSH_HEAD = "git push origin HEAD"

    run(GIT_STATUS)

    run(GIT_ADD)
    run(GIT_COMMIT)

    run(GIT_STATUS)

    run(GIT_TAG)
    run(GIT_PUSH_TAG)
    run(GIT_PUSH_HEAD)

    run(GIT_STATUS)


if len(sys.argv) < 2:
    sys.exit('Error: No arguments')

option = sys.argv[1]
if option not in ['major', 'minor', 'patch']:
    sys.exit('Error: Invalid argument')

versions = find_versions()
print 'Found versions: %s' % (versions)

if len(versions) != 3:
    sys.exit('Error: An error occured while trying to read current versions')

if option == 'major':
    versions[0] = versions[0] + 1
    versions[1] = 0
    versions[2] = 0

elif option == 'minor':
    versions[1] = versions[1] + 1
    versions[2] = 0

elif option == 'patch':
    versions[2] = versions[2] + 1

generate_version(versions)
commit(versions)
