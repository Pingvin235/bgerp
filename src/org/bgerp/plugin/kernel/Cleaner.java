package org.bgerp.plugin.kernel;

import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_HOUSE;
import static ru.bgcrm.dao.Tables.TABLE_CUSTOMER;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_ADDRESS;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_BLOB;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_DATE;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_DATETIME;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_EMAIL;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_FILE;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LIST;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LISTCOUNT;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_MONEY;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_PHONE;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_PHONE_ITEM;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_PREF;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_TREE;
import static ru.bgcrm.dao.message.Tables.TABLE_MESSAGE;
import static ru.bgcrm.dao.message.Tables.TABLE_MESSAGE_TAG;
import static ru.bgcrm.dao.message.Tables.TABLE_PROCESS_MESSAGE_STATE;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;
import static ru.bgcrm.dao.user.Tables.TABLE_USER;

import java.util.Collection;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Utils;

public class Cleaner extends org.bgerp.dao.Cleaner {
    public static final Cleaner INSTANCE = new Cleaner();

    public Cleaner() {
        // message for missing processes
        inconsistencyCleanupQueries.add("DELETE m" + SQL_FROM + TABLE_MESSAGE + "AS m" + SQL_LEFT_JOIN + TABLE_PROCESS
                + "AS p ON m.process_id=p.id" + SQL_WHERE + "m.process_id>0 AND p.id IS NULL");
        // message_tag for missing messages
        inconsistencyCleanupQueries.add("DELETE mt" + SQL_FROM + TABLE_MESSAGE_TAG + "AS mt" + SQL_LEFT_JOIN + TABLE_MESSAGE
                + "AS m ON mt.message_id=m.id" + SQL_WHERE + "m.id IS NULL");
        // process_message_state for missing processes or messages
        inconsistencyCleanupQueries.add("DELETE ms" + SQL_FROM + TABLE_PROCESS_MESSAGE_STATE + "AS ms" + SQL_LEFT_JOIN  + TABLE_PROCESS
                + "AS p ON ms.process_id=p.id" + SQL_WHERE + "p.id IS NULL");
        paramValueForMissingPref(TABLE_PARAM_ADDRESS);
        paramValueForMissingPref(TABLE_PARAM_BLOB);
        paramValueForMissingPref(TABLE_PARAM_DATE);
        paramValueForMissingPref(TABLE_PARAM_DATETIME);
        paramValueForMissingPref(TABLE_PARAM_EMAIL);
        paramValueForMissingPref(TABLE_PARAM_FILE);
        paramValueForMissingPref(TABLE_PARAM_LIST);
        paramValueForMissingPref(TABLE_PARAM_LISTCOUNT);
        paramValueForMissingPref(TABLE_PARAM_MONEY);
        paramValueForMissingPref(TABLE_PARAM_PHONE);
        paramValueForMissingPref(TABLE_PARAM_PHONE_ITEM);
        paramValueForMissingPref(TABLE_PARAM_TREE);

        paramValueForMissingObject(Process.OBJECT_TYPE, TABLE_PROCESS);
        paramValueForMissingObject(Customer.OBJECT_TYPE, TABLE_CUSTOMER);
        paramValueForMissingObject(User.OBJECT_TYPE, TABLE_USER);
        paramValueForMissingObject(AddressHouse.OBJECT_TYPE, TABLE_ADDRESS_HOUSE);
    }

    private void paramValueForMissingPref(String table) {
        inconsistencyCleanupQueries.add("DELETE pv" + SQL_FROM + table + "AS pv" + SQL_LEFT_JOIN + TABLE_PARAM_PREF
                + "AS pp ON pv.param_id=pp.id" + SQL_WHERE + "pp.id IS NULL");
    }

    private void paramValueForMissingObject(String objectType, String objectTable) {
        Collection<Integer> paramIds = ParameterCache.getObjectTypeParameterIds(objectType);
        if (paramIds.isEmpty())
            return;

        paramValueForMissingObject(Utils.toString(paramIds), TABLE_PARAM_ADDRESS, objectTable);
    }

    private void paramValueForMissingObject(String paramIds, String paramValueTable, String objectTable) {
        inconsistencyCleanupQueries.add("DELETE pv" + SQL_FROM + paramValueTable + "AS pv" + SQL_LEFT_JOIN + objectTable
                + "AS o ON pv.id=o.id" + SQL_WHERE + "pv.param_id IN (" + paramIds + ") AND o.id IS NULL");
    }
}
