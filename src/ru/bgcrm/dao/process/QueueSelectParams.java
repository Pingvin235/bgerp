package ru.bgcrm.dao.process;

import ru.bgcrm.model.process.queue.Queue;

public class QueueSelectParams {
    public Queue queue;
    public StringBuilder selectPart;
    public StringBuilder joinPart;
    public StringBuilder wherePart;
    public StringBuilder selectAggregatePart;
}
