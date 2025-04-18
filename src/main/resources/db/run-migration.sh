#!/bin/bash
# Fill in with your actual database credentials
DB_USER="your_username"
DB_PASS="your_password"
DB_NAME="portfolio_heatmap"

# Run the SQL script
mysql -u $DB_USER -p$DB_PASS $DB_NAME < migration/V1__allow_null_purchase_price.sql

# Output message
echo "Migration executed successfully." 