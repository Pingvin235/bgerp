CREATE TABLE IF NOT EXISTS `secret_open` (
	`id` CHAR(20) NOT NULL,
	`secret` CHAR(32) NOT NULL,
	`dt` DATETIME NOT NULL,
	UNIQUE KEY id (`id`)
);
