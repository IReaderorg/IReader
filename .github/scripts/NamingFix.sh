#!/bin/bash

if [[ "$*" == *"preview"* ]]; then
    name="IReader-Preview"
else
    name="IReader"
fi

# Rename MSI files
msi="$(find ./ -iname '*.msi' 2>/dev/null)"
if [ -f "$msi" ]; then
  dir="$(dirname "$msi")"
  version=$(tmp="${msi%.*}" && echo "${tmp##*-}")

  if [ "$(basename "$msi")" != "$name-windows-x64-$version.msi" ]; then
    mv "$msi" "$dir/$name-windows-x64-$version.msi"
  fi
fi

# Rename EXE files
exe="$(find ./ -iname '*.exe' 2>/dev/null)"
if [ -f "$exe" ]; then
  dir="$(dirname "$exe")"
  version=$(tmp="${exe%.*}" && echo "${tmp##*-}")

  if [ "$(basename "$exe")" != "$name-windows-x64-$version.exe" ]; then
    mv "$exe" "$dir/$name-windows-x64-$version.exe"
  fi
fi

# Rename DMG files
dmg="$(find ./ -iname '*.dmg' 2>/dev/null)"
if [ -f "$dmg" ]; then
  dir="$(dirname "$dmg")"
  version=$(tmp="${dmg%.*}" && echo "${tmp##*-}")

  if [ "$(basename "$dmg")" != "$name-macos-x64-$version.dmg" ]; then
    mv "$dmg" "$dir/$name-macos-x64-$version.dmg"
  fi
fi

# Rename tar.gz files
for tg in $(find ./ -iname '*.tar.gz' 2>/dev/null); do
  if [ -f "$tg" ]; then
    dir="$(dirname "$tg")"
    filename="$(basename "$tg")"
    # Extract version from filename (e.g., IReader-linux-x64-2.0.18.tar.gz or IReader-Preview-linux-x64-r123.tar.gz)
    if [[ "$filename" =~ ([0-9]+\.[0-9]+\.[0-9]+) ]]; then
      version="${BASH_REMATCH[1]}"
      new_name="$name-linux-x64-$version.tar.gz"
      if [ "$filename" != "$new_name" ]; then
        mv "$tg" "$dir/$new_name"
      fi
      # Also rename sha256 file if present
      sha_file="${tg}.sha256"
      if [ -f "$sha_file" ]; then
        mv "$sha_file" "$dir/$new_name.sha256"
      fi
    elif [[ "$filename" =~ r([0-9]+) ]]; then
      # Preview build with commit count (e.g., r123)
      commit_count="${BASH_REMATCH[1]}"
      new_name="$name-linux-x64-r$commit_count.tar.gz"
      if [ "$filename" != "$new_name" ]; then
        mv "$tg" "$dir/$new_name"
      fi
      sha_file="${tg}.sha256"
      if [ -f "$sha_file" ]; then
        mv "$sha_file" "$dir/$new_name.sha256"
      fi
    fi
  fi
done

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
