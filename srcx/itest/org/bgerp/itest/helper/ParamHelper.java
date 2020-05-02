package org.bgerp.itest.helper;

import static org.bgerp.itest.kernel.db.DbTest.conPoolRoot;

import java.sql.SQLException;
import java.util.Set;

import org.testng.Assert;

import ru.bgcrm.dao.ParamDAO;
import ru.bgcrm.dao.ParamGroupDAO;
import ru.bgcrm.dao.PatternDAO;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterGroup;
import ru.bgcrm.model.param.Pattern;

public class ParamHelper {

    public static int addParam(String object, String type, String title, int pos, String config, String valuesConfig) throws SQLException {
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
            var param = new Parameter();
            param.setId(-1);
            param.setComment("");
            param.setObject(object);
            param.setType(type);
            param.setTitle(title);
            param.setOrder(pos);
            param.setConfig(config);
            param.setValuesConfig(valuesConfig);
            new ParamDAO(con).updateParameter(param);
            Assert.assertTrue(param.getId() > 0);

            con.commit();

            return param.getId();
        }
    }

    public static int addParamGroup(String object, String title, Set<Integer> params) throws SQLException {
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
            var group = new ParameterGroup();
            group.setObject(object);
            group.setTitle(title);
            group.setParameterIds(params);
            new ParamGroupDAO(con).updateParameterGroup(group);
            Assert.assertTrue(group.getId() > 0);

            con.commit();

            return group.getId();
        }
    }
    
    public static int addPattern(String object, String title, String text) throws SQLException {
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
            var pattern = new Pattern();
            pattern.setObject(object);
            pattern.setTitle(title);
            pattern.setPattern(text);
            new PatternDAO(con).updatePattern(pattern);
            Assert.assertTrue(pattern.getId() > 0);

            con.commit();

            return pattern.getId();
        }
    }
}
