#!/bin/sh
find . -name "*.java" -type f -exec grep --color=auto -Hn TODO {} \;
