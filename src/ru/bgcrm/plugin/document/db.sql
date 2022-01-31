CREATE TABLE IF NOT EXISTS `document` (
	`id` int(11) NOT NULL AUTO_INCREMENT,
	`title` varchar(100) NOT NULL,
	`object_type` varchar(30) NOT NULL,
	`object_id` int(11) NOT NULL,
	`file_data_id` int(11) NOT NULL,
	PRIMARY KEY (`id`),
	KEY `object_id` (`object_id`)
);

ALTER TABLE document MODIFY object_type VARCHAR(30);

-- ! after must be only a new line !;
