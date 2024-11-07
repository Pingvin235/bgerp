/* package org.bgerp.plugin.pln.grpl.action;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.plugin.pln.grpl.Config;
import org.bgerp.plugin.pln.grpl.Plugin;
import org.bgerp.plugin.pln.grpl.dao.GrplDAO;
import org.bgerp.plugin.pln.grpl.model.BoardConfig;
import org.bgerp.plugin.pln.grpl.model.Cell;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/grpl/process", pathId = true)
public class ProcessAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER + "/process";

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        BoardConfig board = setup.getConfig(Config.class).getBoardOrThrow(form.getParamInt("boardId"));
        var process = new ProcessDAO(conSet.getSlaveConnection()).getProcessOrThrow(form.getId());

        GrplDAO dao = new GrplDAO(conSet.getConnection());

        var pslot = dao.getSlot(board, process);
        if (pslot == null) {
            var column = board.getColumnOrThrow(conSet, process);
            final var shiftDuration = board.getShift().getDuration();
            final var processDuration = board.getProcessDuration(conSet, process);

            Map<Date, Pair<Group, Duration>> days = new TreeMap<>();

            var rows = dao.getRows(board, TimeUtils.getNextDay(new Date()), column.getId());
            for (var row : rows) {
                Cell cell = row.getCell(column.getId());
                if (cell.getGroup() != null) {
                    var duration = shiftDuration;

                    for (var slot : cell.getSlots())
                        duration = duration.minus(slot.getDuration());

                    if (duration.compareTo(processDuration) > 0)
                        days.put(row.getDate(), new Pair<>(cell.getGroup(), duration));
                }
            }

            form.setResponseData("duration", processDuration);
            form.setResponseData("days", days);
        } else {
            if (pslot.getTime() == null) {
                var usedSlots = dao.getSlots(board, pslot.getCell().getRow().getDate(), pslot.getCell().getColumnId(), form.getId());
                var processDuration = board.getProcessDuration(conSet, process);

                form.setResponseData("times", board.getTimes(usedSlots, processDuration));
            }

            form.setResponseData("slot", pslot);
        }

        return html(conSet, form, PATH_JSP + "/null.jsp");
    }

    public ActionForward slotSet(DynActionForm form, ConnectionSet conSet) throws Exception {
        BoardConfig board = setup.getConfig(Config.class).getBoardOrThrow(form.getParamInt("boardId"));
        var process = new ProcessDAO(conSet.getSlaveConnection()).getProcessOrThrow(form.getId());
        var duration = board.getProcessDuration(conSet, process);

        var column = board.getColumnOrThrow(conSet, process);

        GrplDAO dao = new GrplDAO(conSet.getConnection());
        dao.setSlot(board, form.getParamDate("date"), column.getId(), process, duration);

        return json(conSet, form);
    }

    public ActionForward slotSetTime(DynActionForm form, ConnectionSet conSet) throws Exception {
        new GrplDAO(conSet.getConnection()).updateSlotTime(form.getParamInt("boardId", Utils::isPositive), form.getId(),
                LocalTime.parse(form.getParam("time", Utils::notBlankString)));

        return json(conSet, form);
    }

    public ActionForward slotFree(DynActionForm form, ConnectionSet conSet) throws Exception {
        new GrplDAO(conSet.getConnection()).deleteSlot(form.getParamInt("boardId", Utils::isPositive), form.getId());

        return json(conSet, form);
    }

    public ActionForward slotFreeTime(DynActionForm form, ConnectionSet conSet) throws Exception {
        new GrplDAO(conSet.getConnection()).updateSlotTime(form.getParamInt("boardId", Utils::isPositive), form.getId(), null);

        return json(conSet, form);
    }
}
 */