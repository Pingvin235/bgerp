CREATE TABLE IF NOT EXISTS `callboard_shift` (
	`id` int(11) NOT NULL AUTO_INCREMENT,
	`category` int(11) NOT NULL,
	`title` varchar(100) NOT NULL,
	`comment` text NOT NULL,
	`color` varchar(10) NOT NULL,
	`use_own_color` bit(1) NOT NULL,
	`config` text NOT NULL,
	`symbol` varchar(45) NOT NULL,
	PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `callboard_shift_user` (
	`id` int(11) NOT NULL AUTO_INCREMENT,
	`graph` int(11) NOT NULL,
	`user` int(11) NOT NULL,
	`group` int(11) NOT NULL,
	`team` int(11) NOT NULL,
	`date` date NOT NULL,
	`shift` int(11) NOT NULL,
	`work_type` int(11) NOT NULL,
	`time_from` int(11) NOT NULL,
	`time_to` int(11) NOT NULL,
	`is_dynamic` bit(1) NOT NULL,
	`comment` varchar(100) NOT NULL,
	PRIMARY KEY (`id`),
	KEY `date` (`date`)
);

CREATE TABLE IF NOT EXISTS `callboard_work_type` (
	`id` int(11) NOT NULL AUTO_INCREMENT,
	`category` int(11) NOT NULL,
	`title` varchar(250) NOT NULL,
	`comment` varchar(250) NOT NULL,
	`config` text NOT NULL,
	`non_work_hours` bit(1) NOT NULL,
	`shortcut` int(11) NOT NULL,
	`type` int(11) NOT NULL DEFAULT '1',
	`rule_config` text NOT NULL,
	PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `callboard_workdays_calendar` (
	`id` int(11) NOT NULL,
	`date` date NOT NULL,
	`type` tinyint(4) NOT NULL,
	PRIMARY KEY (`id`,`date`)
);

CREATE TABLE IF NOT EXISTS `callboard_task` (
	`graph` int(11) NOT NULL,
	`group` int(11) NOT NULL,
	`team` int(11) NOT NULL,
	`user_id` int(11) NOT NULL,
	`time` datetime NOT NULL,
	`slot_from` int(11) NOT NULL,
	`duration` int(11) NOT NULL,
	`process_id` int(11) NOT NULL,
	`reference` varchar(200) NOT NULL,
	KEY `graph` (`graph`),
	KEY `time` (`time`),
	KEY `process_id` (`process_id`)
);

CREATE TABLE IF NOT EXISTS `callboard_shift_order` (
	`graph_id` int(11) NOT NULL,
	`group_id` int(11) NOT NULL,
	`user_id` int(11) NOT NULL,
	`order` int(11) NOT NULL,
	PRIMARY KEY (`order`,`user_id`,`group_id`,`graph_id`)
);

CALL add_column_if_not_exists('callboard_shift', 'category', 'INT(11) NOT NULL AFTER `id`');
CALL add_column_if_not_exists('callboard_work_type', 'category', 'INT(11) NOT NULL AFTER `id`');
CALL add_column_if_not_exists('callboard_shift', 'color', 'VARCHAR(10) NOT NULL AFTER `comment`');
CALL add_column_if_not_exists('callboard_shift', 'use_own_color', 'BIT NOT NULL AFTER `color`');
CALL add_column_if_not_exists('callboard_shift_user', 'shift', 'INT(11) NOT NULL AFTER `date`');

CALL add_column_if_not_exists('callboard_work_type', 'non_work_hours', 'BIT NOT NULL AFTER config');
CALL add_column_if_not_exists('callboard_work_type', 'shortcut', 'INT NOT NULL AFTER non_work_hours');

CALL add_column_if_not_exists('callboard_work_type', 'type', 'INT NOT NULL DEFAULT 1 AFTER shortcut');

CALL drop_column_if_exists('callboard_shift_order', 'owner_id');
ALTER TABLE callboard_shift_order DROP PRIMARY KEY,
ADD PRIMARY KEY (`order`, `user_id`, `group_id`, `graph_id`);

CALL add_key_if_not_exists('callboard_shift_user', 'date', '(date)');

ALTER TABLE callboard_shift_user MODIFY `date` DATE NOT NULL;
CALL add_column_if_not_exists('callboard_work_type', 'rule_config', 'TEXT NOT NULL');

CALL drop_key_if_exists('callboard_task', 'PRIMARY');
CALL drop_column_if_exists('callboard_task', 'date');
CALL add_column_if_not_exists('callboard_task', 'user_id', 'INT NOT NULL AFTER graph');
CALL add_column_if_not_exists('callboard_task', 'time', 'DATETIME NOT NULL AFTER user_id');
CALL add_column_if_not_exists('callboard_task', 'slot_from', 'INT NOT NULL AFTER time');
CALL add_column_if_not_exists('callboard_task', 'duration', 'INT NOT NULL AFTER slot_from');
CALL add_column_if_not_exists('callboard_task', 'reference', 'VARCHAR(200) NOT NULL AFTER duration');
CALL add_key_if_not_exists('callboard_task', 'process_id', '(process_id)');
CALL add_key_if_not_exists('callboard_task', 'graph', '(graph)');
CALL add_key_if_not_exists('callboard_task', 'time', '(time)');

CALL add_column_if_not_exists('callboard_shift_user', 'is_dynamic', 'bit(1) NOT NULL');

CALL add_column_if_not_exists('callboard_shift_user', 'comment', 'varchar(100) NOT NULL');

-- ! after must be only a new line !;
