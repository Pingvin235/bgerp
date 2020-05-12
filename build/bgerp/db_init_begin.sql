USE bgerp;

CREATE TABLE IF NOT EXISTS _check_db_access(a INT, `check_sql-mode` VARCHAR(10) NOT NULL);
INSERT INTO _check_db_access(a) VALUES (42);
DROP TABLE _check_db_access;
