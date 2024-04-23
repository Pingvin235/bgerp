CREATE TABLE IF NOT EXISTS document (
	id INT NOT NULL AUTO_INCREMENT,
	title VARCHAR(100) NOT NULL,
	object_type VARCHAR(30) NOT NULL,
	object_id INT NOT NULL,
	file_data_id INT NOT NULL,
	PRIMARY KEY (id),
	KEY object_id (object_id)
);

ALTER TABLE document MODIFY object_type VARCHAR(30);

-- ! after must be only a new line !;
