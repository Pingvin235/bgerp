CREATE TABLE IF NOT EXISTS fulltext_data ( 
	object_type VARCHAR(100) NOT NULL,
	object_id INT NOT NULL,
	scheduled_dt DATETIME,
	data TEXT NOT NULL,
	FULLTEXT (data) WITH PARSER ngram,
	KEY scheduled_dt (scheduled_dt),
	UNIQUE KEY type_id (object_type, object_id)
);

-- ! после должен быть только перенос строки !;
