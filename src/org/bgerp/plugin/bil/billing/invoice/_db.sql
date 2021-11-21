CREATE TABLE IF NOT EXISTS `invoice` (
  `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `type_id` INT NOT NULL,
  `from_date` DATE NOT NULL,
  `process_id` INT NOT NULL,
  `summa` DECIMAL(10,2) NOT NULL,
  `created_dt` DATETIME NOT NULL,
  `created_user_id` INT NOT NULL,
  `sent_dt` DATETIME NOT NULL,
  `sent_user_id` INT NOT NULL,
  `payment_date` DATE NOT NULL,
  `payment_user_id` INT NOT NULL,
  `positions` TEXT NOT NULL,
  KEY process_id(`process_id`)
);
