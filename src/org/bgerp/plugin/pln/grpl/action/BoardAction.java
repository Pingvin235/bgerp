package org.bgerp.plugin.pln.grpl.action;

import java.sql.Connection;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.model.base.IdTitle;
import org.bgerp.plugin.pln.grpl.Config;
import org.bgerp.plugin.pln.grpl.Plugin;
import org.bgerp.plugin.pln.grpl.dao.GrplDAO;
import org.bgerp.plugin.pln.grpl.model.BoardConfig;
import org.bgerp.plugin.pln.grpl.model.Cell;
import org.bgerp.plugin.pln.grpl.model.Row;
import org.bgerp.util.TimeConvert;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/grpl/board", pathId = true)
public class BoardAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER + "/board";

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        Config config = setup.getConfig(Config.class);

        Date dateFrom = form.getParamDate("dateFrom", TimeConvert.toDate(LocalDate.now().minusDays(2)));
        form.setParam("dateFrom", TimeUtils.format(dateFrom, TimeUtils.FORMAT_TYPE_YMD));
        Date dateTo = form.getParamDate("dateTo", TimeConvert.toDate(LocalDate.now().plusDays(5)));
        form.setParam("dateTo", TimeUtils.format(dateTo, TimeUtils.FORMAT_TYPE_YMD));

        form.setResponseData("boards", config.getBoards());

        return html(conSet, form, PATH_JSP + "/board.jsp");
    }

    public ActionForward show(DynActionForm form, Connection con) throws Exception {
        BoardConfig board = setup.getConfig(Config.class).getBoardOrThrow(form.getId());

        Date dateFrom = form.getParamDate("dateFrom");
        Date dateTo = form.getParamDate("dateTo");

        if (dateFrom == null)
            throw new BGIllegalArgumentException("dateFrom");
        if (dateTo == null || dateTo.before(dateFrom))
            throw new BGIllegalArgumentException("dateTo");

        List<Row> rows = new GrplDAO(con).getRows(board, dateFrom, dateTo);
        Set<Integer> columnIds = rows.stream().flatMap(row -> row.getUsedColumnIds().stream()).collect(Collectors.toSet());
        List<IdTitle> columns = board.getColumns().values().stream().filter(column -> columnIds.contains(column.getId())).toList();

        final Date today = new Date();
        for (Row row : rows) {
            if (row.getDate() == null || TimeUtils.dateBefore(row.getDate(), today) || !row.hasCells())
                continue;

            for (IdTitle column : columns) {
                Cell cell = row.getCell(column.getId());
                if (cell == null)
                    continue;
                cell.addSlotPlacements();
            }
        }

        form.setResponseData("board", board);
        form.setResponseData("columns", columns);
        form.setResponseData("rows", rows);

        updatePersonalization(form, con, map -> map.put("grplBoardLastSelected", String.valueOf(form.getId())));

        return html(con, form, PATH_JSP + "/show.jsp");
    }

    public ActionForward menu(DynActionForm form, Connection con) throws Exception {
        BoardConfig board = setup.getConfig(Config.class).getBoardOrThrow(form.getId());
        var column = board.getColumnOrThrow(form.getParamInt("columnId"));

        var row = new GrplDAO(con).getRow(board, getParamDate(form));
        var cell = row.getCell(column.getId());
        if (cell == null)
            cell = new Cell(row, column.getId(), null);

        form.setResponseData("cell", cell);

        return html(con, form, PATH_JSP + "/menu.jsp");
    }

    public ActionForward cellGroup(DynActionForm form, Connection con) throws Exception {
        BoardConfig board = setup.getConfig(Config.class).getBoardOrThrow(form.getId());
        var column = board.getColumnOrThrow(form.getParamInt("columnId"));

        new GrplDAO(con).updateGroup(board.getId(), getParamDate(form), column.getId(), form.getParamInt("groupId"));

        return json(con, form);
    }

    public ActionForward dialog(DynActionForm form, ConnectionSet conSet) throws Exception {
        BoardConfig board = setup.getConfig(Config.class).getBoardOrThrow(form.getId());
        var column = board.getColumnOrThrow(form.getParamInt("columnId"));
        // TODO: Make configurable in board, if needed
        Duration step = Duration.ofMinutes(30);

        var process = new ProcessDAO(conSet.getSlaveConnection()).getProcessOrThrow(form.getParamInt("processId"));
        var processDuration = board.getProcessDuration(conSet, process);

        LocalTime time = LocalTime.parse(form.getParam("time", Utils::notBlankString));
        LocalTime timeTo = time.plus(Duration.ofMinutes(form.getParamInt("duration", Utils::isPositive)));

        List<LocalTime> times = new ArrayList<>();

        while (time.isBefore(timeTo)) {
            if (time.plus(processDuration).isAfter(timeTo))
                break;
            times.add(time);
            time = time.plus(step);
        }

        form.setResponseData("column", column);
        form.setResponseData("times", times);

        return html(conSet, form, PATH_JSP + "/dialog.jsp");
    }

    public ActionForward slotProcess(DynActionForm form, ConnectionSet conSet) throws Exception {
        BoardConfig board = setup.getConfig(Config.class).getBoardOrThrow(form.getId());

        var date = TimeUtils.parse(form.getParam("date", Utils::notBlankString), TimeUtils.FORMAT_TYPE_YMD);
        var time = LocalTime.parse(form.getParam("time", Utils::notBlankString));

        new GrplDAO(conSet.getConnection()).updateSlotTime(board.getId(), form.getParamInt("processId", Utils::isPositive), date, time);

        return json(conSet, form);
    }

    private Date getParamDate(DynActionForm form) throws BGIllegalArgumentException {
        Date result = form.getParamDate("date");
        if (result == null || TimeUtils.dateBefore(result, new Date()))
            throw new BGIllegalArgumentException("date");
        return result;
    }
}
