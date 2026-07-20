import re
from datetime import datetime

with open('/var/www/html/projects/esla-v3/database/seeders/FormDataSeeder.php', 'r') as f:
    content = f.read()

# Find the $records = [ ... ]; block
start = content.find('$records = [')
end = content.find('];', start)
block = content[start:end]

lines = block.split('\n')
java_array = "    String[][] records = {\n"
for line in lines:
    if 'date' in line and not line.strip().startswith('//'):
        # extract date, actual_payment, premium
        date_match = re.search(r"'date'\s*=>\s*'([^']+)'", line)
        actual_match = re.search(r"'actual_payment'\s*=>\s*([^,]+)", line)
        premium_match = re.search(r"'premium'\s*=>\s*([^,\]]+)", line)
        
        if date_match and actual_match and premium_match:
            date_str = date_match.group(1)
            date_obj = datetime.strptime(date_str, "%m/%d/%Y")
            iso_date = date_obj.strftime("%Y-%m-%d")
            
            actual = actual_match.group(1).strip()
            if actual == 'null':
                actual = "null"
            else:
                actual = f'"{actual}"'
                
            premium = premium_match.group(1).strip()
            if premium == 'null':
                premium = "null"
            else:
                premium = f'"{premium}"'
                
            java_array += f'        {{"{iso_date}", {actual}, {premium}}},\n'
            
java_array += "    };\n"
print(java_array)
