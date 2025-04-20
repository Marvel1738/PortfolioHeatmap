ALTER TABLE users ADD COLUMN email VARCHAR(255) UNIQUE;
UPDATE users SET email = CONCAT(username, '@placeholder.com') WHERE email IS NULL; 