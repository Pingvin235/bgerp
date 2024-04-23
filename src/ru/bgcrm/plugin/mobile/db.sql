CREATE TABLE IF NOT EXISTS mobile_account (
	object_type VARCHAR(100) NOT NULL,
	object_id INT NOT NULL,
	mkey VARCHAR(200) NOT NULL,
	UNIQUE KEY mkey_object (object_type,object_id)
);

-- ! after must be only a new line !;
