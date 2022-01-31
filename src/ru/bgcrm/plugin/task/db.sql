CREATE TABLE IF NOT EXISTS task (
	id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
	type_id VARCHAR(20) NOT NULL,
	process_id INT NOT NULL,
	scheduled_dt DATETIME,
	executed_dt DATETIME,
	config TEXT NOT NULL,
	log TEXT NOT NULL,
	KEY process_id(process_id),
	KEY scheduled_dt(scheduled_dt),
	KEY executed_dt(executed_dt)
);

CALL add_column_if_not_exists('task', 'config', 'TEXT NOT NULL');

-- ! after must be only a new line !;
