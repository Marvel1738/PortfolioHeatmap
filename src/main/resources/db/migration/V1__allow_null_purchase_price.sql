-- Allow NULL values for purchase_price column
ALTER TABLE portfolio_holdings
MODIFY COLUMN purchase_price DOUBLE DEFAULT NULL; 