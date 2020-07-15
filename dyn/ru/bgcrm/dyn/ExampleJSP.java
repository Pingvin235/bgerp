package ru.bgcrm.dyn;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.SQLUtils;

/**
 * Демонстрационный динамический класс, 
 * используется в JSP файле примера отчёта.
 * Файл: webapps/WEB-INF/jspf/user/plugin/report/report/example.jsp
 */
public class ExampleJSP {
    private static final Logger log = Logger.getLogger(ExampleJSP.class);
    
    /**
     * Тестовый метод динамического класса, используется в примере отчёта.
     * @param date
     * @return
     */
    public java.util.List<String[]> getPets(Date date) {
        List<String[]> result = new ArrayList<String[]>();

        Connection con = Setup.getSetup().getDBConnectionFromPool();
        try {
            // здесь может быть запрос в БД, но в данном примере просто 
            // добавление записей в результирующий List
            result.add(new String[]{"Васька " + date, "кот", "5"});
            result.add(new String[]{"Бобик " + date, "пёс", "6"});
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            SQLUtils.closeConnection(con);
        }

        return result;		
    }
}
