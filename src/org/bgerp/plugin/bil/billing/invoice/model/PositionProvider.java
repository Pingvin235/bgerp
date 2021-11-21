package org.bgerp.plugin.bil.billing.invoice.model;

import java.time.Month;
import java.time.YearMonth;
import java.util.List;

public interface PositionProvider {
    public List<Position> getPositions(int processId, YearMonth month);
}
