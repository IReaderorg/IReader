#!/bin/bash

if [[ "$*" == *"preview"* ]]; then
    name="IReader-Preview"
else
    name="IReader"
fi

msi="$(find ./ -iname '*.msi' 2>/dev/null)"
if [ -f "$msi" ]; then
  dir="$(dirname "$msi")"
  version=$(tmp="${msi%.*}" && echo "${tmp##*-}")

  if [ "$(basename "$msi")" != "$name-windows-x64-$version.msi" ]; then
    mv "$msi" "$dir/$name-windows-x64-$version.msi"
  fi
fi

dmg="$(find ./ -iname '*.dmg' 2>/dev/null)"
if [ -f "$dmg" ]; then
  dir="$(dirname "$dmg")"
  version=$(tmp="${dmg%.*}" && echo "${tmp##*-}")

  if [ "$(basename "$dmg")" != "$name-macos-x64-$version.dmg" ]; then
    mv "$dmg" "$dir/$name-macos-x64-$version.dmg"
  fi
fi

apk="$(find ./ -iname '*.apk' 2>/dev/null)"
if [ -f "$apk" ]; then
  dir="$(dirname "$apk")"

  if [ "$(basename "$apk")" != "$name-android.apk" ]; then
    mv "$apk" "$dir/$name-android.apk"
  fi
fi
