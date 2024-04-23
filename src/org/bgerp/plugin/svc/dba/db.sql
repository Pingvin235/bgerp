CREATE TABLE IF NOT EXISTS dba_query_history (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	last_dt DATETIME DEFAULT CURRENT_TIMESTAMP,
	user_id INT NOT NULL,
	`data` TEXT NOT NULL
);

-- ! after must be only a new line !;
