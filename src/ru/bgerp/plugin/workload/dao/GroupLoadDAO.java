package ru.bgerp.plugin.workload.dao;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_GROUP;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.Tables;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.user.User;
import ru.bgcrm.model.user.UserGroup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgerp.plugin.workload.model.GroupLoadConfig;

public class GroupLoadDAO extends ProcessDAO {

    public GroupLoadDAO(Connection con, User user) {
        super(con, user);
    }

    /**
     * Возвращает список процессов с указанными типами процессов {@link ProcessType} и группами пользователей
     * {@link UserGroup}.
     * 
     * @param processTypeIds типы процессов
     * @param groupIds       группы пользователей
     * @return
     */
    public List<Object[]> getProcessList(GroupLoadConfig config, Date date, Collection<Integer> processTypeIds, Collection<Integer> groupIds, String sort)
            throws Exception {

        if (config == null) {
            throw new IllegalArgumentException("config");
        }

        if (date == null) {
            throw new IllegalArgumentException("date");
        }

        Parameter dateFromParam = ParameterCache.getParameter(config.getDateFromParamId());
        Parameter dateToParam = config.getDateToParamId() <= 0 ? null : ParameterCache.getParameter(config.getDateToParamId());

        List<Object[]> result = new ArrayList<>();

        StringBuilder query = new StringBuilder(2000);
        query.append(
                "SELECT DISTINCT SQL_CALC_FOUND_ROWS process.*, dateFromParam.value");

        if (dateToParam != null) {
            query.append(", dateToParam.value");
        }

        query.append(" FROM " + TABLE_PROCESS + "as process");

        if (groupIds != null && groupIds.size() > 0) {
            query.append(" LEFT JOIN ").append(TABLE_PROCESS_GROUP)
                    .append(" as process_group ON process_group.process_id=process.id");
        }

        final String dateParamTable = Parameter.TYPE_DATETIME.equals(dateFromParam.getType()) ? Tables.TABLE_PARAM_DATETIME : Tables.TABLE_PARAM_DATE;

        query.append(" LEFT JOIN " + dateParamTable + " as dateFromParam ON dateFromParam.id=process.id AND dateFromParam.param_id=?");

        if (dateToParam != null) {
            query.append(" LEFT JOIN " + dateParamTable + " as dateToParam ON dateToParam.id=process.id AND dateToParam.param_id=?");
        }

        query.append(" WHERE 1=1");

        if (processTypeIds != null && processTypeIds.size() > 0) {
            query.append(" AND process.type_id IN (");
            query.append(Utils.toString(processTypeIds));
            query.append(") AND process.id>0");
        }

        if (dateToParam != null) {
            query.append(" AND DATE(dateFromParam.value)<=?");
            query.append(" AND DATE(dateToParam.value)>=?");
        } else {
            query.append(" AND DATE(dateFromParam.value)=?");
        }

        if (groupIds != null && groupIds.size() > 0) {
            query.append(" AND process_group.group_id IN (");
            query.append(Utils.toString(groupIds));
            query.append(")");
        }

        PreparedStatement ps = con.prepareStatement(query.toString());

        if (dateToParam != null) {
            ps.setInt(1, config.getDateFromParamId());
            ps.setInt(2, config.getDateToParamId());

            ps.setDate(3, TimeUtils.convertDateToSqlDate(date));
            ps.setDate(4, TimeUtils.convertDateToSqlDate(date));
        } else {
            ps.setInt(1, config.getDateFromParamId());

            ps.setDate(2, TimeUtils.convertDateToSqlDate(date));
        }

        final ParamValueDAO paramValueDao = new ParamValueDAO(con);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            final Process process = getProcessFromRs(rs);

            Object[] row = new Object[4];

            row[0] = process;
            row[1] = rs.getTimestamp("dateFromParam.value");

            if (dateToParam != null) {
                row[2] = rs.getTimestamp("dateToParam.value");
            }

            if (config.getAddressParamId() > 0) {
                row[3] = paramValueDao.getParamAddress(process.getId(), config.getAddressParamId(), 1);
            }

            result.add(row);
        }
        rs.close();
        ps.close();
        
        sortProcessList(result, sort);

        return result;
    }

    private void sortProcessList(List<Object[]> result, String sort) {

        switch (Utils.maskNull(sort)) {

        case "time":
            Collections.sort(result, (o1, o2) -> {

                final Process process1 = (Process) o1[0];
                final Process process2 = (Process) o2[0];

                int cmp = compareTime((Date) o1[1], (Date) o2[1]);
                if (cmp != 0) {
                    return cmp;
                }

                cmp = compareGroup(process1, process2);
                if (cmp != 0) {
                    return cmp;
                }

                return 0;
            });

            break;

        case "processType":
            Collections.sort(result, (o1, o2) -> {

                final Process process1 = (Process) o1[0];
                final Process process2 = (Process) o2[0];

                int cmp = compareProcessType(process1, process2);
                if (cmp != 0) {
                    return cmp;
                }

                cmp = compareTime((Date) o1[1], (Date) o2[1]);
                if (cmp != 0) {
                    return cmp;
                }

                return 0;
            });

            break;

        case "userGroup":
        default:
            Collections.sort(result, (o1, o2) -> {

                final Process process1 = (Process) o1[0];
                final Process process2 = (Process) o2[0];

                int cmp = compareGroup(process1, process2);
                if (cmp != 0) {
                    return cmp;
                }

                cmp = compareTime((Date) o1[1], (Date) o2[1]);
                if (cmp != 0) {
                    return cmp;
                }

                return 0;
            });

            break;
        }
    }

    private int compareGroup(final Process process1, final Process process2) {
        if (process1.getGroupIds().equals(process2.getGroupIds())) {
            return 0;
        }

        String title1 = process1.getGroupIds().stream().findAny().map(a -> UserCache.getUserGroup(a)).map(a -> a.getTitle()).orElse("");
        String title2 = process2.getGroupIds().stream().findAny().map(a -> UserCache.getUserGroup(a)).map(a -> a.getTitle()).orElse("");

        int cmp = title1.compareTo(title2);
        if (cmp != 0) {
            return cmp;
        }

        return 0;
    }
    
    private int compareTime(final Date date1, final Date date2) {
        if (date1 == null) {
            if (date2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (date2 == null) {
                return 1;
            } else {
                return date1.compareTo(date2);
            }
        }
    }
    
    private int compareProcessType(final Process process1, final Process process2) {
        if (process1.getTypeId() == process2.getTypeId()) {
            return 0;
        }

        ProcessType processType1 = ProcessTypeCache.getProcessTypeSafe(process1.getTypeId());
        ProcessType processType2 = ProcessTypeCache.getProcessTypeSafe(process2.getTypeId());

        return processType1.getTitle().compareTo(processType2.getTitle());
    }
}
