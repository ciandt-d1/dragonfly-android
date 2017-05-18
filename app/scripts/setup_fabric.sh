#!/bin/sh

src="$(pwd)/../.configs/fabric.properties"
dst="$(pwd)/fabric.properties"

if ! [ -e "$src" ]; then
    exit 1
fi

if ! [ -e "$dst" ] || ! cmp $src $dst >/dev/null 2>&1; then
    cp $src $dst
fi