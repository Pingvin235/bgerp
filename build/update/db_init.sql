-- if sql-mode is set to empty string in my.cnf
CREATE TABLE IF NOT EXISTS _check_db_access(a INT, `check_sql-mode` VARCHAR(10) NOT NULL);
INSERT INTO _check_db_access(a) VALUES (42);
DROP TABLE _check_db_access;

-- fulltext is supported
CREATE TABLE IF NOT EXISTS _check_fulltext (
	data TEXT NOT NULL,
	FULLTEXT (data) /*!50700 WITH PARSER ngram */
);
DROP TABLE _check_fulltext;

-- #BLOCK#NO_HASH#;
DROP PROCEDURE IF EXISTS drop_column_if_exists;
delimiter $$
CREATE PROCEDURE drop_column_if_exists(IN tbl CHAR(64), IN col CHAR(64))
BEGIN
	SET @s = CONCAT("SET @cnt:=(SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='", tbl, "' AND column_name='", col, "')");
	PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
	IF (@cnt > 0) THEN
		SET @s = CONCAT("SET @yyyymmdd:=(SELECT DATE_FORMAT(NOW(), '%Y%m%d'))");
		PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

		SET @s = CONCAT("ALTER TABLE ", tbl, " RENAME COLUMN ", col, " TO _", col, "_", @yyyymmdd);
		PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
	END IF;
END$$
delimiter ;
-- CALL drop_column_if_exists('table_name', 'column_name');

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
-- CALL add_column_if_not_exists('table_name', 'column_name', ' VARCHAR(100) NOT NULL');

DROP PROCEDURE IF EXISTS rename_column_if_exists;
delimiter $$
CREATE PROCEDURE rename_column_if_exists(IN tbl CHAR(64), IN col_name_old CHAR(64), IN col_name_new CHAR(64))
BEGIN
	SET @s = CONCAT("SET @cnt:=(SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='", tbl, "' AND column_name='", col_name_old, "')");
	PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
	IF (@cnt > 0) THEN
		SET @s = CONCAT("ALTER TABLE ", tbl, " RENAME COLUMN ", col_name_old, " TO ", col_name_new);
		PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
	END IF;
END$$
delimiter ;
-- CALL rename_column_if_exists('table_name', 'column_name_old', 'column_name_new');

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
-- CALL drop_key_if_exists('table_name', 'key_name');

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
-- CALL add_key_if_not_exists('table_name', 'key_name', '(col1, col2)');

DROP PROCEDURE IF EXISTS add_unique_key_if_not_exists;
delimiter $$
CREATE PROCEDURE add_unique_key_if_not_exists(IN tbl CHAR(64), IN name CHAR(64), IN def CHAR(64))
BEGIN
-- duplicate key, https://www.mysqltutorial.org/mysql-error-handling-in-stored-procedures/
	DECLARE EXIT HANDLER FOR 1062
	BEGIN
		SELECT CONCAT("Error, duplicate key occurred. Table: ", tbl, ", key name: ", name);

		SET @tmp_tbl = CONCAT("_", tbl, "_", DATE_FORMAT(NOW(), "%Y%m%d_%H%i%s"));

		SELECT CONCAT("Creating: ", @tmp_tbl);

		SET @s = CONCAT("CREATE TABLE ", @tmp_tbl, " SELECT * FROM ", tbl);
		PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

		SET @s = CONCAT("TRUNCATE TABLE ", tbl);
		PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

		SET @s = CONCAT("ALTER TABLE ", tbl, " ADD UNIQUE KEY ", name, " ", def);
		PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

		SET @s = CONCAT("INSERT IGNORE INTO ", tbl, " SELECT * FROM ", @tmp_tbl);
		PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
	END;

	CALL add_key_if_not_exists_base(tbl, name, "", CONCAT("ADD UNIQUE KEY ", name, " ", def));
END$$
delimiter ;
-- CALL add_unique_key_if_not_exists('table_name', 'key_name', '(col1, col2)');

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
-- CALL alter_table_if_not_column_exists('table_name', 'column_name', 'CHANGE old_column_name column_name INT NOT NULL');

DROP PROCEDURE IF EXISTS rename_table;
DROP PROCEDURE IF EXISTS rename_table_if_exists;
delimiter $$
CREATE PROCEDURE rename_table_if_exists(IN name_old CHAR(64), IN name_new CHAR(64))
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
-- CALL rename_table_if_exists('name_old', 'name_new');

DROP PROCEDURE IF EXISTS drop_table_if_exists;
delimiter $$
CREATE PROCEDURE drop_table_if_exists(IN table_name CHAR(64))
BEGIN
	SET @s = CONCAT("SET @cnt:=(SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='", table_name, "')");
	PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
	IF (@cnt = 1) THEN
		SET @s = CONCAT("SET @yyyymmdd:=(SELECT DATE_FORMAT(NOW(), '%Y%m%d'))");
		PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

		SET @s = CONCAT("RENAME TABLE ", table_name, " TO _", table_name, "_", @yyyymmdd);
		PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
	END IF;
END$$
delimiter ;
-- CALL drop_table_if_exists('table_name');

-- #ENDB#;

CREATE TABLE IF NOT EXISTS config_global (
	id INT NOT NULL AUTO_INCREMENT,
	parent_id INT NOT NULL,
	active TINYINT NOT NULL,
	title VARCHAR(255) NOT NULL,
	`data` LONGTEXT,
	dt DATETIME NOT NULL,
	user_id INT NOT NULL,
	last_modify_user_id INT NOT NULL,
	last_modify_dt DATETIME NOT NULL,
	PRIMARY KEY (id)
);

CALL rename_table_if_exists('n_config_global', 'config_global');
CALL add_column_if_not_exists('config_global', 'last_modify_dt', 'DATETIME NOT NULL');
CALL add_column_if_not_exists('config_global', 'last_modify_user_id', 'INT NOT NULL');
CALL add_column_if_not_exists('config_global', 'parent_id', 'INT NOT NULL AFTER id');
CALL drop_column_if_exists('config_global', 'dt');
CALL drop_column_if_exists('config_global', 'user_id');