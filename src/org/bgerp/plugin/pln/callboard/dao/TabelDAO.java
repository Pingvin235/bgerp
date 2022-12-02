package org.bgerp.plugin.pln.callboard.dao;

import java.io.FileInputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.bgerp.plugin.pln.callboard.cache.CallboardCache;
import org.bgerp.plugin.pln.callboard.model.DayType;
import org.bgerp.plugin.pln.callboard.model.WorkDaysCalendar;
import org.bgerp.plugin.pln.callboard.model.WorkShift;
import org.bgerp.plugin.pln.callboard.model.WorkType;
import org.bgerp.plugin.pln.callboard.model.WorkTypeTime;
import org.bgerp.plugin.pln.callboard.model.config.CalendarConfig;
import org.bgerp.plugin.pln.callboard.model.config.DayTypeConfig;
import org.bgerp.plugin.pln.callboard.model.config.CallboardConfig.Callboard;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class TabelDAO extends CommonDAO {
    private static final String SHORTCUT_FREE_DAY = "В";

    private static final int USER_ROW_FROM = 25, USER_ROWS = 4;
    private static final int USER_COL_FROM = 0, USER_COLS = 27;

    private static final String HOLIDAY_WORK_SHORTCUT = "РВ";
    // фиктивное сокращение в него складываются все виды работ в праздники и РВ,
    // затем удаляется.
    private static final String HOLIDAY_SHORTCUT = "Z";
    private static final int HOLIDAY_POS = 3;

    // правый столбик "Отработано за" по сокращениям позиция в столбе
    private static final Map<String, int[]> SHORTCUT_POS = new HashMap<String, int[]>();
    static {
        SHORTCUT_POS.put("Я", new int[] { 0 });
        SHORTCUT_POS.put("Н", new int[] { 2 });
        // SHORTCUT_POS.put( "РВ", new int[]{ 0, 3 } );
        SHORTCUT_POS.put("С", new int[] { 0, 4 });
    }

    public TabelDAO(Connection con) {
        super(con);
    }

    public HSSFWorkbook generateTabel(Callboard callboard, final Date dateFrom, Date dateTo) throws Exception {
        DayTypeConfig dayTypeConfig = Setup.getSetup().getConfig(DayTypeConfig.class);

        WorkDaysCalendar calendar = Setup.getSetup().getConfig(CalendarConfig.class)
                .getCalendar(callboard.getCalendarId());
        Map<Date, Integer> excludeDates = new WorkTypeDAO(con).getWorkDaysCalendarExcludes(calendar.getId());

        ParamValueDAO paramDao = new ParamValueDAO(con);

        Map<Integer, Map<Date, WorkShift>> userShifts = new ShiftDAO(con).getUserShifts(callboard.getId(), dateFrom,
                dateTo);

        List<User> userList = new UserDAO(con).getUserList(Collections.singleton(callboard.getGroupId()), dateFrom,
                dateTo);

        // "/home/shamil/tmp/tabel_blank.xls"
        HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(callboard.getTabelConfig().getTemplatePath()));
        HSSFSheet sheet = workbook.getSheetAt(0);

        // отдел
        sheet.getRow(9).getCell(1).setCellValue(callboard.getTabelConfig().getOrgName());

        // даты
        addDates(dateFrom, dateTo, sheet);

        // количество дней в месяце
        final int days = TimeUtils.convertDateToCalendar(dateTo).getActualMaximum(Calendar.DAY_OF_MONTH);

        // числа
        addDays(sheet, days, 20, new GetForDay() {
            @Override
            public String add(int day, boolean upCell) {
                return upCell ? String.valueOf(day) : "";
            }

            @Override
            public String getDefault(boolean upCell) {
                return upCell ? "X" : "";
            }
        });

        // final Map<Integer, WorkType> workTypeMap = new WorkTypeDAO( con
        // ).getWorkTypeMap();

        // диапазоны в строках с пользователем
        List<CellRangeAddress> rangesForCopy = getRangesForCopy(sheet);

        // подсчёт количества рабочих часов
        int workHoursSum = 0;

        Calendar date = TimeUtils.convertDateToCalendar(dateFrom);
        Calendar calendarTo = TimeUtils.convertDateToCalendar(dateTo);
        while (TimeUtils.dateBeforeOrEq(date, calendarTo)) {
            Pair<DayType, Boolean> typePair = calendar.getDayType(date.getTime(), excludeDates);
            DayType dayType = dayTypeConfig.getType(typePair.getFirst().getId());
            if (dayType != null) {
                workHoursSum += dayType.getWorkHours();
            }

            date.add(Calendar.DAY_OF_YEAR, 1);
        }

        // тестирование
        for (int i = 0; i < userList.size(); i++) {
            if (!userShifts.containsKey(userList.get(i).getId())
            // || userList.get( i ).getId() != 35 // Князев, отладка
            ) {
                userList.remove(i);
                i--;
            }
        }

        // обработка пользователей
        final int size = userList.size();
        for (int i = 0; i < size; i++) {
            User user = userList.get(i);

            int offset = i * USER_ROWS;

            if (i > 0) {
                sheet.shiftRows(30 + offset - USER_ROWS, 34 + offset - USER_ROWS, USER_ROWS);
                createUserRows(sheet, USER_ROW_FROM + offset, rangesForCopy);
            }

            String post = Utils
                    .maskNull(paramDao.getParamText(user.getId(), callboard.getTabelConfig().getParamDolznost()));
            if (Utils.notBlankString(post)) {
                post = ", " + post;
            }

            // порядковый номер и ФИО
            HSSFRow row = sheet.getRow(USER_ROW_FROM + offset);
            row.getCell(0).setCellValue(i + 1);
            row.getCell(1).setCellValue(user.getTitle() + post);
            row.getCell(2).setCellValue(
                    paramDao.getParamText(user.getId(), callboard.getTabelConfig().getParamTabelNumber()));

            // ключ - строка
            final Map<String, Integer> neyavkMap = new HashMap<String, Integer>(4);

            @SuppressWarnings("unchecked")
            final Map<Date, WorkShift> dateShifts = (Map<Date, WorkShift>) Utils.maskNull(userShifts.get(user.getId()),
                    Collections.emptyMap());

            // суммы для столбца "Отработано за месяц"
            // по позициям в 0 элементе - сумма дней, в 1 - минут
            final int[][] workedForPeriod = new int[5][2];

            // только для возможности увеличивать из класса
            int userWorkMinutes = 0;

            // по дням сокращения
            final LinkedHashMap<Integer, LinkedHashMap<String, Integer>> dayLabels = new LinkedHashMap<Integer, LinkedHashMap<String, Integer>>(
                    31);

            // выборка сокращений
            date = TimeUtils.convertDateToCalendar(dateFrom);
            while (TimeUtils.dateBeforeOrEq(date, calendarTo)) {
                Date curDate = TimeUtils.convertCalendarToDate(date);
                Date prevDate = TimeUtils.getPrevDay(curDate);

                int day = date.get(Calendar.DAY_OF_MONTH);

                // Pair<DayType, Boolean> prevDayType = calendar.getDayType( prevDate,
                // excludeDates );
                Pair<DayType, Boolean> curDayType = calendar.getDayType(curDate, excludeDates);

                // final boolean prevDayHoliday = prevDayType != null &&
                // prevDayType.getFirst().isHoliday();
                final boolean curDayHoliday = curDayType != null && curDayType.getFirst().isHoliday();

                // ключ - код типа работ. значение - суммарное число минут в эти сутки
                LinkedHashMap<String, Integer> labels = new LinkedHashMap<String, Integer>();
                dayLabels.put(day, labels);

                // смены предыдущих суток, может что перешло на эти
                WorkShift shift = dateShifts.get(prevDate);
                if (shift != null) {
                    for (WorkTypeTime time : shift.getWorkTypeTimeList()) {
                        WorkType type = CallboardCache.getWorkType(time.getWorkTypeId());
                        if (type != null) {
                            if (!type.getIsNonWorkHours()) {
                                int minutes = time.getWorkMinutesInDay(type, prevDate, curDate);
                                if (minutes > 0) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("PrevDay " + TimeUtils.format(curDate, TimeUtils.FORMAT_TYPE_YMD)
                                                + "  += " + minutes + "; type: " + type.getId());
                                    }

                                    for (String shortcut : type.getShortcutList()) {
                                        labels.put(shortcut, Utils.maskNull(labels.get(shortcut), 0) + minutes);
                                        // РВ переходит на следующий день по дню начала смены либо остаток явки с
                                        // предыдущего дня
                                        if (HOLIDAY_WORK_SHORTCUT.equals(shortcut)
                                                || (curDayHoliday && !"Н".equals(shortcut))) {
                                            labels.put(HOLIDAY_SHORTCUT,
                                                    Utils.maskNull(labels.get(HOLIDAY_SHORTCUT), 0) + minutes);
                                        }
                                    }

                                    if (!curDayHoliday) {
                                        userWorkMinutes += minutes;
                                    }
                                }
                            }
                        } else {
                            log.warn("Can't find workType with id=" + time.getWorkTypeId());
                        }
                    }
                }

                // смена текущих суток
                shift = dateShifts.get(curDate);
                if (shift != null) {
                    for (WorkTypeTime time : shift.getWorkTypeTimeList()) {
                        WorkType type = CallboardCache.getWorkType(time.getWorkTypeId());
                        if (type != null) {
                            // выявление неявок
                            if (type.getIsNonWorkHours()) {
                                int hours = time.getMinutesInDay(type, curDate, curDate, false) / 60;
                                // весь день не учитывамый тип работ - неявка
                                if (hours >= 23) {
                                    String shortcut = Utils.getFirst(type.getShortcutList());

                                    Integer current = Utils.maskNull(neyavkMap.get(shortcut), 0);
                                    neyavkMap.put(shortcut, ++current);

                                    labels.put(shortcut, 0);

                                    // еще одна поправка: индивидуальная норма высчитывается не только в отпуска и
                                    // больничные, но на все виды работ, которые попадают под условия:
                                    // Если в виде работы отмечено, что данный вид не надо учитывать в рабочих часах
                                    // (Отпуск, Больничный, Прогул, Уход за ребенком и тп) и
                                    // заполнено поле "Обозначение в табеле", https://sup.ufanet.ru/issues/9980
                                    userWorkMinutes += curDayType.getFirst().getWorkHours() * 60;

                                    break;
                                }
                            } else {
                                int minutes = time.getWorkMinutesInDay(type, curDate, curDate);
                                if (minutes > 0) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("CurDay " + TimeUtils.format(curDate, TimeUtils.FORMAT_TYPE_YMD)
                                                + "  += " + minutes + "; type: " + type.getId());
                                    }

                                    for (String shortcut : type.getShortcutList()) {
                                        labels.put(shortcut, Utils.maskNull(labels.get(shortcut), 0) + minutes);
                                        // РВ на данный день, либо праздник и явка
                                        if (HOLIDAY_WORK_SHORTCUT.equals(shortcut)
                                                || (curDayHoliday && !"Н".equals(shortcut))) {
                                            labels.put(HOLIDAY_SHORTCUT,
                                                    Utils.maskNull(labels.get(HOLIDAY_SHORTCUT), 0) + minutes);
                                        }
                                    }

                                    if (!curDayHoliday) {
                                        userWorkMinutes += minutes;
                                    }
                                }
                            }
                        } else {
                            log.warn("Can't find workType with id=" + time.getWorkTypeId());
                        }
                    }
                }

                date.add(Calendar.DAY_OF_YEAR, 1);
            }

            log.debug("userWorkMinutes: {}; workHoursSum: {}", userWorkMinutes, workHoursSum);

            int suMinutesRest = userWorkMinutes - workHoursSum * 60;
            // сверхурочка
            if (suMinutesRest > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("su: " + suMinutesRest / 60 + "; mins: " + suMinutesRest);
                }

                List<LinkedHashMap<String, Integer>> dayLabelsList = new ArrayList<LinkedHashMap<String, Integer>>(
                        dayLabels.values());
                Collections.reverse(dayLabelsList);

                for (LinkedHashMap<String, Integer> labels : dayLabelsList) {
                    final Integer yMinutes = Utils.maskNull(labels.get("Я"), 0);
                    final Integer nMinutes = Utils.maskNull(labels.get("Н"), 0);

                    // нельзя в сверурочку переводить ночные и один час должен остаться в явке
                    Integer allowedУMinutes = yMinutes - nMinutes - 60;
                    allowedУMinutes = Math.max(allowedУMinutes, 0);

                    if (allowedУMinutes > 0) {
                        final int suMinutes = Math.min(allowedУMinutes, suMinutesRest);

                        suMinutesRest -= suMinutes;

                        labels.put("Я", yMinutes - suMinutes);
                        labels.put("С", suMinutes);

                        if (log.isDebugEnabled()) {
                            log.debug("y: " + yMinutes / 60 + " ( " + yMinutes + " ) allowed: " + allowedУMinutes / 60
                                    + " ( " + allowedУMinutes + " ) su: " + suMinutes / 60 + " ( " + suMinutes
                                    + ") suRest: " + suMinutesRest / 60);
                        }
                    }

                    if (suMinutesRest == 0) {
                        break;
                    }
                }
            }

            // одна смена не может дважды попасть как день одного типа (например Я/С)
            // должно дать один день явки в итоге и день сверухрочки, а не два дня явки и
            // день сверхурочки
            Set<Integer> dayInPosition = new HashSet<Integer>(3);

            // суммирование сокращений
            for (Map.Entry<Integer, LinkedHashMap<String, Integer>> meD : dayLabels.entrySet()) {
                dayInPosition.clear();

                LinkedHashMap<String, Integer> labels = meD.getValue();

                for (Map.Entry<String, Integer> me : labels.entrySet()) {
                    String shortcut = me.getKey();
                    int minutes = me.getValue();

                    if (HOLIDAY_SHORTCUT.equals(shortcut)) {
                        workedForPeriod[HOLIDAY_POS][0]++;
                        workedForPeriod[HOLIDAY_POS][1] += minutes;
                    } else {
                        int[] positions = SHORTCUT_POS.get(shortcut);
                        if (positions != null) {
                            for (int position : positions) {
                                if (!dayInPosition.contains(position)) {
                                    workedForPeriod[position][0]++;
                                    dayInPosition.add(position);
                                }
                                workedForPeriod[position][1] += minutes;

                                if (log.isDebugEnabled()) {
                                    log.debug(meD.getKey() + " " + shortcut + " " + workedForPeriod[position][0] + " m "
                                            + workedForPeriod[position][1] + " h " + workedForPeriod[position][1] / 60);
                                }
                            }
                        }
                    }
                }

                labels.remove(HOLIDAY_SHORTCUT);
            }

            addDays(sheet, days, USER_ROW_FROM + offset, new GetForDay() {
                // т.к. первый вызов идёт для upCell, чтобы два раза не считать - сюда
                // сохраняется строка для нижней ячейки
                private String valueForDownCell = "";

                @Override
                public String add(int day, boolean upCell) {
                    LinkedHashMap<String, Integer> labels = dayLabels.get(day);

                    if (upCell) {
                        StringBuilder labelString = new StringBuilder(50);
                        StringBuilder hoursString = new StringBuilder(50);

                        for (Map.Entry<String, Integer> me : labels.entrySet()) {
                            Utils.addSeparated(labelString, "/", me.getKey());
                            // различные Б, ОТ добавляются с 0 в минутах
                            if (me.getValue() > 0) {
                                Utils.addSeparated(hoursString, "/", String.valueOf(me.getValue() / 60));
                            }
                        }

                        valueForDownCell = hoursString.toString();

                        if (labels.size() == 0) {
                            labelString.append(SHORTCUT_FREE_DAY);
                        }

                        return labelString.toString();
                    } else {
                        return valueForDownCell;
                    }
                }

                @Override
                public String getDefault(boolean upCell) {
                    return "X";
                }
            });

            HSSFRow rowNext = sheet.getRow(USER_ROW_FROM + offset + 2);

            // раб., команд., ночн., празд., сверхур. - суммы за месяц
            for (int k = 0; k < 5; k++) {
                if (workedForPeriod[k][0] > 0) {
                    row.getCell(19 + k).setCellValue(workedForPeriod[k][0]);
                    rowNext.getCell(19 + k).setCellValue(workedForPeriod[k][1] / 60);
                }
            }

            int pos = 0;
            for (Map.Entry<String, Integer> me : neyavkMap.entrySet()) {
                row = sheet.getRow(USER_ROW_FROM + offset + pos++);
                row.getCell(24).setCellValue(me.getKey());
                row.getCell(25).setCellValue(me.getValue());
            }
        }

        return workbook;
    }

    private List<CellRangeAddress> getRangesForCopy(HSSFSheet sheet) {
        List<CellRangeAddress> rangesForCopy = new ArrayList<CellRangeAddress>();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            if (USER_ROW_FROM <= range.getFirstRow() && range.getFirstRow() < USER_ROW_FROM + USER_ROWS
                    && USER_COL_FROM <= range.getFirstColumn() && range.getLastColumn() < USER_COL_FROM + USER_COLS) {
                rangesForCopy.add(range);
            }
        }
        return rangesForCopy;
    }

    private void addDates(Date dateFrom, Date dateTo, HSSFSheet sheet) {
        HSSFRow row = sheet.getRow(14);
        row.getCell(15).setCellValue(TimeUtils.format(dateTo, TimeUtils.FORMAT_TYPE_YMD));
        row.getCell(19).setCellValue(TimeUtils.format(dateFrom, TimeUtils.FORMAT_TYPE_YMD));
        row.getCell(21).setCellValue(TimeUtils.format(dateTo, TimeUtils.FORMAT_TYPE_YMD));
    }

    private void addDays(HSSFSheet sheet, final int days, int rowNum, GetForDay adder) {
        // числа
        final int daysInFirstRow = days / 2;

        int d = 1;

        HSSFRow row1 = sheet.getRow(rowNum);
        HSSFRow row2 = sheet.getRow(rowNum + 1);
        for (int c = 1; c <= 16; c++) {
            final int cellnum = 2 + c;

            row1.getCell(cellnum)
                    .setCellValue(c <= daysInFirstRow ? String.valueOf(adder.add(d, true)) : adder.getDefault(true));
            row2.getCell(cellnum)
                    .setCellValue(c <= daysInFirstRow ? String.valueOf(adder.add(d, false)) : adder.getDefault(false));

            if (c <= daysInFirstRow) {
                d++;
            }
        }

        row1 = sheet.getRow(rowNum + 2);
        row2 = sheet.getRow(rowNum + 3);
        for (; d <= 31; d++) {
            final int cellnum = 2 + d - daysInFirstRow;
            row1.getCell(cellnum).setCellValue(d <= days ? String.valueOf(adder.add(d, true)) : adder.getDefault(true));
            row2.getCell(cellnum)
                    .setCellValue(d <= days ? String.valueOf(adder.add(d, false)) : adder.getDefault(false));
        }
    }

    private static interface GetForDay {
        public String add(int day, boolean upCell);

        public String getDefault(boolean upCell);
    }

    public static void createUserRows(HSSFSheet sheet, int toRow, List<CellRangeAddress> rangesForCopy) {
        for (int rowInd = 0; rowInd < USER_ROWS; rowInd++) {
            HSSFRow rowFrom = sheet.getRow(USER_ROW_FROM + rowInd);
            HSSFRow row = sheet.createRow(toRow + rowInd);

            for (int colInd = USER_COL_FROM; colInd < USER_COLS; colInd++) {
                Cell cellFrom = rowFrom.getCell(colInd);

                Cell cell = row.createCell(colInd);
                cell.setCellStyle(cellFrom.getCellStyle());
                cell.setCellValue("");
            }
        }

        int rowDelta = toRow - USER_ROW_FROM;
        for (CellRangeAddress address : rangesForCopy) {
            sheet.addMergedRegion(new CellRangeAddress(address.getFirstRow() + rowDelta,
                    address.getLastRow() + rowDelta, address.getFirstColumn(), address.getLastColumn()));
        }
    }
}
