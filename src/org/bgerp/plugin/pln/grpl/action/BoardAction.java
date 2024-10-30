package org.bgerp.plugin.pln.grpl.action;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.Date;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.plugin.pln.grpl.Config;
import org.bgerp.plugin.pln.grpl.Plugin;
import org.bgerp.plugin.pln.grpl.dao.GrplDAO;
import org.bgerp.plugin.pln.grpl.model.BoardConfig;
import org.bgerp.plugin.pln.grpl.model.Cell;
import org.bgerp.util.TimeConvert;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/grpl/board", pathId = true)
public class BoardAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER + "/board";

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        Config config = setup.getConfig(Config.class);

        Date dateFrom = form.getParamDate("dateFrom", TimeConvert.toDate(LocalDate.now().minusDays(2)));
        form.setParam("dateFrom", TimeUtils.format(dateFrom, TimeUtils.FORMAT_TYPE_YMD));
        Date dateTo = form.getParamDate("dateTo", TimeConvert.toDate(LocalDate.now().plusDays(10)));
        form.setParam("dateTo", TimeUtils.format(dateTo, TimeUtils.FORMAT_TYPE_YMD));

        form.setResponseData("boards", config.getBoards());

        return html(conSet, form, PATH_JSP + "/null.jsp");
    }

    public ActionForward show(DynActionForm form, Connection con) throws Exception {
        BoardConfig board = setup.getConfig(Config.class).getBoardOrThrow(form.getId());

        Date dateFrom = form.getParamDate("dateFrom");
        Date dateTo = form.getParamDate("dateTo");

        if (dateFrom == null)
            throw new BGIllegalArgumentException("dateFrom");
        if (dateTo == null || dateTo.before(dateFrom))
            throw new BGIllegalArgumentException("dateTo");

        form.setResponseData("board", board);
        form.setResponseData("rows", new GrplDAO(con).getRows(board, dateFrom, dateTo));

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

        new GrplDAO(con).setGroup(board.getId(), getParamDate(form), column.getId(), form.getParamInt("groupId"));

        return json(con, form);
    }

    private Date getParamDate(DynActionForm form) throws BGIllegalArgumentException {
        Date result = form.getParamDate("date");
        if (result == null || TimeUtils.dateBefore(result, new Date()))
            throw new BGIllegalArgumentException("date");
        return result;
    }
}
