package org.bgerp.itest.helper;

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

    public static int addParam(ParamDAO dao, String object, String type, String title, int pos, String config, String valuesConfig) throws SQLException {
        var param = new Parameter();
        param.setId(-1);
        param.setComment("");
        param.setObject(object);
        param.setType(type);
        param.setTitle(title);
        param.setOrder(pos);
        param.setConfig(config);
        param.setValuesConfig(valuesConfig);
        dao.updateParameter(param);
        Assert.assertTrue(param.getId() > 0);
        return param.getId();
    }

    public static int addParamGroup(ParamGroupDAO dao, String object, String title, Set<Integer> params) throws SQLException {
        var group = new ParameterGroup();
        group.setObject(object);
        group.setTitle(title);
        group.setParameterIds(params);
        dao.updateParameterGroup(group);
        Assert.assertTrue(group.getId() > 0);
        return group.getId();
    }
    
    public static int addPattern(PatternDAO dao, String object, String title, String text) throws SQLException {
        var pattern = new Pattern();
        pattern.setObject(object);
        pattern.setTitle(title);
        pattern.setPattern(text);
        dao.updatePattern(pattern);
        Assert.assertTrue(pattern.getId() > 0);
        return pattern.getId();
    }
}
