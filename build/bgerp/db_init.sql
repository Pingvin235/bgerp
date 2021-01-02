USE bgerp;

CREATE TABLE IF NOT EXISTS _check_db_access(a INT, `check_sql-mode` VARCHAR(10) NOT NULL);
INSERT INTO _check_db_access(a) VALUES (42);
DROP TABLE _check_db_access;

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
  SET @s = CONCAT("SET @cnt_old:=(SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='", name_old, "')");
  PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  SET @s = CONCAT("SET @cnt_new:=(SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='", name_new, "')");
  PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  IF (@cnt_old = 1 AND @cnt_new = 0) THEN
    SET @s = CONCAT("RENAME TABLE ", name_old, " TO ", name_new);
    PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;
END$$
delimiter ;
-- CALL rename_table ('old_name', 'new_name');

-- #ENDB#;

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

CALL add_column_if_not_exists('n_config_global', 'last_modify_dt', 'DATETIME NOT NULL');
CALL add_column_if_not_exists('n_config_global', 'last_modify_user_id', 'INT NOT NULL');
CALL rename_table('n_config_global', 'config_global');
