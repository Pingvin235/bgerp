CREATE TABLE IF NOT EXISTS mobile_account (
  object_type varchar(100) NOT NULL,
  object_id int(11) NOT NULL,
  mkey varchar(200) NOT NULL,
  UNIQUE KEY mkey_object (object_type,object_id)
);

-- ! after must be only a new line !;
