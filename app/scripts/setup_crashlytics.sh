#!/bin/sh

BUILDUSER=$(id -un)
BUILDDATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
GITLASTCOMMIT=$(git log --format=format:'%H' --max-count=1)
GITBRANCH=$(git branch 2>/dev/null | sed -e '/^[^*]/d' -e 's/* //')

HEADER='<?xml version="1.0" encoding="utf-8"?>
<resources>\n'
LINEUSER='	<string name="build_user" translatable="false">%s</string>\n'
LINEBUILDDATE='	<string name="build_date" translatable="false">%s</string>\n'
LINECOMMIT='	<string name="last_commit" translatable="false">%s</string>\n'
LINEBRANCH='	<string name="branch" translatable="false">%s</string>\n'
FOOTER='</resources>'

{
	printf "$HEADER"
	printf "$LINEUSER" "$BUILDUSER"
	printf "$LINEBUILDDATE" "$BUILDDATE"
	printf "$LINECOMMIT" "$GITLASTCOMMIT"
	printf "$LINEBRANCH" "$GITBRANCH"
	printf "$FOOTER"
} > $(pwd)/src/main/res/values/config_crashlytics.xml
