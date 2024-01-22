package org.bgerp.itest.helper;

import java.sql.SQLException;
import java.util.Set;

import org.bgerp.dao.param.ParamDAO;
import org.bgerp.dao.param.ParamGroupDAO;
import org.bgerp.itest.kernel.db.DbTest;
import org.testng.Assert;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.PatternDAO;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterGroup;
import ru.bgcrm.model.param.Pattern;

public class ParamHelper {

    public static int addParam(String object, String type, String title, int pos, String config, String valuesConfig) throws SQLException {
        var con = DbTest.conRoot;

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

        ParameterCache.flush(con);

        return param.getId();
    }

    public static int addParamGroup(String object, String title, Set<Integer> params) throws SQLException {
        var con = DbTest.conRoot;

        var group = new ParameterGroup();
        group.setObject(object);
        group.setTitle(title);
        group.setParameterIds(params);
        new ParamGroupDAO(con).updateParameterGroup(group);
        Assert.assertTrue(group.getId() > 0);

        return group.getId();
    }

    public static int addPattern(String object, String title, String text) throws SQLException {
        var con = DbTest.conRoot;

        var pattern = new Pattern();
        pattern.setObject(object);
        pattern.setTitle(title);
        pattern.setPattern(text);
        new PatternDAO(con).updatePattern(pattern);
        Assert.assertTrue(pattern.getId() > 0);

        return pattern.getId();
    }
}
