import os
import re

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Pattern 1: if (dateStr != null ...) { ... loanDate = LocalDate.parse(dateStr); }
    # This matches the common pattern I saw in several files
    pattern1 = re.compile(r'if\s*\((\w+)\s*!=\s*null\s*&&\s*!(\w+)\.isEmpty\(\)\)\s*\{(\s*if\s*\(\w+\.length\(\)\s*>\s*10\)\s*\w+\s*=\s*\w+\.substring\(0,\s*10\);)?\s*(\w+)\s*=\s*LocalDate\.parse\((\w+)\);\s*\}', re.MULTILINE)
    
    # Replacement for Pattern 1:
    # \4 = com.kelsz.esla.util.DateUtils.parseLocalDateSafely(\1);
    content = pattern1.sub(r'\4 = com.kelsz.esla.util.DateUtils.parseLocalDateSafely(\1);', content)

    # Pattern 2: simple LocalDate.parse
    content = content.replace('LocalDate.parse(dateStr)', 'com.kelsz.esla.util.DateUtils.parseLocalDateSafely(dateStr)')
    content = content.replace('LocalDate.parse(sddStr)', 'com.kelsz.esla.util.DateUtils.parseLocalDateSafely(sddStr)')
    content = content.replace('LocalDate.parse(fdDateStr)', 'com.kelsz.esla.util.DateUtils.parseLocalDateSafely(fdDateStr)')

    with open(filepath, 'w') as f:
        f.write(content)

files_to_fix = [
    'src/main/java/services/LoanService.java',
    'src/main/java/services/PaymentService.java',
    'src/main/java/services/MemberServiceChargeRefundFormService.java',
    'src/main/java/features/dashboard.java'
]

for f in files_to_fix:
    if os.path.exists(f):
        print(f"Fixing {f}")
        fix_file(f)
