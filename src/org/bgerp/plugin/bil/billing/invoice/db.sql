CREATE TABLE IF NOT EXISTS `invoice` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`type_id` INT NOT NULL,
	`date_from` DATE NOT NULL,
	`date_to` DATE,
	`process_id` INT NOT NULL,
	`number_cnt` INT NOT NULL,
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
	KEY date_from(`date_from`),
	UNIQUE KEY `number`(`number`)
);

CALL alter_table_if_not_column_exists('invoice', 'date_from', 'CHANGE from_date date_from DATE NOT NULL');
CALL add_column_if_not_exists('invoice', 'date_to', 'DATE AFTER date_from');
CALL add_column_if_not_exists('invoice', 'number_cnt', 'INT NOT NULL AFTER process_id');
CALL drop_key_if_exists('invoice', 'from_date');
CALL add_key_if_not_exists('invoice', 'date_from', '(date_from)');
CALL add_unique_key_if_not_exists('invoice', 'number', '(number)');
