CREATE TABLE IF NOT EXISTS team_party (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	title VARCHAR(200) NOT NULL,
	secret CHAR(32) NOT NULL,
	created_dt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	KEY secret(secret)
);

CREATE TABLE IF NOT EXISTS team_party_member (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	party_id INT NOT NULL,
	title VARCHAR(200),
	UNIQUE KEY party_id_title(party_id, title)
);

CREATE TABLE IF NOT EXISTS team_party_payment (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	party_id INT NOT NULL,
	member_id INT NOT NULL,
	amount DECIMAL(10, 1) NOT NULL,
	description VARCHAR(250) NOT NULL,
	KEY party_id(party_id)
);
