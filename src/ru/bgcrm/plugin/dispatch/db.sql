CREATE TABLE IF NOT EXISTS dispatch (
	id INT NOT NULL AUTO_INCREMENT,
	title VARCHAR(150) NOT NULL,
	comment VARCHAR(250) NOT NULL,
	account_count INT NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS dispatch_message (
	id INT NOT NULL auto_increment PRIMARY KEY,
	dispatch_ids VARCHAR(200) NOT NULL,
	title VARCHAR(200) NOT NULL,
	text TEXT NOT NULL,
	ready TINYINT(1) NOT NULL,
	create_dt DATETIME NOT NULL,
	sent_dt DATETIME,
	KEY create_dt(create_dt)
);

CREATE TABLE IF NOT EXISTS dispatch_message_dispatch (
	message_id INT NOT NULL,
	dispatch_id INT NOT NULL,
	UNIQUE KEY message_dispatch(message_id, dispatch_id),
	CONSTRAINT fk_dispatch_message_dispatch_message FOREIGN KEY (message_id) REFERENCES dispatch_message(id)
		ON UPDATE RESTRICT
		ON DELETE CASCADE,
	CONSTRAINT fk_dispatch_message_dispatch_dispatch FOREIGN KEY (dispatch_id) REFERENCES dispatch(id)
		ON UPDATE RESTRICT
		ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS dispatch_account_subscription (
	account VARCHAR(100) NOT NULL,
	dispatch_id INT NOT NULL,
	KEY account (account),
	CONSTRAINT fk_dispatch_account_subscription_dispatch FOREIGN KEY (dispatch_id) REFERENCES dispatch(id)
		ON UPDATE RESTRICT
		ON DELETE CASCADE
);

-- ! after must be only a new line !;
