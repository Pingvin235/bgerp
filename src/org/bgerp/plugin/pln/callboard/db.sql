CREATE TABLE IF NOT EXISTS callboard_shift (
	id INT NOT NULL AUTO_INCREMENT,
	category INT NOT NULL,
	title VARCHAR(100) NOT NULL,
	comment TEXT NOT NULL,
	color VARCHAR(10) NOT NULL,
	use_own_color BIT NOT NULL,
	config TEXT NOT NULL,
	symbol VARCHAR(45) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS callboard_shift_user (
	id INT NOT NULL AUTO_INCREMENT,
	graph INT NOT NULL,
	user INT NOT NULL,
	`group` INT NOT NULL,
	team INT NOT NULL,
	`date` DATE NOT NULL,
	shift INT NOT NULL,
	work_type INT NOT NULL,
	time_from INT NOT NULL,
	time_to INT NOT NULL,
	is_dynamic BIT NOT NULL,
	comment VARCHAR(100) NOT NULL,
	PRIMARY KEY (id),
	KEY `date` (`date`)
);

CREATE TABLE IF NOT EXISTS callboard_work_type (
	id INT NOT NULL AUTO_INCREMENT,
	category INT NOT NULL,
	title VARCHAR(250) NOT NULL,
	comment VARCHAR(250) NOT NULL,
	config TEXT NOT NULL,
	non_work_hours BIT NOT NULL,
	shortcut INT NOT NULL,
	`type` INT NOT NULL DEFAULT '1',
	rule_config TEXT NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS callboard_workdays_calendar (
	id INT NOT NULL,
	`date` DATE NOT NULL,
	`type` TINYINT NOT NULL,
	PRIMARY KEY (id,`date`)
);

CREATE TABLE IF NOT EXISTS callboard_task (
	graph INT NOT NULL,
	`group` INT NOT NULL,
	team INT NOT NULL,
	user_id INT NOT NULL,
	`time` DATETIME NOT NULL,
	slot_from INT NOT NULL,
	duration INT NOT NULL,
	process_id INT NOT NULL,
	reference VARCHAR(200) NOT NULL,
	KEY graph (graph),
	KEY `time` (`time`),
	KEY process_id (process_id)
);

CREATE TABLE IF NOT EXISTS callboard_shift_order (
	graph_id INT NOT NULL,
	group_id INT NOT NULL,
	user_id INT NOT NULL,
	`order` INT NOT NULL,
	PRIMARY KEY (`order`,user_id,group_id,graph_id)
);

CALL add_column_if_not_exists('callboard_shift', 'category', 'INT NOT NULL AFTER id');
CALL add_column_if_not_exists('callboard_work_type', 'category', 'INT NOT NULL AFTER id');
CALL add_column_if_not_exists('callboard_shift', 'color', 'VARCHAR(10) NOT NULL AFTER comment');
CALL add_column_if_not_exists('callboard_shift', 'use_own_color', 'BIT NOT NULL AFTER color');
CALL add_column_if_not_exists('callboard_shift_user', 'shift', 'INT NOT NULL AFTER `date`');

CALL add_column_if_not_exists('callboard_work_type', 'non_work_hours', 'BIT NOT NULL AFTER config');
CALL add_column_if_not_exists('callboard_work_type', 'shortcut', 'INT NOT NULL AFTER non_work_hours');

CALL add_column_if_not_exists('callboard_work_type', 'type', 'INT NOT NULL DEFAULT 1 AFTER shortcut');

CALL drop_column_if_exists('callboard_shift_order', 'owner_id');
ALTER TABLE callboard_shift_order DROP PRIMARY KEY,
ADD PRIMARY KEY (`order`, user_id, group_id, graph_id);

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

CALL add_column_if_not_exists('callboard_shift_user', 'is_dynamic', 'BIT NOT NULL');

CALL add_column_if_not_exists('callboard_shift_user', 'comment', 'VARCHAR(100) NOT NULL');

-- ! after must be only a new line !;
