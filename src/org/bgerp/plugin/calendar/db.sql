CREATE TABLE IF NOT EXISTS `calendar_year` (
  `calendar_id` INT NOT NULL,
  `year` INT NOT NULL,
  UNIQUE KEY calendar_id_year (`calendar_id`, `year`)
);

CREATE TABLE IF NOT EXISTS `calendar_event` (
  `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `calendar_id` INT NOT NULL,
  `year` INT NOT NULL,
  `type_id` INT NOT NULL,
  `from_dt` DATETIME NOT NULL,
  `to_dt` DATETIME NOT NULL,
  `amount` INT NOT NULL
);

-- ! after must be only a new line !;
