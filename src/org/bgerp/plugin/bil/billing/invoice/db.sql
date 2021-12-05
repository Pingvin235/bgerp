CREATE TABLE IF NOT EXISTS `invoice` (
  `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `type_id` INT NOT NULL,
  `from_date` DATE NOT NULL,
  `process_id` INT NOT NULL,
  `number` CHAR(20) NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `created_dt` DATETIME NOT NULL,
  `created_user_id` INT NOT NULL,
  `sent_dt` DATETIME,
  `sent_user_id` INT,
  `payment_date` DATE,
  `payment_user_id` INT,
  `positions` TEXT NOT NULL,
  KEY process_id(`process_id`),
  KEY from_date(`from_date`)
);
