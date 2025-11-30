#!/usr/bin/env python3
import sys
import xml.etree.ElementTree as ET

if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

tree = ET.parse('i18n/src/commonMain/composeResources/values/strings.xml')
root = tree.getroot()
common = ['error', 'retry', 'back', 'close', 'cancel', 'ok', 'refresh', 'done', 'submit', 'save', 'delete', 'edit', 'add', 'remove', 'search', 'settings', 'enable', 'disable', 'switch', 'dismiss']
for s in root.findall('string'):
    name = s.get('name')
    if name in common:
        print(f'{name}: "{s.text}"')
