package org.bgerp.plugin.kernel;

import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_HOUSE;
import static ru.bgcrm.dao.Tables.TABLE_CUSTOMER;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LOG;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_PREF;
import static ru.bgcrm.dao.message.Tables.TABLE_MESSAGE;
import static ru.bgcrm.dao.message.Tables.TABLE_MESSAGE_TAG;
import static ru.bgcrm.dao.message.Tables.TABLE_PROCESS_MESSAGE_STATE;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;
import static ru.bgcrm.dao.user.Tables.TABLE_USER;

import org.bgerp.dao.param.ParamValueDAO;

import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;

public class Cleaner extends org.bgerp.dao.Cleaner {
    public static final Cleaner INSTANCE = new Cleaner();

    public Cleaner() {
        // message for missing processes
        inconsistencyCleanupQueries.add(SQL_DELETE + "m" + SQL_FROM + TABLE_MESSAGE + "AS m" + SQL_LEFT_JOIN + TABLE_PROCESS
                + "AS p ON m.process_id=p.id" + SQL_WHERE + "m.process_id>0 AND p.id IS NULL");
        // message_tag for missing messages
        inconsistencyCleanupQueries.add(SQL_DELETE + "mt" + SQL_FROM + TABLE_MESSAGE_TAG + "AS mt" + SQL_LEFT_JOIN + TABLE_MESSAGE
                + "AS m ON mt.message_id=m.id" + SQL_WHERE + "m.id IS NULL");
        // process_message_state for missing processes or messages
        inconsistencyCleanupQueries.add(SQL_DELETE + "ms" + SQL_FROM + TABLE_PROCESS_MESSAGE_STATE + "AS ms" + SQL_LEFT_JOIN  + TABLE_PROCESS
                + "AS p ON ms.process_id=p.id" + SQL_WHERE + "p.id IS NULL");

        for (String table : ParamValueDAO.TABLE_NAMES)
            paramValueForMissingPref(table);

        // TODO: TABLE_PARAM_LIST_VALUE, TABLE_PARAM_LISTCOUNT_VALUE for missing parameters

        paramValueForMissingObject(Process.OBJECT_TYPE, TABLE_PROCESS);
        paramValueForMissingObject(Customer.OBJECT_TYPE, TABLE_CUSTOMER);
        paramValueForMissingObject(User.OBJECT_TYPE, TABLE_USER);
        paramValueForMissingObject(AddressHouse.OBJECT_TYPE, TABLE_ADDRESS_HOUSE);
    }

    private void paramValueForMissingPref(String table) {
        inconsistencyCleanupQueries.add(SQL_DELETE + "pv" + SQL_FROM + table + "AS pv"
            + SQL_LEFT_JOIN + TABLE_PARAM_PREF + "AS pp ON pv.param_id=pp.id"
            + SQL_WHERE + "pp.id IS NULL");
    }

    private void paramValueForMissingObject(String objectType, String objectTable) {
       for (String table : ParamValueDAO.TABLE_NAMES)
            paramValueForMissingObject(objectType, table, objectTable);

        inconsistencyCleanupQueries.add(SQL_DELETE + "pl" + SQL_FROM + TABLE_PARAM_LOG + "AS pl"
            + SQL_INNER_JOIN + TABLE_PARAM_PREF + "AS pref ON pl.param_id=pref.id AND pref.object='" + objectType + "'"
            + SQL_LEFT_JOIN + objectTable + "AS o ON pl.object_id=o.id"
            + SQL_WHERE + "o.id IS NULL");
    }

    private void paramValueForMissingObject(String objectType, String paramValueTable, String objectTable) {
        inconsistencyCleanupQueries.add(SQL_DELETE + "pv" + SQL_FROM + paramValueTable + "AS pv"
            + SQL_INNER_JOIN + TABLE_PARAM_PREF + "AS pref ON pv.param_id=pref.id AND pref.object='" + objectType + "'"
            + SQL_LEFT_JOIN + objectTable + "AS o ON pv.id=o.id"
            + SQL_WHERE + "o.id IS NULL");
    }
}
