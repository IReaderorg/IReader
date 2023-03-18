#!/bin/bash

deb="$(find ./ -iname '*.deb')"
dir="$(dirname "$deb")"
echo "Found deb file $deb in $dir"

mkdir "$dir/tmp" || exit 1
echo "Extracting deb file"
ar x "$deb" --output "$dir/tmp"
mkdir "$dir/tmp/control_dir"
echo "Extracting control tar"
tar -xf "$dir/tmp/control.tar.xz" -C "$dir/tmp/control_dir"

echo "Adding java dependency"
# grep: if rerun on the same file don't change it again
depends=", java8-runtime-headless"
grep -qxF "$depends" "$dir/tmp/control_dir/control" ||\
    sed -i "/^Depends:/s/ $/$depends/" "$dir/tmp/control_dir/control"
cat "$dir/tmp/control_dir/control"
echo "Compressing new control tar"
tar -cf "$dir/tmp/control.tar.xz" -C "$dir/tmp/control_dir" -I "xz" .
rm -rf "$dir/tmp/control_dir"
echo "Making new deb file"
ar rcs "$deb" "$dir/tmp/"*
echo "Cleaning up"
rm -rf "$dir/tmp"
