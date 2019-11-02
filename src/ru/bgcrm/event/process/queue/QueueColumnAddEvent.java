package ru.bgcrm.event.process.queue;

import ru.bgcrm.event.Event;
import ru.bgcrm.util.ParameterMap;

public class QueueColumnAddEvent implements Event {
	private ParameterMap col;
	private StringBuilder selectPart;
	private StringBuilder joinPart;

	public QueueColumnAddEvent(ParameterMap col, StringBuilder selectPart, StringBuilder joinPart) {

		this.col = col;
		this.selectPart = selectPart;
		this.joinPart = joinPart;
	}

	public ParameterMap getCol() {
		return col;
	}

	public StringBuilder getSelectPart() {
		return selectPart;
	}

	public StringBuilder getJoinPart() {
		return joinPart;
	}
}
