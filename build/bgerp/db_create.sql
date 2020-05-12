CREATE DATABASE IF NOT EXISTS bgerp DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

-- MySQL 5.7
-- GRANT ALL PRIVILEGES ON bgerp.* TO 'bgerp'@'localhost' IDENTIFIED BY 'erp';
-- GRANT ALL PRIVILEGES ON bgerp.* TO 'bgerp'@'%' IDENTIFIED BY 'erp';

-- MySQL 8
-- SET GLOBAL validate_password.length = 5;
-- SET GLOBAL validate_password.policy = LOW;

CREATE USER IF NOT EXISTS 'bgerp'@'%';
ALTER USER 'bgerp'@'%' IDENTIFIED BY GENERATED_PASSWORD;
GRANT ALTER ROUTINE ON bgerp.* TO 'bgerp'@'%';
GRANT ALL PRIVILEGES ON bgerp.* TO 'bgerp'@'%';
