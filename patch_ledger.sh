#!/bin/bash
FILE="src/main/java/features/manageLedger.java"

# 1. Update PaymentTableModel isCellEditable
sed -i 's/return column == 1 || column == 3 || column == 8;/return false;/g' $FILE

# 2. Update LoanTableModel isCellEditable
sed -i 's/return column == 1 || column == 2 || column == 3 || column == 7 || column == 9;/return false;/g' $FILE

# 3. Add column identifiers for Payment
sed -i 's/"Premium", "Actual Payroll", "Remarks"/"Premium", "Actual Payroll", "Remarks", "", ""/g' $FILE

# 4. Add column identifiers for Loan
sed -i 's/"Interest", "Total", "No. of Months", "Cutoff Amount", "Remarks"/"Interest", "Total", "No. of Months", "Cutoff Amount", "Remarks", "", ""/g' $FILE
