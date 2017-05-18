#!/bin/sh

src="$(pwd)/../.configs/fabric.properties"
dst="$(pwd)/fabric.properties"

if ! [ -e "$src" ]; then
    exit 1
fi

if ! [ -e "$dst" ]; then
	cp $src $dst
fi