#!/bin/bash

if [ "$(basename "$(pwd)")" = "scripts" ]; then
  cd ..
fi

echo "Writing ci gradle.properties"
[ ! -d "/path/to/dir" ] && mkdir ".gradle"
cp ".github/runner-files/ci-gradle.properties" ".gradle/gradle.properties"