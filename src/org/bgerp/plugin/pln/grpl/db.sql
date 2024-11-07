CREATE TABLE IF NOT EXISTS grpl_board_group (
	board_id INT NOT NULL,
	column_id INT NOT NULL,
	date DATE,
	group_id INT NOT NULL,
	UNIQUE KEY board_date_column(board_id, column_id, date)
);

CREATE TABLE IF NOT EXISTS grpl_board_process (
	board_id INT NOT NULL,
	process_id INT NOT NULL,
	duration INT NOT NULL,
	column_id INT NOT NULL,
	date DATE,
	time TIME,
	KEY board_id_date(board_id, date),
	UNIQUE KEY process_board(process_id, board_id)
);
