CREATE TABLE IF NOT EXISTS grpl_board_group (
	board_id INT NOT NULL,
	date DATE,
	column_id INT NOT NULL,
	group_id INT NOT NULL,
	UNIQUE KEY board_date_column(board_id, date, column_id)
);

CREATE TABLE IF NOT EXISTS grpl_board_process (
	board_id INT NOT NULL,
	date DATE,
	column_id INT NOT NULL,
	process_id INT NOT NULL,
	duration INT NOT NULL,
	time TIME,
	KEY board_id_date(board_id, date),
	UNIQUE KEY process_board(process_id, board_id)
);
