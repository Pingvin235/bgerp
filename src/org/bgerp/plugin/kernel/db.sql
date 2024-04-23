-- #BLOCK#;
CREATE TABLE IF NOT EXISTS _check_db_access(a INT, `check_sql-mode` VARCHAR(10) NOT NULL);
INSERT INTO _check_db_access(a) VALUES (42);
DROP TABLE _check_db_access;
-- #ENDB#;

-- the table is created in update/db_init.sql
CALL drop_column_if_exists('config_global', 'dt');
CALL drop_column_if_exists('config_global', 'user_id');

CREATE TABLE IF NOT EXISTS address_country (
	id INT NOT NULL AUTO_INCREMENT,
	title VARCHAR(150) NOT NULL,
	last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (id),
	UNIQUE KEY title (title)
);
ALTER TABLE address_country MODIFY title VARCHAR(150) NOT NULL;
CALL add_unique_key_if_not_exists('address_country', 'title', '(title)');

CREATE TABLE IF NOT EXISTS address_city (
	id INT NOT NULL AUTO_INCREMENT,
	country_id INT NOT NULL DEFAULT '-1',
	title VARCHAR(150) NOT NULL,
	last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (id),
	UNIQUE KEY country_title (country_id,title)
);
ALTER TABLE address_city MODIFY title VARCHAR(150) NOT NULL;
CALL add_unique_key_if_not_exists('address_city', 'country_title', '(country_id, title)');

CREATE TABLE IF NOT EXISTS address_area (
	id INT NOT NULL AUTO_INCREMENT,
	city_id INT NOT NULL DEFAULT '-1',
	title VARCHAR(150) NOT NULL,
	last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (id),
	UNIQUE KEY city_title (city_id,title)
);
CALL add_key_if_not_exists('address_area', 'city_id', '(city_id)');
CALL add_unique_key_if_not_exists('address_area', 'city_title', '(city_id, title)');
ALTER TABLE address_area DROP KEY city_id;
CALL drop_key_if_exists('address_area', 'street_id');

CREATE TABLE IF NOT EXISTS address_quarter (
	id INT NOT NULL AUTO_INCREMENT,
	city_id INT NOT NULL DEFAULT '-1',
	title VARCHAR(150) NOT NULL,
	last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (id)
);
ALTER TABLE address_quarter MODIFY title VARCHAR(150) NOT NULL;
CALL add_key_if_not_exists('address_quarter', 'city_id', '(city_id)');
CALL add_unique_key_if_not_exists('address_quarter', 'city_title', '(city_id, title)');
ALTER TABLE address_quarter DROP KEY city_id;

CREATE TABLE IF NOT EXISTS address_street (
	id INT NOT NULL AUTO_INCREMENT,
	city_id INT NOT NULL DEFAULT '-1',
	title VARCHAR(150) NOT NULL,
	last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (id),
	UNIQUE KEY city_title (city_id,title)
);
ALTER TABLE address_street MODIFY title VARCHAR(150) NOT NULL;
CALL add_key_if_not_exists('address_street', 'city_id', '(city_id)');
CALL add_unique_key_if_not_exists('address_street', 'city_title', '(city_id, title)');
ALTER TABLE address_street DROP KEY city_id;

CREATE TABLE IF NOT EXISTS address_house (
	id INT NOT NULL AUTO_INCREMENT,
	area_id INT NOT NULL,
	quarter_id INT NOT NULL,
	street_id INT NOT NULL,
	house INT NOT NULL,
	frac VARCHAR(50) NOT NULL,
	post_index VARCHAR(10) NOT NULL,
	comment TEXT NOT NULL,
	last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (id),
	UNIQUE KEY street_number (street_id,house,frac)
);
ALTER TABLE address_house MODIFY frac VARCHAR(30) NOT NULL;
ALTER TABLE address_house MODIFY comment TEXT NOT NULL;
ALTER TABLE address_house MODIFY area_id INT NOT NULL;
ALTER TABLE address_house MODIFY quarter_id INT NOT NULL;
ALTER TABLE address_house MODIFY street_id INT NOT NULL;
ALTER TABLE address_house MODIFY house INT NOT NULL;
CALL drop_key_if_exists('address_house', 'house');
CALL add_key_if_not_exists('address_house', 'street_id', '(street_id)');
CALL add_unique_key_if_not_exists('address_house', 'street_number', '(street_id, house, frac)');
CALL add_column_if_not_exists('address_house', 'post_index', 'VARCHAR(10) NOT NULL AFTER frac');
ALTER TABLE address_house MODIFY frac VARCHAR(50) NOT NULL;

CREATE TABLE IF NOT EXISTS customer (
	id INT NOT NULL AUTO_INCREMENT,
	title VARCHAR(255) NOT NULL,
	title_pattern_id INT NOT NULL DEFAULT '-1',
	title_pattern VARCHAR(255) NOT NULL,
	param_group_id INT NOT NULL,
	create_dt DATETIME NOT NULL,
	create_user_id INT NOT NULL,
	pswd VARCHAR(255) NOT NULL,
	PRIMARY KEY (id),
	KEY title (title)
);
ALTER TABLE customer MODIFY title_pattern VARCHAR(255) NOT NULL;
CALL rename_column_if_exists('customer', 'date_created', 'create_dt');
CALL rename_column_if_exists('customer', 'user_id_created', 'create_user_id');

CREATE TABLE IF NOT EXISTS customer_link (
	customer_id INT NOT NULL,
	object_type VARCHAR(30) NOT NULL,
	object_id INT NOT NULL DEFAULT '-1',
	object_title VARCHAR(100) NOT NULL,
	config VARCHAR(100) NOT NULL,
	PRIMARY KEY (customer_id,object_type,object_id),
	KEY object_id (object_id)
);
CALL add_key_if_not_exists('customer_link', 'object_id', '(object_id)');
CALL add_column_if_not_exists('customer_link', 'config', 'VARCHAR(100) NOT NULL');
ALTER TABLE customer_link MODIFY object_title VARCHAR(100);
CALL drop_column_if_exists('customer_link', 'dt_created');

CREATE TABLE IF NOT EXISTS customer_group_title (
	id INT NOT NULL AUTO_INCREMENT,
	title VARCHAR(250) NOT NULL,
	comment VARCHAR(250) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS customer_group (
	customer_id INT NOT NULL,
	group_id INT NOT NULL,
	PRIMARY KEY (customer_id,group_id),
	KEY group_id (group_id)
);

CREATE TABLE IF NOT EXISTS process_status_title (
	id INT NOT NULL AUTO_INCREMENT,
	pos INT NOT NULL,
	title VARCHAR(200) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS process_type (
	id INT NOT NULL AUTO_INCREMENT,
	title VARCHAR(250) NOT NULL,
	parent_id INT NOT NULL,
	use_parent_props TINYINT NOT NULL,
	child_count INT NOT NULL,
	`data` TEXT NOT NULL,
	config TEXT NOT NULL,
	last_modify_user_id INT NOT NULL,
	last_modify_dt DATETIME NOT NULL,
	PRIMARY KEY (id)
);
CALL add_column_if_not_exists('process_type', 'config', 'TEXT NOT NULL');
CALL add_column_if_not_exists('process_type', 'last_modify_dt', 'DATETIME NOT NULL');
CALL add_column_if_not_exists('process_type', 'last_modify_user_id', 'INT NOT NULL');
CALL drop_column_if_exists('process_type', 'archive');

CREATE TABLE IF NOT EXISTS process_type_param (
	type_id INT NOT NULL,
	param_id INT NOT NULL
);

CREATE TABLE IF NOT EXISTS process_type_status (
	type_id INT NOT NULL,
	pos INT NOT NULL,
	status_id INT NOT NULL
);

CREATE TABLE IF NOT EXISTS process (
	id INT NOT NULL AUTO_INCREMENT,
	title VARCHAR(200) NOT NULL,
	type_id INT NOT NULL,
	`priority` TINYINT NOT NULL,
	status_id INT NOT NULL,
	status_dt DATETIME,
	status_user_id INT NOT NULL,
	`description` TEXT NOT NULL,
	create_dt DATETIME NOT NULL,
	create_user_id INT NOT NULL,
	close_dt DATETIME,
	close_user_id INT NOT NULL,
	`groups` CHAR(100) NOT NULL,
	executors CHAR(100) NOT NULL,
	PRIMARY KEY (id),
	KEY `status` (status_id),
	KEY `type` (type_id),
	KEY create_dt (create_dt),
	KEY close_dt (close_dt)
) AUTO_INCREMENT=1970;
CALL add_key_if_not_exists('process', 'status', '(status_id)');
CALL add_key_if_not_exists('process', 'type', '(type_id)');
CALL drop_key_if_exists('process', 'type_status');
CALL add_key_if_not_exists('process', 'create_dt', '(create_dt)');
CALL add_key_if_not_exists('process', 'close_dt', '(close_dt)');
CALL add_column_if_not_exists('process', 'create_user_id', 'INT NOT NULL AFTER create_dt');
CALL add_column_if_not_exists('process', 'close_user_id', 'INT NOT NULL AFTER close_dt');
CALL add_column_if_not_exists('process', 'status_user_id', 'INT NOT NULL AFTER status_dt');
CALL drop_key_if_exists('process', 'last_message_dt');

CREATE TABLE IF NOT EXISTS process_executor (
	process_id INT NOT NULL,
	group_id INT NOT NULL,
	role_id INT NOT NULL,
	user_id INT NOT NULL,
	KEY process_id (process_id),
	KEY user_id (user_id),
	KEY group_id (group_id)
);
CALL add_key_if_not_exists('process_executor', 'process_id', '(process_id)');
CALL add_key_if_not_exists('process_executor', 'user_id', '(user_id)');
CALL add_column_if_not_exists('process_executor', 'group_id', 'INT NOT NULL AFTER process_id');
CALL add_column_if_not_exists('process_executor', 'role_id', 'INT NOT NULL AFTER group_id');
CALL add_key_if_not_exists('process_executor', 'group_id', '(group_id)');

CREATE TABLE IF NOT EXISTS process_group (
	process_id INT NOT NULL,
	group_id INT NOT NULL,
	role_id INT NOT NULL,
	PRIMARY KEY (process_id,group_id,role_id)
);
CALL add_column_if_not_exists('process_group', 'role_id', 'INT NOT NULL');
CALL drop_key_if_exists('process_group', 'PRIMARY');
CALL add_key_if_not_exists('process_group', 'process_group_role', '(process_id, group_id, role_id)');
CALL add_key_if_not_exists('process_group', 'group_id', '(group_id)');

CREATE TABLE IF NOT EXISTS process_status (
	process_id INT NOT NULL,
	dt DATETIME NOT NULL,
	status_id INT NOT NULL,
	user_id INT NOT NULL,
	`last` TINYINT NOT NULL DEFAULT '1',
	comment VARCHAR(4096) NOT NULL,
	KEY process_id (process_id)
);
CALL add_key_if_not_exists('process_status', 'process_id', '(process_id)');
CALL add_column_if_not_exists('process_status', 'last', 'TINYINT NOT NULL DEFAULT 1 AFTER user_id');
CALL drop_column_if_exists('process_status', 'id');
ALTER TABLE process_status MODIFY status_id INT NOT NULL, MODIFY comment VARCHAR(250) NOT NULL;
ALTER TABLE process_status MODIFY comment VARCHAR(4096);

CREATE TABLE IF NOT EXISTS process_link (
	process_id INT NOT NULL,
	object_type VARCHAR(30) NOT NULL,
	object_id INT NOT NULL DEFAULT '-1',
	object_title VARCHAR(100) NOT NULL,
	config VARCHAR(100) NOT NULL,
	PRIMARY KEY (process_id,object_type,object_id),
	KEY object_id (object_id)
);
CALL add_key_if_not_exists('process_link', 'object_id', '(object_id)');
CALL add_column_if_not_exists('process_link', 'config', 'VARCHAR(100) NOT NULL');
ALTER TABLE process_link MODIFY object_title VARCHAR(100);
CALL add_key_if_not_exists('process_link', 'object_title', '(object_title)');
CALL drop_column_if_exists('process_link', 'dt_created');

CREATE TABLE IF NOT EXISTS queue (
	id INT NOT NULL AUTO_INCREMENT,
	title VARCHAR(250) NOT NULL,
	config TEXT NOT NULL,
	last_modify_user_id INT NOT NULL,
	last_modify_dt DATETIME NOT NULL,
	PRIMARY KEY (id)
);
CALL add_column_if_not_exists('queue', 'last_modify_dt', 'DATETIME NOT NULL');
CALL add_column_if_not_exists('queue', 'last_modify_user_id', 'INT NOT NULL');

CREATE TABLE IF NOT EXISTS queue_process_type (
	queue_id INT NOT NULL,
	type_id INT NOT NULL
);

CREATE TABLE IF NOT EXISTS message (
	id INT NOT NULL AUTO_INCREMENT,
	system_id VARCHAR(100) NOT NULL,
	process_id INT NOT NULL,
	type_id INT NOT NULL,
	`direction` TINYINT NOT NULL,
	user_id INT NOT NULL,
	`from` VARCHAR(100) NOT NULL,
	`to` VARCHAR(250) NOT NULL,
	from_dt DATETIME NOT NULL,
	to_dt DATETIME,
	`subject` VARCHAR(250) NOT NULL,
	`text` TEXT NOT NULL,
	attach_data TEXT NOT NULL,
	PRIMARY KEY (id),
	KEY process_id (process_id),
	KEY `type` (type_id),
	KEY `from` (`from`),
	KEY system_id (system_id(5))
);
CALL rename_table('n_message', 'message');
CALL add_key_if_not_exists('message', 'from', '(`from`)');
CALL add_column_if_not_exists('message', 'system_id', 'VARCHAR(100) NOT NULL AFTER id');
CALL add_key_if_not_exists('message', 'system_id', '(system_id(5))');
ALTER TABLE message MODIFY `to` VARCHAR(250) NOT NULL;

CREATE TABLE IF NOT EXISTS message_tag (
	message_id INT NOT NULL,
	tag_id INT NOT NULL,
	UNIQUE KEY message_tag(message_id, tag_id)
);

CREATE TABLE IF NOT EXISTS process_message_state (
	process_id INT NOT NULL,
	in_last_dt DATETIME,
	out_last_dt DATETIME,
	in_count INT NOT NULL,
	in_unread_count INT NOT NULL,
	out_count INT NOT NULL,
	UNIQUE KEY process_id (process_id)
);
CALL add_column_if_not_exists('process_message_state', 'in_unread_count', 'INT NOT NULL AFTER in_count');
CALL add_column_if_not_exists('process_message_state', 'in_last_id', 'INT NOT NULL, ADD out_last_id INT NOT NULL');

CREATE TABLE IF NOT EXISTS object_title_pattern (
	id INT NOT NULL AUTO_INCREMENT,
	`object` VARCHAR(20) NOT NULL,
	title VARCHAR(255) NOT NULL,
	pattern VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS param_group (
	group_id INT NOT NULL,
	param_id INT NOT NULL,
	KEY group_id (group_id)
);

CREATE TABLE IF NOT EXISTS param_group_title (
	id INT NOT NULL AUTO_INCREMENT,
	`object` VARCHAR(20) CHARACTER SET utf8 NOT NULL,
	title CHAR(250) CHARACTER SET utf8 NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS param_pref (
	id INT NOT NULL AUTO_INCREMENT,
	`object` VARCHAR(50) NOT NULL,
	`type` VARCHAR(20) NOT NULL,
	title VARCHAR(255) NOT NULL,
	`order` INT NOT NULL,
	config TEXT NOT NULL,
	comment VARCHAR(1024) NOT NULL,
	PRIMARY KEY (id)
);
ALTER TABLE `param_pref` CHANGE COLUMN `object` `object` VARCHAR(50) NOT NULL;
CALL add_column_if_not_exists('param_pref', 'comment', 'VARCHAR(1024) NOT NULL AFTER config');
CALL drop_column_if_exists('param_pref', 'script');

CREATE TABLE IF NOT EXISTS param_address (
	id INT NOT NULL,
	param_id INT NOT NULL,
	n INT NOT NULL,
	house_id INT NOT NULL,
	flat CHAR(10) NOT NULL,
	room VARCHAR(20) NOT NULL,
	pod TINYINT NOT NULL,
	floor TINYINT NOT NULL,
	`value` VARCHAR(255) NOT NULL,
	comment VARCHAR(255) NOT NULL,
	`custom` TEXT,
	KEY house_id (house_id)
);
ALTER TABLE param_address CHANGE flat flat CHAR(10) NOT NULL;
ALTER TABLE param_address MODIFY room VARCHAR(20) NOT NULL;
ALTER TABLE param_address CHANGE floor floor TINYINT(4) NOT NULL;
ALTER TABLE param_address CHANGE pod pod TINYINT(4) NOT NULL;
CALL drop_key_if_exists('param_address', 'PRIMARY');
CALL add_column_if_not_exists('param_address', 'n', 'INT NOT NULL AFTER param_id');
UPDATE param_address SET n=1 WHERE n=0;
CALL drop_key_if_exists('param_address', 'id_param');
CALL add_unique_key_if_not_exists('param_address', 'id_param_id_value', '(id, param_id, value)');

CREATE TABLE IF NOT EXISTS param_blob (
	id INT NOT NULL,
	param_id INT NOT NULL,
	`value` TEXT NOT NULL,
	PRIMARY KEY (id,param_id),
	KEY param_id (param_id)
);

CREATE TABLE IF NOT EXISTS param_date (
	id INT NOT NULL,
	param_id INT NOT NULL,
	`value` DATE NOT NULL,
	PRIMARY KEY (id,param_id),
	KEY param_id (param_id),
	KEY `value` (`value`)
);

CREATE TABLE IF NOT EXISTS param_datetime (
	id INT NOT NULL,
	param_id INT NOT NULL,
	`value` DATETIME NOT NULL,
	PRIMARY KEY (id,param_id)
);

CREATE TABLE IF NOT EXISTS param_email (
	id INT NOT NULL,
	param_id INT NOT NULL,
	n INT NOT NULL DEFAULT '1',
	`value` VARCHAR(255) NOT NULL,
	comment VARCHAR(1024) NOT NULL,
	PRIMARY KEY (id,param_id,n),
	KEY param_id (param_id),
	KEY `value` (`value`)
);
CALL add_column_if_not_exists('param_email', 'comment', 'VARCHAR(1024) NOT NULL AFTER value');
CALL add_column_if_not_exists('param_email', 'n', 'INT(10) NOT NULL AFTER param_id');
ALTER TABLE param_email MODIFY n INT NOT NULL DEFAULT 1 AFTER param_id;
CALL drop_key_if_exists('param_email', 'id_param');
CALL add_key_if_not_exists('param_email', 'PRIMARY', '(id, param_id, n)');

CREATE TABLE IF NOT EXISTS param_file (
	id INT NOT NULL,
	param_id INT NOT NULL,
	n INT NOT NULL DEFAULT '1',
	`value` INT NOT NULL,
	UNIQUE KEY id_param_id_n (id,param_id,n),
	KEY param_id (param_id)
);
CALL add_column_if_not_exists('param_file', 'n', 'INT NOT NULL DEFAULT 1 AFTER param_id');
CALL drop_key_if_exists('param_file', 'PRIMARY');
CALL add_unique_key_if_not_exists('param_file', 'id_param_id_n', '(id, param_id, n)');
CALL drop_column_if_exists('param_file', 'user_id');
CALL drop_column_if_exists('param_file', 'comment');
CALL drop_column_if_exists('param_file', 'version');

CREATE TABLE IF NOT EXISTS param_list (
	id INT NOT NULL,
	param_id INT NOT NULL,
	`value` INT NOT NULL,
	comment VARCHAR(50) NOT NULL,
	KEY `value` (`value`),
	KEY id_param (id,param_id)
);
CALL drop_key_if_exists('param_list', 'PRIMARY');
CALL add_key_if_not_exists('param_list', 'id_param', '(id, param_id)');
ALTER TABLE param_list CHANGE value value INT NOT NULL;
CALL alter_table_if_not_column_exists('param_list', 'comment', 'CHANGE custom comment VARCHAR(150) NOT NULL');
CALL add_column_if_not_exists('param_list', 'comment', 'VARCHAR(150) NOT NULL');
CALL drop_column_if_exists('param_list', '_comment');
CALL add_unique_key_if_not_exists('param_list', 'id_param_value', '(id, param_id, value)');
CALL drop_key_if_exists('param_list', 'id_param');

CREATE TABLE IF NOT EXISTS param_list_value (
	param_id INT NOT NULL,
	id INT NOT NULL,
	title VARCHAR(250) NOT NULL,
	KEY param_id (param_id)
);
CALL alter_table_if_not_column_exists('param_list_value', 'title', 'CHANGE value title VARCHAR(250) NOT NULL');
CALL rename_table('n_param_list_value', 'param_list_value');

CREATE TABLE IF NOT EXISTS param_listcount (
	id INT NOT NULL,
	param_id INT NOT NULL,
	`value` INT NOT NULL,
	count DECIMAL(10,2) NOT NULL,
	KEY id_param (id,param_id),
	KEY value_count (`value`,count)
);
ALTER TABLE param_listcount MODIFY count DECIMAL(10,2) NOT NULL;
CALL drop_column_if_exists('param_listcount', 'comment');

CREATE TABLE IF NOT EXISTS param_listcount_value (
	param_id INT NOT NULL,
	id INT NOT NULL,
	title VARCHAR(255) NOT NULL,
	KEY param_id (param_id)
);

CREATE TABLE IF NOT EXISTS param_money (
	id INT NOT NULL,
	param_id INT NOT NULL,
	`value` DECIMAL(10,2) NOT NULL,
	UNIQUE KEY id_param_id (id,param_id),
	KEY param_id (param_id),
	KEY `value` (`value`)
);

CREATE TABLE IF NOT EXISTS param_phone (
	id INT NOT NULL,
	param_id INT NOT NULL,
	`value` TEXT NOT NULL,
	PRIMARY KEY (id,param_id)
);

CREATE TABLE IF NOT EXISTS param_phone_item (
	id INT NOT NULL,
	param_id INT NOT NULL,
	n TINYINT NOT NULL,
	phone VARCHAR(15) NOT NULL,
	format VARCHAR(2) NOT NULL,
	comment VARCHAR(255) NOT NULL,
	PRIMARY KEY (id,param_id,n) ,
	KEY phone (phone)
);
ALTER TABLE param_phone_item MODIFY phone VARCHAR(15) NOT NULL;
CALL drop_column_if_exists('param_phone_item', 'flags');

CREATE TABLE IF NOT EXISTS param_text (
	id INT NOT NULL,
	param_id INT NOT NULL,
	`value` VARCHAR(255) NOT NULL,
	PRIMARY KEY (id,param_id),
	KEY param_id (param_id),
	KEY `value` (`value`)
);

CREATE TABLE IF NOT EXISTS param_tree (
	id INT NOT NULL,
	param_id INT NOT NULL,
	`value` VARCHAR(50) NOT NULL,
	KEY id (id),
	KEY param_id (param_id),
	KEY `value` (`value`)
);
ALTER TABLE param_tree MODIFY value VARCHAR(50) NOT NULL;

CREATE TABLE IF NOT EXISTS param_tree_value (
	param_id INT NOT NULL,
	id VARCHAR(50) NOT NULL,
	parent_id VARCHAR(50) NOT NULL,
	title VARCHAR(250) NOT NULL,
	KEY param_id (param_id)
);
CALL drop_key_if_exists('param_tree_value', 'PRIMARY');
ALTER TABLE param_tree_value MODIFY id VARCHAR(50) NOT NULL;
ALTER TABLE param_tree_value MODIFY parent_id VARCHAR(50) NOT NULL;

CREATE TABLE IF NOT EXISTS param_log (
	dt DATETIME NOT NULL,
	object_id INT NOT NULL,
	user_id INT NOT NULL,
	param_id INT NOT NULL,
	`text` VARCHAR(255) NOT NULL,
	KEY object_id (object_id)
);
ALTER TABLE param_log MODIFY dt TIMESTAMP(4) NOT NULL;

CREATE TABLE IF NOT EXISTS user (
	id INT NOT NULL AUTO_INCREMENT,
	deleted TINYINT NOT NULL,
	title VARCHAR(255) NOT NULL,
	`login` VARCHAR(32) NOT NULL,
	pswd VARCHAR(32) NOT NULL,
	`description` VARCHAR(255) NOT NULL,
	create_dt DATETIME NOT NULL,
	`status` INT NOT NULL,
	config TEXT NOT NULL,
	personalization TEXT NOT NULL,
	PRIMARY KEY (id)
);
CALL add_column_if_not_exists('user', 'personalization', 'TEXT NOT NULL');
CALL drop_column_if_exists('user', 'email');
CALL drop_column_if_exists('user', 'ids');
CALL rename_column_if_exists('user', 'date_created', 'create_dt');
CALL drop_column_if_exists('user', 'lu');

CREATE TABLE IF NOT EXISTS user_group (
	user_id INT NOT NULL,
	group_id INT NOT NULL,
	date_from DATE,
	date_to DATE,
	KEY user_id (user_id),
	KEY group_id (group_id)
);
CALL add_key_if_not_exists('user_group', 'user_id', '(user_id)');
CALL add_key_if_not_exists('user_group', 'group_id', '(group_id)');
CALL add_column_if_not_exists('user_group', 'date_from', 'DATE');
CALL add_column_if_not_exists('user_group', 'date_to', 'DATE');
ALTER TABLE user_group MODIFY date_from DATE NOT NULL, MODIFY date_to DATE;

CREATE TABLE IF NOT EXISTS user_group_title (
	id INT NOT NULL AUTO_INCREMENT,
	title VARCHAR(250) NOT NULL,
	`description` VARCHAR(250) NOT NULL,
	config TEXT NOT NULL,
	parent_id INT NOT NULL,
	archive TINYINT NOT NULL,
	child_count INT NOT NULL,
	PRIMARY KEY (id)
);
CALL add_column_if_not_exists('user_group_title', 'config', 'TEXT NOT NULL');
CALL add_column_if_not_exists('user_group_title', 'parent_id', 'INT NOT NULL');
CALL add_column_if_not_exists('user_group_title', 'archive', 'TINYINT NOT NULL');
CALL add_column_if_not_exists('user_group_title', 'child_count', 'INT NOT NULL');

CREATE TABLE IF NOT EXISTS user_group_permset (
	group_id INT NOT NULL,
	permset_id INT NOT NULL,
	pos INT NOT NULL,
	UNIQUE KEY group_permset (group_id,permset_id)
);

CREATE TABLE IF NOT EXISTS user_queue (
	user_id INT NOT NULL,
	queue_id INT NOT NULL,
	PRIMARY KEY (user_id,queue_id)
);

CREATE TABLE IF NOT EXISTS user_group_queue (
	group_id INT NOT NULL,
	queue_id INT NOT NULL,
	PRIMARY KEY (group_id,queue_id)
);

CREATE TABLE IF NOT EXISTS user_permission (
	user_id INT NOT NULL,
	`action` VARCHAR(255) NOT NULL,
	config VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_permset_title (
	id INT NOT NULL AUTO_INCREMENT,
	title VARCHAR(255) NOT NULL,
	comment VARCHAR(255) NOT NULL,
	roles VARCHAR(255) NOT NULL,
	config TEXT NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_permset (
	user_id INT NOT NULL,
	permset_id INT NOT NULL,
	pos INT NOT NULL,
	UNIQUE KEY `user_permset` (user_id,permset_id)
);
CALL add_unique_key_if_not_exists('user_permset', 'user_permset', '(user_id, permset_id)');
CALL add_column_if_not_exists('user_permset', 'pos', 'INT NOT NULL');

CREATE TABLE IF NOT EXISTS user_permset_permission (
	permset_id INT NOT NULL,
	`action` VARCHAR(255) NOT NULL,
	config VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS file_data (
	id INT NOT NULL AUTO_INCREMENT,
	title VARCHAR(100) NOT NULL,
	dt DATETIME NOT NULL,
	`secret` CHAR(32) NOT NULL,
	PRIMARY KEY (id)
);
CALL add_column_if_not_exists('file_data', 'secret', 'CHAR(32) NOT NULL');
CALL rename_column_if_exists('file_data', 'time', 'dt');

CREATE TABLE IF NOT EXISTS process_log (
	id INT NOT NULL,
	dt DATETIME NOT NULL,
	user_id INT NOT NULL,
	`data` TEXT NOT NULL,
	KEY id (id)
);

CREATE TABLE IF NOT EXISTS customer_log (
	id INT NOT NULL,
	dt DATETIME NOT NULL,
	user_id INT NOT NULL,
	`data` TEXT NOT NULL,
	KEY id (id)
);
CALL rename_table('n_customer_log', 'customer_log');

CREATE TABLE IF NOT EXISTS news (
	id INT NOT NULL AUTO_INCREMENT,
	user_id INT NOT NULL,
	create_dt DATETIME NOT NULL,
	update_dt TIMESTAMP NOT NULL,
	title VARCHAR(255) NOT NULL,
	`description` TEXT,
	is_popup BIT NOT NULL,
	life_time INT NOT NULL DEFAULT '30',
	read_time INT NOT NULL DEFAULT '24',
	`groups` VARCHAR(250) NOT NULL,
	PRIMARY KEY (id)
);
CALL add_column_if_not_exists('news', 'read_time', 'INT NOT NULL DEFAULT 24');
CALL add_column_if_not_exists('news', 'groups', 'VARCHAR(250) NOT NULL');
CALL rename_table('n_news', 'news');

CREATE TABLE IF NOT EXISTS news_user (
	news_id INT NOT NULL,
	user_id INT NOT NULL,
	is_read BIT NOT NULL DEFAULT b'0',
	KEY user_id (user_id)
);

CREATE TABLE IF NOT EXISTS process_common_filter (
	id INT NOT NULL AUTO_INCREMENT,
	queue_id INT NOT NULL,
	title VARCHAR(255) NOT NULL,
	`url` VARCHAR(2048) NOT NULL,
	PRIMARY KEY (id)
);
ALTER TABLE process_common_filter CHANGE COLUMN `url` `url` VARCHAR(2048) NOT NULL;

CREATE TABLE IF NOT EXISTS counter (
	id INT(10) NOT NULL AUTO_INCREMENT,
	title VARCHAR(200) NOT NULL,
	`value` INT(10) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS iface_state (
	object_type VARCHAR(100) NOT NULL,
	object_id INT NOT NULL,
	iface_id VARCHAR(100) NOT NULL,
	state VARCHAR(100) NOT NULL,
	UNIQUE KEY object_iface (object_type,object_id,iface_id)
);

CREATE TABLE IF NOT EXISTS properties (
	param VARCHAR(100) NOT NULL PRIMARY KEY,
	value VARCHAR(100) NOT NULL
);

CALL rename_table('user_group_permission', '_user_group_permission');

CREATE TABLE IF NOT EXISTS demo_entity (
	id INT NOT NULL AUTO_INCREMENT,
	title VARCHAR(200) NOT NULL,
	config TEXT,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS param_treecount (
	id INT NOT NULL,
	param_id INT NOT NULL,
	`value` VARCHAR(50) NOT NULL,
	count DECIMAL(10,2) NOT NULL,
	KEY id_param_id(id, param_id),
	KEY value_count(value, count)
);

CREATE TABLE IF NOT EXISTS param_treecount_value (
	param_id INT NOT NULL,
	id VARCHAR(50) NOT NULL,
	parent_id VARCHAR(50) NOT NULL,
	title VARCHAR(250) NOT NULL,
	KEY param_id(param_id)
);

-- TODO: The columns or tables are not already in use. For  activation of deletion, place uncommented line prior the comment.
-- drop_column_if_exists('message', 'processed');

-- must be the last query;
INSERT IGNORE INTO user (id, title, login, pswd, description) VALUES (1, "Administrator", "admin", "admin", "Administrator");
