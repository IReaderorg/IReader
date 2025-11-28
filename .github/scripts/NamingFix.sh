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

# Rename all signed APKs to IReader-{arch}.apk format
for apk in $(find ./ -iname '*-signed.apk' 2>/dev/null); do
  if [ -f "$apk" ]; then
    dir="$(dirname "$apk")"
    filename="$(basename "$apk")"
    
    # Extract architecture from filename (e.g., android-standard-arm64-v8a-release-unsigned-signed.apk)
    if [[ "$filename" =~ (arm64-v8a|armeabi-v7a|x86_64|x86) ]]; then
      arch="${BASH_REMATCH[1]}"
      new_name="$name-$arch.apk"
      
      if [ "$filename" != "$new_name" ]; then
        mv "$apk" "$dir/$new_name"
      fi
    fi
  fi
done
