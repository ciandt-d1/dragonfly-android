#!/usr/bin/env groovy

static int[] findVersions() {
    int[] versions = [0, 0, 0]

    File file = new File("versions.gradle")

    def majorRE = file.text =~ /appVersionMajor = (\d+)/
    if (majorRE.find()) {
        versions[0] = Integer.valueOf(majorRE.group(1))
    }

    def minorRE = file.text =~ /appVersionMinor = (\d+)/
    if (minorRE.find()) {
        versions[1] = Integer.valueOf(minorRE.group(1))
    }

    def patchRE = file.text =~ /appVersionPatch = (\d+)/
    if (patchRE.find()) {
        versions[2] = Integer.valueOf(patchRE.group(1))
    }

    return versions
}


static String generateVersion(int[] versions) {

    def content = """ext {
	appVersionMajor = ${versions[0]}
	appVersionMinor = ${versions[1]}
	appVersionPatch = ${versions[2]}
}
"""

    File file = new File("versions.gradle")
    file.text = content

    return content
}


static void commit(int[] versions) {

    def version = "${versions[0]}.${versions[1]}.${versions[2]}"

    def git_add = "git add versions.gradle"
    def git_commit = "git commit -m 'Increase version'"
    def git_tag = "git tag -a $version -m $version"
    def git_push_tag = "git push origin $version"
    def git_push_head = "git push origin HEAD"

    println git_add
    println git_add.execute().text

    println git_commit
    println git_commit.execute().text

    println git_tag
    println git_tag.execute().text

    println git_push_tag
    println git_push_tag.execute().text

    println git_push_head
    println git_push_head.execute().text
}


if (args.length == 0) {
    println "No arguments"
    return
}

options = ['major', 'minor', 'patch']
option = args[0]

if (!(option in options)) {
    println "Invalid argument"
    return
}

int[] versions = findVersions()
if (versions.length != 3) {
    println "An error occured while trying to read current versions"
    return
}

if (option == "major") {
    versions[0]++
    versions[1] = 0
    versions[2] = 0

} else if (option == "minor") {
    versions[1]++
    versions[2] = 0

} else if (option == "patch") {
    versions[2]++
}

generateVersion(versions)
commit(versions)