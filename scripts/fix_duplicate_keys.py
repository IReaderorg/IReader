#!/usr/bin/env python3
"""
Fix duplicate keys in strings.xml
"""

import sys
import xml.etree.ElementTree as ET

if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

def fix_duplicates():
    path = 'i18n/src/commonMain/composeResources/values/strings.xml'
    tree = ET.parse(path)
    root = tree.getroot()
    
    seen = set()
    to_remove = []
    
    for elem in root.findall('string'):
        name = elem.get('name')
        if name in seen:
            to_remove.append(elem)
            print(f'Removing duplicate: {name}')
        else:
            seen.add(name)
    
    for elem in to_remove:
        root.remove(elem)
    
    tree.write(path, encoding='utf-8', xml_declaration=False)
    print(f'Removed {len(to_remove)} duplicates')

if __name__ == '__main__':
    fix_duplicates()
