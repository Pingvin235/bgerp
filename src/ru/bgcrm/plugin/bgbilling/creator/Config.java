package ru.bgcrm.plugin.bgbilling.creator;

import static org.bgerp.model.param.Parameter.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.BGException;
import org.bgerp.model.param.Parameter;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.util.Utils;

public class Config extends org.bgerp.app.cfg.Config {
    private static final Set<String> ALLOWED_CONFIRM_PARAM_TYPES = new HashSet<String>(Arrays.asList(TYPE_ADDRESS, TYPE_TEXT, TYPE_PHONE, TYPE_DATE));
    private static final Set<String> ALLOWED_SEARCH_PARAM_TYPES = new HashSet<String>(Arrays.asList(TYPE_ADDRESS, TYPE_TEXT, TYPE_PHONE));
    private static final Set<String> ALLOWED_IMPORT_PARAM_TYPES = new HashSet<String>(
            Arrays.asList(TYPE_ADDRESS, TYPE_TEXT, TYPE_PHONE, TYPE_DATE, TYPE_LIST, TYPE_EMAIL));

    public static final class ParameterGroupTitlePatternRule {
        public int parameterGroupId;
        public int titlePatternId;
        public Pattern pattern;

        public boolean check(String contractTitle) {
            return pattern == null || pattern.matcher(contractTitle).find();
        }
    }

    public final List<ServerCustomerCreator> serverCreatorList = new ArrayList<>();

    // коды биллингов, импортируемые; если не указаны - импортируются все
    public final Set<String> importBillingIds;

    // подтверждающие контрагента поля для первой фазы поиска
    public final List<Parameter> confirmParameterList;
    // поля по которым ищут контрагента, вторая фаза поиска
    public final List<Parameter> searchParameterList;
    // максимальное расстояние между наименованиями для второго режима поиска
    private final int titleDistance;
    // это же расстояние, определённое для конкретного параметра
    private final Map<Integer, Integer> titleDistanceForParam = new HashMap<>();
    // поля, импортируемые при привязке договора к контрагенту
    public final List<Parameter> importParameterList;
    // правила определения группы параметров контрагента из номера договора
    private final List<ParameterGroupTitlePatternRule> paramGroupRuleList = new ArrayList<>();

    public Config(ConfigMap setup) throws BGException {
        super(setup);

        final String prefix = "bgbilling:creator.";

        importBillingIds = Utils.toSet(setup.get(prefix + "importBillingIds", ""));

        confirmParameterList = loadFields(setup, prefix + "confirmParameters", ALLOWED_CONFIRM_PARAM_TYPES);
        searchParameterList = loadFields(setup, prefix + "searchParameters", ALLOWED_SEARCH_PARAM_TYPES);
        importParameterList = loadFields(setup, prefix + "importParameters", ALLOWED_IMPORT_PARAM_TYPES);

        titleDistance = setup.getInt(prefix + "titleDistance", 2);
        for (Map.Entry<String, String> me : setup.sub(prefix + "titleDistance.").entrySet()) {
            titleDistanceForParam.put(Utils.parseInt(me.getKey()), Utils.parseInt(me.getValue()));
        }

        final String paramGroupPrefix = prefix + "parameterGroupRule.";

        for (Map.Entry<Integer, ConfigMap> me : setup.subIndexed(paramGroupPrefix).entrySet()) {
            int paramGroupId = me.getValue().getInt("paramGroupId", 0);
            // -1 - по-умолчанию без шаблона
            int titlePatternId = me.getValue().getInt("titlePatternId", -1);
            String contractTitlePattern = me.getValue().get("contractTitlePattern");

            if (paramGroupId <= 0) {
                continue;
            }

            ParameterGroupTitlePatternRule rule = new ParameterGroupTitlePatternRule();
            rule.parameterGroupId = paramGroupId;
            rule.titlePatternId = titlePatternId;
            if (Utils.notBlankString(contractTitlePattern)) {
                rule.pattern = Pattern.compile(contractTitlePattern);
            }

            paramGroupRuleList.add(rule);
        }

        log.info("Loaded " + paramGroupRuleList.size() + " param group rules.");

        for (Map.Entry<Integer, ConfigMap> me : setup.subIndexed(prefix + "server.").entrySet()) {
            int id = me.getKey();
            ConfigMap params = me.getValue();
            try {
                serverCreatorList.add(new ServerCustomerCreator(this, params));
            } catch (Exception e) {
                throw new BGException("Error load customer creator " + id + ": " + e.getMessage());
            }
        }
    }

    public ParameterGroupTitlePatternRule getCustomerParameterGroup(String contractTitle) {
        for (ParameterGroupTitlePatternRule rule : paramGroupRuleList) {
            if (rule.check(contractTitle)) {
                return rule;
            }
        }
        return null;
    }

    private List<Parameter> loadFields(ConfigMap setup, String prefix, Set<String> allowedTypes) throws BGException {
        List<Parameter> parameters = new ArrayList<Parameter>();

        for (int paramId : Utils.toIntegerList(setup.get(prefix))) {
            if (paramId <= 0) {
                continue;
            }

            Parameter param = ParameterCache.getParameter(paramId);
            if (param == null) {
                throw new BGException("Can't find parameter: " + paramId);
            }

            if (!allowedTypes.contains(param.getType())) {
                throw new BGException("Unsupported key param type:" + param.getType());
            }

            parameters.add(param);
        }

        return parameters;
    }

    public ServerCustomerCreator getServerCustomerCreator(String billingId, Connection con) {
        for (ServerCustomerCreator scc : serverCreatorList) {
            if (billingId.equals(scc.getBillingId())) {
                return scc;
            }
        }
        return null;
    }

    public int getMaxTitleDistance(int paramId) {
        if (titleDistanceForParam.containsKey(paramId)) {
            return titleDistanceForParam.get(paramId);
        }
        return titleDistance;
    }
}
