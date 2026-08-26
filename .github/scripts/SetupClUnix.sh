#!/bin/bash

if [ "$(basename "$(pwd)")" = "scripts" ]; then
  cd ..
fi

echo "Writing ci gradle.properties"
mkdir -p "$HOME/.gradle"
cp ".github/runner-files/ci-gradle.properties" "$HOME/.gradle/gradle.properties"
# Also append/override to project gradle.properties
cat ".github/runner-files/ci-gradle.properties" >> "gradle.properties"