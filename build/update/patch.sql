-- #BLOCK#;
DROP PROCEDURE IF EXISTS drop_column_if_exists;
delimiter $$
CREATE PROCEDURE drop_column_if_exists(IN tbl CHAR(64), IN col CHAR(64))
BEGIN
  SET @s = CONCAT("SET @cnt:=(SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='", tbl, "' AND column_name='", col, "')");
  PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  IF (@cnt > 0) THEN
    SET @s = CONCAT("ALTER TABLE ", tbl, " DROP COLUMN ", col);
    PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;
END$$
delimiter ;
-- CALL drop_column_if_exists('table', 'column');

DROP PROCEDURE IF EXISTS add_column_if_not_exists;
delimiter $$
CREATE PROCEDURE add_column_if_not_exists(IN tbl CHAR(64), IN col CHAR(64), IN def CHAR(64))
BEGIN
  SET @s = CONCAT("SET @cnt:=(SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='", tbl, "' AND column_name='", col, "')");
  PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  IF (@cnt = 0) THEN
    SET @s = CONCAT("ALTER TABLE ", tbl, " ADD COLUMN ", col, " ", def);
    PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;
END$$
delimiter ;
-- CALL add_column_if_not_exists('table', 'column', ' VARCHAR (100) NOT NULL');

DROP PROCEDURE IF EXISTS drop_key_if_exists;
delimiter $$
CREATE PROCEDURE drop_key_if_exists(IN tbl CHAR(64), IN name CHAR(64))
BEGIN
  SET @s = CONCAT("SET @cnt:=(SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='", tbl, "' AND index_name='", name, "')");
  PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  IF (@cnt > 0) THEN
    SET @s = CONCAT("ALTER TABLE ", tbl,
      IF(name = 'PRIMARY', " DROP PRIMARY KEY", CONCAT(" DROP KEY ", name)));
    PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;
END$$
delimiter ;
-- CALL drop_key_if_exists('table', 'key');

DROP PROCEDURE IF EXISTS add_key_if_not_exists_base;
delimiter $$
CREATE PROCEDURE add_key_if_not_exists_base(IN tbl CHAR(64), IN name CHAR(64), IN def CHAR(64), IN command CHAR(64))
BEGIN
  SET @s = CONCAT("SET @cnt:=(SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='", tbl, "' AND index_name='", name, "')");
  PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  IF (@cnt = 0) THEN
    IF (LENGTH(command) > 0) THEN
      SET @s = CONCAT("ALTER TABLE ", tbl, " ", command);
    ELSE
      SET @s = CONCAT("ALTER TABLE ", tbl,  " ",
        IF(name ='PRIMARY', "ADD PRIMARY KEY", CONCAT(" ADD KEY ", name)),
        " ", def);
    END IF;
    PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;
END$$
delimiter ;

DROP PROCEDURE IF EXISTS add_key_if_not_exists;
delimiter $$
CREATE PROCEDURE add_key_if_not_exists(IN tbl CHAR(64), IN name CHAR(64), IN def CHAR(64))
BEGIN
  CALL add_key_if_not_exists_base(tbl, name, def, "");
END$$
delimiter ;
-- CALL add_key_if_not_exists('table', 'key', '(col1,col2)');

DROP PROCEDURE IF EXISTS add_unique_key_if_not_exists;
delimiter $$
CREATE PROCEDURE add_unique_key_if_not_exists(IN tbl CHAR(64), IN name CHAR(64), IN def CHAR(64))
BEGIN
  CALL add_key_if_not_exists_base(tbl, name, "", CONCAT("ADD UNIQUE KEY ", name, " ", def));
END$$
delimiter ;
-- CALL add_unique_key_if_not_exists('table', 'key', '(col1,col2)');

DROP PROCEDURE IF EXISTS alter_table_if_not_column_exists;
delimiter $$
CREATE PROCEDURE alter_table_if_not_column_exists(IN tbl CHAR(64), IN col CHAR(64), IN def CHAR(64))
BEGIN
  SET @s = CONCAT("SET @cnt:=(SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='", tbl, "' AND column_name='", col, "')");
  PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  IF (@cnt = 0) THEN
    SET @s = CONCAT("ALTER TABLE ", tbl, " ", def);
    PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;
END$$
delimiter ;
-- CALL alter_table_if_not_column_exists ('tt', 'm', 'CHANGE b m INT NOT NULL');

DROP PROCEDURE IF EXISTS rename_table;
delimiter $$
CREATE PROCEDURE rename_table(IN name_old CHAR(64), IN name_new CHAR(64))
BEGIN
  SET @s = CONCAT("SET @cnt_old:=(SELECT COUNT(*) FROM information_schema.tables WHERE table_name='", name_old, "')");
  PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  SET @s = CONCAT("SET @cnt_new:=(SELECT COUNT(*) FROM information_schema.tables WHERE table_name='", name_new, "')");
  PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  IF (@cnt_old = 1 AND @cnt_new = 0) THEN
    SET @s = CONCAT("RENAME TABLE ", name_old, " TO ", name_new);
    PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;
END$$
delimiter ;
-- CALL rename_table ('old_name', 'new_name');

-- #ENDB#;

CREATE TABLE IF NOT EXISTS `address_area` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `city_id` int(11) NOT NULL DEFAULT '-1',
  `title` varchar(150) NOT NULL,
  `last_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `city_title` (`city_id`,`title`)
);

CREATE TABLE IF NOT EXISTS `address_city` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `country_id` int(11) NOT NULL DEFAULT '-1',
  `title` varchar(150) NOT NULL,
  `last_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `country_title` (`country_id`,`title`)
);

CREATE TABLE IF NOT EXISTS `address_config` (
  `table_id` varchar(50) NOT NULL,
  `record_id` int(11) NOT NULL,
  `key` varchar(50) NOT NULL,
  `value` text,
  PRIMARY KEY (`table_id`,`record_id`,`key`)
);

CREATE TABLE IF NOT EXISTS `address_country` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(150) NOT NULL,
  `last_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `title` (`title`)
);

CREATE TABLE IF NOT EXISTS `address_house` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `area_id` int(11) NOT NULL,
  `quarter_id` int(11) NOT NULL,
  `street_id` int(11) NOT NULL,
  `house` int(11) NOT NULL,
  `frac` varchar(50) NOT NULL,
  `post_index` varchar(10) NOT NULL,
  `comment` text NOT NULL,
  `last_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `street_number` (`street_id`,`house`,`frac`)
);

CREATE TABLE IF NOT EXISTS `address_quarter` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `city_id` int(11) NOT NULL DEFAULT '-1',
  `title` varchar(150) NOT NULL,
  `last_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `address_street` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `city_id` int(11) NOT NULL DEFAULT '-1',
  `title` varchar(150) NOT NULL,
  `last_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `city_title` (`city_id`,`title`)
);

CREATE TABLE IF NOT EXISTS `analytic_house_capacity` (
  `house_id` int(11) NOT NULL,
  `service_type` varchar(25) NOT NULL,
  `dt` date NOT NULL,
  `value` int(11) NOT NULL,
  PRIMARY KEY (`house_id`,`service_type`,`dt`)
);

CREATE TABLE IF NOT EXISTS `n_config_global` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `active` tinyint(1) NOT NULL,
  `title` varchar(255) NOT NULL,
  `data` longtext,
  `dt` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `last_modify_user_id` int(11) NOT NULL,
  `last_modify_dt` datetime NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `customer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8 NOT NULL,
  `title_pattern_id` int(11) NOT NULL DEFAULT '-1',
  `title_pattern` varchar(255) NOT NULL,
  `param_group_id` int(11) NOT NULL,
  `date_created` datetime NOT NULL,
  `user_id_created` int(11) NOT NULL,
  `pswd` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `title` (`title`)
);

CREATE TABLE IF NOT EXISTS `customer_link` (
  `customer_id` int(11) NOT NULL,
  `object_type` varchar(30) NOT NULL,
  `object_id` int(11) NOT NULL DEFAULT '-1',
  `object_title` varchar(100) NOT NULL,
  `dt_created` datetime NOT NULL,
  `config` varchar(100) NOT NULL,
  PRIMARY KEY (`customer_id`,`object_type`,`object_id`),
  KEY `object_id` (`object_id`)
);

CREATE TABLE IF NOT EXISTS `customer_group_title` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(250) NOT NULL,
  `comment` varchar(250) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `customer_group` (
  `customer_id` int(11) NOT NULL,
  `group_id` int(11) NOT NULL,
  PRIMARY KEY (`customer_id`,`group_id`),
  KEY `group_id` (`group_id`)
);

CREATE TABLE IF NOT EXISTS `process_status_title` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pos` int(11) NOT NULL,
  `title` varchar(200) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `process_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(250) NOT NULL,
  `archive` tinyint(4) NOT NULL,
  `parent_id` int(11) NOT NULL,
  `use_parent_props` tinyint(4) NOT NULL,
  `child_count` int(11) NOT NULL,
  `data` text NOT NULL,
  `config` text NOT NULL,
  `last_modify_user_id` int(11) NOT NULL,
  `last_modify_dt` datetime NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `process_type_param` (
  `type_id` int(11) NOT NULL,
  `param_id` int(11) NOT NULL
);

CREATE TABLE IF NOT EXISTS `process_type_status` (
  `type_id` int(11) NOT NULL,
  `pos` int(10) NOT NULL,
  `status_id` int(11) NOT NULL
);

CREATE TABLE IF NOT EXISTS `process` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type_id` int(11) NOT NULL,
  `priority` tinyint(4) NOT NULL,
  `status_id` int(11) NOT NULL,
  `status_dt` datetime,
  `status_user_id` int(11) NOT NULL,
  `description` text NOT NULL,
  `create_dt` datetime NOT NULL,
  `create_user_id` int(11) NOT NULL,
  `close_dt` datetime,
  `close_user_id` int(11) NOT NULL,
  `groups` char(100) NOT NULL,
  `executors` char(100) NOT NULL,
  `title` char(200) NOT NULL,
  `last_message_dt` datetime,
  PRIMARY KEY (`id`),
  KEY `status` (`status_id`),
  KEY `type` (`type_id`),
  KEY `create_dt` (`create_dt`),
  KEY `close_dt` (`close_dt`)
);

CREATE TABLE IF NOT EXISTS `process_executor` (
  `process_id` int(11) NOT NULL,
  `group_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  KEY `process_id` (`process_id`),
  KEY `user_id` (`user_id`),
  KEY `group_id` (`group_id`)
);

CREATE TABLE IF NOT EXISTS `process_group` (
  `process_id` int(11) NOT NULL,
  `group_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  PRIMARY KEY (`process_id`,`group_id`,`role_id`)
);

CREATE TABLE IF NOT EXISTS `process_status` (
  `process_id` int(11) NOT NULL,
  `dt` datetime NOT NULL,
  `status_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `last` tinyint(4) NOT NULL DEFAULT '1',
  `comment` varchar(4096) NOT NULL,
  KEY `process_id` (`process_id`)
);

CREATE TABLE IF NOT EXISTS `process_link` (
  `process_id` int(11) NOT NULL,
  `object_type` varchar(30) NOT NULL,
  `object_id` int(11) NOT NULL DEFAULT '-1',
  `object_title` varchar(100) NOT NULL,
  `dt_created` datetime NOT NULL,
  `config` varchar(100) NOT NULL,
  PRIMARY KEY (`process_id`,`object_type`,`object_id`),
  KEY `object_id` (`object_id`)
);

CREATE TABLE IF NOT EXISTS `queue` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(250) NOT NULL,
  `config` text NOT NULL,
  `last_modify_user_id` int(11) NOT NULL,
  `last_modify_dt` datetime NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `queue_process_type` (
  `queue_id` int(11) NOT NULL,
  `type_id` int(11) NOT NULL
);

CREATE TABLE IF NOT EXISTS `n_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `system_id` varchar(100) NOT NULL,
  `process_id` int(11) NOT NULL,
  `type_id` int(11) NOT NULL,
  `direction` tinyint(1) NOT NULL,
  `user_id` int(11) NOT NULL,
  `from` varchar(100) NOT NULL,
  `to` varchar(250) NOT NULL,
  `from_dt` datetime NOT NULL,
  `to_dt` datetime,
  `subject` varchar(250) NOT NULL,
  `text` text NOT NULL,
  `processed` tinyint(1) NOT NULL,
  `attach_data` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `process_id` (`process_id`),
  KEY `type` (`type_id`),
  KEY `processed` (`processed`),
  KEY `from` (`from`),
  KEY `system_id` (`system_id`(5))
);

CREATE TABLE IF NOT EXISTS `process_message_state` (
  `process_id` int(11) NOT NULL,
  `in_last_dt` datetime,
  `out_last_dt` datetime,
  `in_count` int(11) NOT NULL,
  `in_unread_count` int(11) NOT NULL,
  `out_count` int(11) NOT NULL,
  UNIQUE KEY `process_id` (`process_id`)
);

CREATE TABLE IF NOT EXISTS `object_title_pattern` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `object` varchar(20) NOT NULL,
  `title` varchar(255) NOT NULL,
  `pattern` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `param_group` (
  `group_id` int(11) NOT NULL,
  `param_id` int(11) NOT NULL,
  KEY `group_id` (`group_id`)
);

CREATE TABLE IF NOT EXISTS `param_group_title` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `object` varchar(20) CHARACTER SET utf8 NOT NULL,
  `title` char(250) CHARACTER SET utf8 NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `param_pref` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `object` varchar(50) NOT NULL,
  `type` varchar(20) NOT NULL,
  `title` varchar(255) NOT NULL,
  `order` int(11) NOT NULL,
  `script` text NOT NULL,
  `config` text NOT NULL,
  `comment` varchar(1024) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `param_address` (
  `id` int(11) NOT NULL,
  `param_id` int(11) NOT NULL,
  `n` int(11) NOT NULL,
  `house_id` int(11) NOT NULL,
  `flat` char(10) NOT NULL,
  `room` varchar(20) NOT NULL,
  `pod` tinyint(4) NOT NULL,
  `floor` tinyint(4) NOT NULL,
  `value` varchar(255) NOT NULL,
  `comment` varchar(255) NOT NULL,
  `custom` text,
  PRIMARY KEY (`id`,`param_id`,`n`),
  KEY `house_id` (`house_id`)
);

CREATE TABLE IF NOT EXISTS `param_email` (
  `id` int(11) NOT NULL,
  `param_id` int(11) NOT NULL,
  `n` int(11) NOT NULL DEFAULT '1',
  `value` varchar(255) NOT NULL,
  `comment` varchar(1024) NOT NULL,
  PRIMARY KEY (`id`,`param_id`,`n`),
  KEY `param_id` (`param_id`),
  KEY `value` (`value`)
);

CREATE TABLE IF NOT EXISTS `param_list` (
  `id` int(11) NOT NULL,
  `param_id` int(11) NOT NULL,
  `value` int(11) NOT NULL,
  `comment` varchar(50) NOT NULL,
  `_comment` varchar(50) NOT NULL,
  KEY `value` (`value`),
  KEY `id_param` (`id`,`param_id`)
);

CREATE TABLE IF NOT EXISTS `n_param_list_value` (
  `param_id` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `title` varchar(250) NOT NULL,
  KEY `param_id` (`param_id`)
);

CREATE TABLE IF NOT EXISTS `param_phone` (
  `id` int(11) NOT NULL,
  `param_id` int(11) NOT NULL,
  `value` text NOT NULL,
  PRIMARY KEY (`id`,`param_id`)
);

CREATE TABLE IF NOT EXISTS `param_phone_item` (
  `id` int(11) NOT NULL,
  `param_id` int(11) NOT NULL,
  `n` tinyint(4) NOT NULL,
  `phone` varchar(11) NOT NULL,
  `format` varchar(2) NOT NULL,
  `comment` varchar(255) NOT NULL,
  `flags` int(11) NOT NULL,
  PRIMARY KEY (`id`,`param_id`,`n`) ,
  KEY `phone` (`phone`)
);

CREATE TABLE IF NOT EXISTS `param_text` (
  `id` int(11) NOT NULL,
  `param_id` int(11) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`id`,`param_id`),
  KEY `param_id` (`param_id`),
  KEY `value` (`value`)
);

CREATE TABLE IF NOT EXISTS `param_listcount` (
  `id` int(11) NOT NULL,
  `param_id` int(11) NOT NULL,
  `value` int(11) NOT NULL,
  `count` double NOT NULL,
  `comment` varchar(50) NOT NULL,
  KEY `id_param` (`id`,`param_id`),
  KEY `value_count` (`value`,`count`)
);

CREATE TABLE IF NOT EXISTS `param_listcount_value` (
  `param_id` int(10) NOT NULL,
  `id` int(10) NOT NULL,
  `title` varchar(255) NOT NULL,
  KEY `param_id` (`param_id`)
);

CREATE TABLE IF NOT EXISTS `param_tree` (
  `id` int(11) NOT NULL,
  `param_id` int(11) NOT NULL,
  `value` int(11) NOT NULL,
  KEY `id` (`id`),
  KEY `param_id` (`param_id`),
  KEY `value` (`value`)
);

CREATE TABLE IF NOT EXISTS `param_tree_value` (
  `param_id` int(10) NOT NULL,
  `id` int(10) NOT NULL,
  `parent_id` int(10) NOT NULL,
  `title` varchar(250) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `param_id` (`param_id`)
);

CREATE TABLE IF NOT EXISTS `param_blob` (
  `id` int(11) NOT NULL,
  `param_id` int(11) NOT NULL,
  `value` text NOT NULL,
  PRIMARY KEY (`id`,`param_id`),
  KEY `param_id` (`param_id`)
);

CREATE TABLE IF NOT EXISTS `param_date` (
  `id` int(11) NOT NULL,
  `param_id` int(11) NOT NULL,
  `value` date NOT NULL,
  PRIMARY KEY (`id`,`param_id`),
  KEY `param_id` (`param_id`),
  KEY `value` (`value`)
);

CREATE TABLE IF NOT EXISTS `param_datetime` (
  `id` int(11) NOT NULL,
  `param_id` int(11) NOT NULL,
  `value` datetime NOT NULL,
  PRIMARY KEY (`id`,`param_id`)
);

CREATE TABLE IF NOT EXISTS `param_file` (
  `id` int(10) NOT NULL,
  `param_id` int(10) NOT NULL,
  `n` int(11) NOT NULL DEFAULT '1',
  `value` int(10) NOT NULL,
  `user_id` int(10) NOT NULL,
  `comment` text,
  `version` int(10) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`,`param_id`,`n`,`version`),
  KEY `param_id` (`param_id`)
);

CREATE TABLE IF NOT EXISTS `user` (
  `lu` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deleted` tinyint(4) NOT NULL,
  `title` varchar(255) NOT NULL,
  `login` varchar(32) NOT NULL,
  `pswd` varchar(32) NOT NULL,
  `description` varchar(255) NOT NULL,
  `date_created` datetime NOT NULL,
  `status` int(11) NOT NULL,
  `ids` varchar(250) NOT NULL,
  `config` text NOT NULL,
  `email` varchar(64) NOT NULL,
  `personalization` text NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `user_group` (
  `user_id` int(11) NOT NULL,
  `group_id` int(11) NOT NULL,
  `date_from` date,
  `date_to` date,
  KEY `user_id` (`user_id`),
  KEY `group_id` (`group_id`)
);

CREATE TABLE IF NOT EXISTS `user_group_title` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(250) NOT NULL,
  `description` varchar(250) NOT NULL,
  `config` text NOT NULL,
  `parent_id` int(11) NOT NULL,
  `archive` tinyint(4) NOT NULL,
  `child_count` int(11) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `user_group_permset` (
  `group_id` int(11) NOT NULL,
  `permset_id` int(11) NOT NULL,
  `pos` int(11) NOT NULL,
  UNIQUE KEY `group_permset` (`group_id`,`permset_id`)
);

CREATE TABLE IF NOT EXISTS `user_queue` (
  `user_id` int(11) NOT NULL,
  `queue_id` int(11) NOT NULL,
  PRIMARY KEY (`user_id`,`queue_id`)
);

CREATE TABLE IF NOT EXISTS `user_group_queue` (
  `group_id` int(11) NOT NULL,
  `queue_id` int(11) NOT NULL,
  PRIMARY KEY (`group_id`,`queue_id`)
);

CREATE TABLE IF NOT EXISTS `user_permission` (
  `user_id` int(10) NOT NULL,
  `action` varchar(255) NOT NULL,
  `config` varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS `user_permset_title` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `comment` varchar(255) NOT NULL,
  `roles` varchar(255) NOT NULL,
  `config` text NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `user_permset` (
  `user_id` int(11) NOT NULL,
  `permset_id` int(11) NOT NULL,
  `pos` int(11) NOT NULL,
  UNIQUE KEY `user_permset` (`user_id`,`permset_id`)
);

CREATE TABLE IF NOT EXISTS `user_permset_permission` (
  `permset_id` int(11) NOT NULL,
  `action` varchar(255) NOT NULL,
  `config` varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS `file_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `time` datetime NOT NULL,
  `secret` char(32) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `dynamic_class` (
  `name` varchar(255) NOT NULL,
  `last_mod` bigint(20) NOT NULL,
  `data` blob NOT NULL,
  PRIMARY KEY (`name`)
);

CREATE TABLE IF NOT EXISTS `dynamic_class_iface` (
  `name` varchar(255) NOT NULL,
  `iface` varchar(255) NOT NULL,
  PRIMARY KEY (`name`,`iface`)
);

CREATE TABLE IF NOT EXISTS `address_distribution` (
  `distr_id` int(10) NOT NULL,
  `user_id` int(10) NOT NULL,
  `hid` int(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS `param_log` (
  `dt` datetime NOT NULL,
  `object_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `param_id` int(11) NOT NULL,
  `text` varchar(255) NOT NULL,
  KEY `object_id` (`object_id`)
);

CREATE TABLE IF NOT EXISTS `process_log` (
  `id` int(11) NOT NULL,
  `dt` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `data` text NOT NULL,
  KEY `id` (`id`)
);

CREATE TABLE IF NOT EXISTS `n_customer_log` (
  `id` int(11) NOT NULL,
  `dt` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `data` text NOT NULL,
  KEY `id` (`id`)
);

CREATE TABLE IF NOT EXISTS `document` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `object_type` varchar(30) NOT NULL,
  `object_id` int(11) NOT NULL,
  `file_data_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `object_id` (`object_id`)
);

CREATE TABLE IF NOT EXISTS `bgbilling_common_contract` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `customer_id` int(11) NOT NULL,
  `area_id` int(11) NOT NULL,
  `number` int(11) NOT NULL,
  `date_from` date NOT NULL,
  `pswd` varchar(32) NOT NULL,
  `date_to` date,
  PRIMARY KEY (`id`),
  UNIQUE KEY `num` (`area_id`,`number`),
  KEY `customer_id` (`customer_id`)
);

CREATE TABLE IF NOT EXISTS `n_news` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `create_dt` datetime NOT NULL,
  `update_dt` timestamp NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text,
  `is_popup` bit(1) NOT NULL,
  `life_time` int(11) NOT NULL DEFAULT '30',
  `read_time` int(11) NOT NULL DEFAULT '24',
  `groups` varchar(250) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `news_user` (
  `news_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `is_read` bit(1) NOT NULL DEFAULT b'0',
  KEY `user_id` (`user_id`)
);

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

CREATE TABLE IF NOT EXISTS `fias_house_interval` (
  `intguid` varchar(50) NOT NULL,
  `aoguid` varchar(50) NOT NULL,
  `start_index` int(11) NOT NULL,
  `end_index` int(11) NOT NULL,
  `status` enum('1','2','3') NOT NULL,
  `postal_code` int(11) NOT NULL,
  `last_update_date` date NOT NULL,
  PRIMARY KEY (`intguid`)
);

CREATE TABLE IF NOT EXISTS `fias_street` (
  `aoguid` varchar(50) NOT NULL,
  `street_title` varchar(100) NOT NULL,
  `street_short_name` varchar(10) NOT NULL,
  `crm_city_id` int(11) NOT NULL,
  `crm_street_id` int(11) NOT NULL,
  `area_code` varchar(3) NOT NULL,
  `region_code` varchar(2) NOT NULL,
  `city_code` varchar(3) NOT NULL,
  `last_update_date` date NOT NULL,
  `postal_code` varchar(50) NOT NULL,
  PRIMARY KEY (`aoguid`)
);

CREATE TABLE IF NOT EXISTS `process_common_filter` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `queue_id` int(10) NOT NULL,
  `title` varchar(255) NOT NULL,
  `url` varchar(2048) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `user_group_permission` (
  `group_id` INT(10) NOT NULL,
  `action` VARCHAR(255) NOT NULL,
  `config` VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS `dispatch` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(150) NOT NULL,
  `comment` varchar(250) NOT NULL,
  `account_count` INT NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS dispatch_message
(
  id INT NOT NULL auto_increment PRIMARY KEY,
  dispatch_ids VARCHAR(200) NOT NULL,
  title VARCHAR(200) NOT NULL,
  text TEXT NOT NULL,
  ready TINYINT(1) NOT NULL,
  create_dt DATETIME NOT NULL,
  sent_dt DATETIME,
  KEY create_dt(create_dt)
);

CREATE TABLE IF NOT EXISTS dispatch_message_dispatch
(
  message_id INT NOT NULL,
  dispatch_id INT NOT NULL,
  UNIQUE KEY message_dispatch(message_id, dispatch_id),
  CONSTRAINT `fk_dispatch_message_dispatch_message` FOREIGN KEY (message_id) REFERENCES dispatch_message(id)
    ON UPDATE RESTRICT
    ON DELETE CASCADE,
  CONSTRAINT `fk_dispatch_message_dispatch_dispatch` FOREIGN KEY (dispatch_id) REFERENCES dispatch(id)
    ON UPDATE RESTRICT
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS dispatch_account_subscription
(
  account VARCHAR(100) NOT NULL,
  dispatch_id INT NOT NULL,
  KEY account (account),
  CONSTRAINT `fk_dispatch_account_subscription_dispatch` FOREIGN KEY (dispatch_id) REFERENCES dispatch(id)
    ON UPDATE RESTRICT
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `counter` (
  `id` INT(10) NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `value` INT(10) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS customer_cache (
  customer_id INT NOT NULL,
  cached INT UNSIGNED ZEROFILL NOT NULL,
  cached_dt DATETIME NOT NULL,
  PRIMARY KEY (`customer_id`)
);

CREATE  TABLE IF NOT EXISTS address_quarter_distribution (
  `distr_id` INT(10) NOT NULL ,
  `quarter_id` INT(10) NOT NULL ,
  `group_id` INT(10) NOT NULL ,
  PRIMARY KEY (`distr_id`, `quarter_id`, `group_id`)
);

ALTER TABLE param_address CHANGE flat flat CHAR(10) NOT NULL;

ALTER TABLE param_address MODIFY room VARCHAR(20) NOT NULL;

CALL add_key_if_not_exists('customer_link', 'object_id', '(object_id)');
CALL add_key_if_not_exists('process_link', 'object_id', '(object_id)');

ALTER TABLE address_house MODIFY frac VARCHAR(30) NOT NULL;
ALTER TABLE address_house MODIFY comment TEXT NOT NULL;
ALTER TABLE address_house MODIFY area_id INT NOT NULL;
ALTER TABLE address_house MODIFY quarter_id INT NOT NULL;
ALTER TABLE address_house MODIFY street_id INT NOT NULL;
ALTER TABLE address_house MODIFY house INT NOT NULL;

ALTER TABLE address_quarter MODIFY title VARCHAR(150) NOT NULL;
ALTER TABLE address_country MODIFY title VARCHAR(150) NOT NULL;
ALTER TABLE address_city MODIFY title VARCHAR(150) NOT NULL;
ALTER TABLE address_street MODIFY title VARCHAR(150) NOT NULL;
CALL drop_key_if_exists('address_house', 'house');
CALL add_key_if_not_exists('address_house', 'street_id', '(street_id)');
CALL add_key_if_not_exists('address_street', 'city_id', '(city_id)');
CALL add_key_if_not_exists('address_quarter', 'city_id', '(city_id)');
CALL add_key_if_not_exists('address_area', 'city_id', '(city_id)');

CALL add_column_if_not_exists('file_data', 'secret', 'CHAR(32) NOT NULL');
CALL alter_table_if_not_column_exists('n_param_list_value', 'title', 'CHANGE value title VARCHAR(250) NOT NULL');

CALL drop_key_if_exists('param_list', 'PRIMARY');
CALL add_key_if_not_exists('param_list', 'id_param', '(id, param_id)');

CALL add_key_if_not_exists('process', 'status', '(status_id)');
CALL add_key_if_not_exists('process', 'type', '(type_id)');
CALL drop_key_if_exists('process', 'type_status');
CALL add_key_if_not_exists('process', 'create_dt', '(create_dt)');
CALL add_key_if_not_exists('process', 'close_dt', '(close_dt)');

CALL add_key_if_not_exists('process_status', 'process_id', '(process_id)');

CALL add_unique_key_if_not_exists('address_country', 'title', '(title)');
CALL add_unique_key_if_not_exists('address_city', 'country_title', '(country_id, title)');

CALL add_unique_key_if_not_exists('address_street', 'city_title', '(city_id, title)');
CALL add_unique_key_if_not_exists('address_quarter', 'city_title', '(city_id, title)');
CALL add_unique_key_if_not_exists('address_area', 'city_title', '(city_id, title)');
CALL add_unique_key_if_not_exists('address_house', 'street_number', '(street_id, house, frac)');

ALTER TABLE address_street DROP KEY city_id;
ALTER TABLE address_quarter DROP KEY city_id;
ALTER TABLE address_area DROP KEY city_id;
CALL drop_key_if_exists('address_area', 'street_id');

ALTER TABLE customer MODIFY title_pattern VARCHAR(255) NOT NULL;

ALTER TABLE param_address CHANGE floor floor TINYINT(4) NOT NULL;
ALTER TABLE param_address CHANGE pod pod TINYINT(4)  NOT NULL;

ALTER TABLE param_list CHANGE value value INT NOT NULL;

ALTER TABLE param_address DROP PRIMARY KEY;
ALTER TABLE param_address ADD KEY id_param(id, param_id);

CALL add_column_if_not_exists('param_address', 'n', 'INT NOT NULL AFTER param_id');

UPDATE param_address SET n=1 WHERE n=0;

CALL add_column_if_not_exists('process_link', 'config', 'VARCHAR(100) NOT NULL');
CALL add_column_if_not_exists('customer_link', 'config', 'VARCHAR(100) NOT NULL');

CALL add_column_if_not_exists('process_type', 'config', 'TEXT NOT NULL');

ALTER TABLE document MODIFY object_type VARCHAR(30);
ALTER TABLE customer_link MODIFY object_title VARCHAR(100);
ALTER TABLE process_link MODIFY object_title VARCHAR(100);

CALL add_column_if_not_exists('bgbilling_common_contract', 'pswd', 'VARCHAR(32) NOT NULL');
CALL add_column_if_not_exists('bgbilling_common_contract', 'date_from', 'DATE');
UPDATE bgbilling_common_contract SET date_from=CURDATE() WHERE date_from IS NULL;
CALL add_column_if_not_exists('bgbilling_common_contract', 'date_to', 'DATE');

CALL add_column_if_not_exists('user_group_title', 'config', 'TEXT NOT NULL');

ALTER TABLE `param_pref` CHANGE COLUMN `object` `object` VARCHAR(50) NOT NULL;

CALL add_key_if_not_exists('process_executor', 'process_id', '(process_id)');
CALL add_key_if_not_exists('process_executor', 'user_id', '(user_id)');

CALL add_column_if_not_exists('param_pref', 'comment', 'VARCHAR(1024) NOT NULL AFTER config');

CALL add_column_if_not_exists('param_email', 'comment', 'VARCHAR(1024) NOT NULL AFTER value');
CALL add_column_if_not_exists('param_email', 'n', 'INT(10) NOT NULL AFTER param_id');

CALL add_unique_key_if_not_exists('user_permset', 'user_permset', '(user_id, permset_id)');
CALL add_unique_key_if_not_exists('user_group', 'user_group', '(user_id, group_id)');

CALL drop_key_if_exists('param_address', 'id_param');
CALL add_key_if_not_exists('param_address', 'PRIMARY', '(id, param_id, n)');

CALL add_column_if_not_exists('param_file', 'n', 'INT NOT NULL DEFAULT 1 AFTER param_id');
CALL drop_key_if_exists('param_file', 'PRIMARY');
CALL add_key_if_not_exists('param_file', 'PRIMARY', '(id, param_id, n)');

ALTER TABLE param_email MODIFY n INT NOT NULL DEFAULT 1 AFTER param_id;
CALL drop_key_if_exists('param_email', 'id_param');
CALL add_key_if_not_exists('param_email', 'PRIMARY', '(id, param_id, n)');

-- TODO: Убрать лишние столбцы (roles) в user_group_title, переименовать description в comment.

CALL drop_key_if_exists('address_distribution', 'PRIMARY');
CALL add_key_if_not_exists('address_distribution', 'PRIMARY', '(`hid`, `distr_id`, `user_id`)');
CALL add_key_if_not_exists('address_distribution', 'user_index', '(user_id)');

CALL add_column_if_not_exists('address_house', 'post_index', 'VARCHAR(10) NOT NULL AFTER frac');
UPDATE address_house SET post_index=(SELECT value FROM address_config WHERE table_id='address_house' AND record_id=address_house.id AND `key`='s.box.index' AND value!='' AND value!='0') WHERE post_index='';

CALL add_column_if_not_exists('n_config_global', 'last_modify_dt', 'DATETIME NOT NULL');
UPDATE n_config_global SET last_modify_dt=NOW() WHERE last_modify_dt='0000-00-00 00:00:00';
CALL add_column_if_not_exists('n_config_global', 'last_modify_user_id', 'INT NOT NULL');

CALL add_column_if_not_exists('user_permset', 'pos', 'INT NOT NULL');

CALL add_column_if_not_exists('process', 'create_user_id', 'INT NOT NULL AFTER create_dt');
CALL add_column_if_not_exists('process', 'close_user_id', 'INT NOT NULL AFTER close_dt');

CALL add_column_if_not_exists('process_type', 'last_modify_dt', 'DATETIME NOT NULL');
UPDATE process_type SET last_modify_dt=NOW() WHERE last_modify_dt='0000-00-00 00:00:00';
CALL add_column_if_not_exists('process_type', 'last_modify_user_id', 'INT NOT NULL');

CALL add_column_if_not_exists('queue', 'last_modify_dt', 'DATETIME NOT NULL');
UPDATE queue SET last_modify_dt=NOW() WHERE last_modify_dt='0000-00-00 00:00:00';
CALL add_column_if_not_exists('queue', 'last_modify_user_id', 'INT NOT NULL');

CALL add_column_if_not_exists('process', 'status_user_id', 'INT NOT NULL AFTER status_dt');

-- RENAME TABLE n_news_user TO news_user;
-- ALTER  TABLE n_news_user ADD KEY user_id(user_id);
-- ALTER TABLE n_news_user DROP COLUMN id, DROP PRIMARY KEY;

ALTER TABLE user_group DROP KEY user_group;
CALL add_key_if_not_exists('user_group', 'user_id', '(user_id)');
CALL add_key_if_not_exists('user_group', 'group_id', '(group_id)');

CALL add_column_if_not_exists('process_group', 'role_id', 'INT NOT NULL');
CALL drop_key_if_exists('process_group', 'PRIMARY');
CALL add_key_if_not_exists('process_group', 'process_group_role', '(process_id, group_id, role_id)');

CALL add_column_if_not_exists('user_group', 'date_from', 'DATE');
CALL add_column_if_not_exists('user_group', 'date_to', 'DATE');

CALL add_column_if_not_exists('user_group_title', 'parent_id', 'INT NOT NULL');
CALL add_column_if_not_exists('user_group_title', 'archive', 'TINYINT NOT NULL');
CALL add_column_if_not_exists('user_group_title', 'child_count', 'INT NOT NULL');

CALL add_column_if_not_exists('user', 'personalization', 'TEXT NOT NULL');

CALL add_column_if_not_exists('n_news', 'read_time', 'INT NOT NULL DEFAULT 24');

CALL add_column_if_not_exists('process_status', 'last', 'TINYINT NOT NULL DEFAULT 1 AFTER user_id');
CALL drop_column_if_exists('process_status', 'id');

CALL add_key_if_not_exists('process_group', 'group_id', '(group_id)');

ALTER TABLE user_group CHANGE COLUMN date_from date_from DATE NULL;

CALL add_column_if_not_exists('process_executor', 'group_id', 'INT NOT NULL AFTER process_id');
CALL add_column_if_not_exists('process_executor', 'role_id', 'INT NOT NULL AFTER group_id');
CALL add_key_if_not_exists('process_executor', 'group_id', '(group_id)');

CALL alter_table_if_not_column_exists('param_list', 'comment', 'CHANGE custom comment VARCHAR(150) NOT NULL');
CALL add_column_if_not_exists('param_list', 'comment', 'VARCHAR(150) NOT NULL');

CALL add_column_if_not_exists('param_listcount', 'comment', 'VARCHAR(50) NOT NULL AFTER `count`');

ALTER TABLE process_status MODIFY status_id INT NOT NULL, MODIFY comment VARCHAR(250) NOT NULL;

CALL add_column_if_not_exists('callboard_shift', 'category', 'INT(11) NOT NULL AFTER `id`');
CALL add_column_if_not_exists('callboard_work_type', 'category', 'INT(11) NOT NULL AFTER `id`');
CALL add_column_if_not_exists('callboard_shift', 'color', 'VARCHAR(10) NOT NULL AFTER `comment`');
CALL add_column_if_not_exists('callboard_shift', 'use_own_color', 'BIT NOT NULL AFTER `color`');
CALL add_column_if_not_exists('callboard_shift_user', 'shift', 'INT(11) NOT NULL AFTER `date`');

ALTER TABLE process_status MODIFY comment VARCHAR(4096);
CALL add_column_if_not_exists('callboard_work_type', 'non_work_hours', 'BIT NOT NULL AFTER config');
CALL add_column_if_not_exists('callboard_work_type', 'shortcut', 'INT NOT NULL AFTER non_work_hours');

CALL add_key_if_not_exists('n_message', 'from', '(`from`)');

ALTER TABLE address_house MODIFY frac VARCHAR(50) NOT NULL;

CALL add_column_if_not_exists('param_file', 'user_id', 'INT(10) NOT NULL AFTER `value`');
CALL add_column_if_not_exists('param_file', 'comment', 'TEXT AFTER `user_id`');
CALL add_column_if_not_exists('param_file', 'version', 'INT(10) NULL DEFAULT 1 AFTER `comment`');

CALL drop_key_if_exists('param_file', 'PRIMARY');
CALL add_key_if_not_exists('param_file', 'PRIMARY', '(`id`, `param_id`, `n`, `version`)');

CALL add_column_if_not_exists('callboard_work_type', 'type', 'INT NOT NULL DEFAULT 1 AFTER shortcut');

CALL drop_column_if_exists('callboard_shift_order', 'owner_id');
ALTER TABLE callboard_shift_order DROP PRIMARY KEY,
ADD PRIMARY KEY (`order`, `user_id`, `group_id`, `graph_id`);

CALL add_column_if_not_exists('n_message', 'system_id', 'VARCHAR(100) NOT NULL AFTER id');
CALL add_key_if_not_exists('n_message', 'system_id', '(system_id(5))');

ALTER TABLE user_group MODIFY date_from DATETIME, MODIFY date_to DATETIME;

CALL add_key_if_not_exists('callboard_shift_user', 'date', '(date)');
-- ALTER TABLE callboard_work_type MODIFY shortcut VARCHAR(100) NOT NULL;

CALL add_column_if_not_exists('process_message_state', 'in_unread_count', 'INT NOT NULL AFTER in_count');

-- INSERT INTO process_message_state(process_id, in_last_dt, in_count) SELECT process_id, MAX(from_dt), COUNT(*) FROM n_message WHERE process_id>0 GROUP BY process_id;

ALTER TABLE callboard_shift_user MODIFY `date` DATE NOT NULL;
CALL add_column_if_not_exists('callboard_work_type', 'rule_config', 'TEXT NOT NULL');

ALTER TABLE n_message MODIFY `to` VARCHAR(250) NOT NULL;

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

ALTER TABLE process_common_filter CHANGE COLUMN `url` `url` VARCHAR(2048) NOT NULL;

CALL add_column_if_not_exists('n_news', 'groups', 'VARCHAR(250) NOT NULL');

CALL add_column_if_not_exists('callboard_shift_user', 'is_dynamic', 'bit(1) NOT NULL');

CALL add_column_if_not_exists('callboard_shift_user', 'comment', 'varchar(100) NOT NULL');

ALTER TABLE param_phone_item MODIFY phone VARCHAR(15) NOT NULL;

ALTER TABLE param_tree MODIFY value VARCHAR(50) NOT NULL;

CALL drop_key_if_exists('param_tree_value', 'PRIMARY');
ALTER TABLE param_tree_value MODIFY id VARCHAR(50) NOT NULL;
ALTER TABLE param_tree_value MODIFY parent_id VARCHAR(50) NOT NULL;

CALL add_column_if_not_exists('process_message_state', 'in_last_id', 'INT NOT NULL, ADD out_last_id INT NOT NULL');

ALTER TABLE param_listcount MODIFY count DECIMAL(10,2) NOT NULL;

CREATE TABLE IF NOT EXISTS mobile_account (
  object_type varchar(100) NOT NULL,
  object_id int(11) NOT NULL,
  mkey varchar(200) NOT NULL,
  UNIQUE KEY mkey_object (object_type,object_id)
);

CREATE TABLE IF NOT EXISTS iface_state (
  object_type varchar(100) NOT NULL,
  object_id int(11) NOT NULL,
  iface_id varchar(100) NOT NULL,
  state varchar(100) NOT NULL,
  UNIQUE KEY object_iface (object_type,object_id,iface_id)
);

-- ALTER TABLE process_link ADD object_id_ext VARCHAR(100) NOT NULL AFTER object_id;
-- ALTER TABLE process_link DROP PRIMARY KEY;
-- ALTER TABLE process_link ADD UNIQUE KEY process_object_u (process_id, object_type, object_id, object_id_ext);

-- ALTER TABLE customer_link ADD object_id_ext VARCHAR(100) NOT NULL AFTER object_id;
-- ALTER TABLE customer_link DROP PRIMARY KEY;
-- ALTER TABLE customer_link ADD UNIQUE KEY customer_object_u (customer_id, object_type, object_id, object_id_ext);

CREATE TABLE IF NOT EXISTS properties (
  param VARCHAR(100) NOT NULL PRIMARY KEY,
  value VARCHAR(100) NOT NULL
);

CALL add_key_if_not_exists('process_link', 'object_title', '(object_title)');

CREATE TABLE IF NOT EXISTS message_tag (
  message_id INT NOT NULL,
  tag_id INT NOT NULL,
  UNIQUE KEY message_tag(message_id, tag_id)
);

CALL rename_table('n_message', 'message');
CALL rename_table('n_param_list_value', 'param_list_value');
CALL rename_table('n_customer_log', 'customer_log');
CALL rename_table('n_news', 'news');
CALL rename_table('n_config_global', 'config_global');

-- при добавлении в дамп не указывать: значения DEFAULT столбцов, если они 0 для чисел и '' для строк, названия движков
-- использовать процедуры для модификации существующих таблиц add_key_if_not_exists и т.п.
-- для плагинов создавать отдельные patch_plugin.sql файлы и переносить всё туда

