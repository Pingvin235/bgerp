CREATE TABLE IF NOT EXISTS `bgbilling_common_contract` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `customer_id` int(11) NOT NULL,
  `area_id` int(11) NOT NULL,
  `number` int(11) NOT NULL,
  `date_from` date NOT NULL,
  `pswd` varchar(32) NOT NULL,
  `date_to` date,
  PRIMARY KEY (`id`),
  UNIQUE KEY `num` (`area_id`,`number`),
  KEY `customer_id` (`customer_id`)
);

CALL add_column_if_not_exists('bgbilling_common_contract', 'pswd', 'VARCHAR(32) NOT NULL');
CALL add_column_if_not_exists('bgbilling_common_contract', 'date_from', 'DATE');
UPDATE bgbilling_common_contract SET date_from=CURDATE() WHERE date_from IS NULL;
CALL add_column_if_not_exists('bgbilling_common_contract', 'date_to', 'DATE');

-- ! after must be only a new line !;
