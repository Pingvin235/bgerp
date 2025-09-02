package org.bgerp.dao.expression;

import java.sql.Connection;
import java.util.Map;

import ru.bgcrm.model.customer.Customer;

/**
 * Expression object for accessing customer parameters
 *
 * @author Shamil Vakhitov
 */
public class CustomerParamExpressionObject extends ParamExpressionObject {
    private static final String KEY = Customer.OBJECT_TYPE + "Param";
    private static final String KEY_SHORT = "cp";

    public CustomerParamExpressionObject(Connection con, int customerId) {
        super(con, customerId);
    }

    @Override
    public void toContext(Map<String, Object> context) {
        context.put(KEY, this);
        context.put(KEY_SHORT, this);
    }
}
