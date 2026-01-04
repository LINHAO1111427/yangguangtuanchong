#!/usr/bin/env python3
"""
Fix Maven module order in yudao-framework/pom.xml
Move biz-ip module before excel module
"""

pom_file = 'yudao-framework/pom.xml'

with open(pom_file, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Find and reorder modules
new_lines = []
in_modules = False
modules_section = []

for i, line in enumerate(lines):
    if '<modules>' in line:
        in_modules = True
        new_lines.append(line)
    elif '</modules>' in line:
        in_modules = False

        # Reorder modules
        biz_modules = []
        other_modules = []

        for module_line in modules_section:
            if 'biz-tenant' in module_line or 'biz-data-permission' in module_line or 'biz-ip' in module_line:
                biz_modules.append(module_line)
            else:
                other_modules.append(module_line)

        # Write other modules first
        new_lines.extend(other_modules)

        # Add empty line if needed
        if other_modules and other_modules[-1].strip() != '':
            new_lines.append('\n')

        # Write biz modules
        new_lines.extend(biz_modules)

        # Add empty line if needed
        if biz_modules and biz_modules[-1].strip() != '':
            new_lines.append('\n')

        # Add closing tag
        new_lines.append(line)
        modules_section = []
    elif in_modules:
        modules_section.append(line)
    else:
        new_lines.append(line)

# Write back
with open(pom_file, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

print("✅ Fixed module order in yudao-framework/pom.xml")
print("   biz-ip module is now before excel module")
