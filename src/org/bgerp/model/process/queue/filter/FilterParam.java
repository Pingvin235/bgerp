package org.bgerp.model.process.queue.filter;

import java.util.Date;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.sql.LikePattern;

import ru.bgcrm.dao.process.QueueSelectParams;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class FilterParam extends Filter {
    private Parameter parameter;

    public FilterParam(int id, ConfigMap filter, Parameter parameter) {
        super(id, filter);
        this.parameter = parameter;
    }

    public Parameter getParameter() {
        return parameter;
    }

    @Override
    public void apply(DynActionForm form, QueueSelectParams params) {
        StringBuilder joinPart = params.joinPart;
        StringBuilder wherePart = params.wherePart;

        int paramId = parameter.getId();
        String paramType = parameter.getType();

        if (Parameter.TYPE_ADDRESS.equals(paramType)) {
            String city = form.getParam("param" + parameter.getId() + "valueCity");
            String street = form.getParam("param" + parameter.getId() + "valueStreet");
            String quarter = form.getParam("param" + parameter.getId() + "valueQuarter");
            String houseAndFrac = form.getParam("param" + parameter.getId() + "valueHouse");
            int flat = form.getParamInt("param" + parameter.getId() + "valueFlat", 0);
            int streetId = form.getParamInt("param" + parameter.getId() + "valueStreetId", 0);
            int houseId = form.getParamInt("param" + parameter.getId() + "valueHouseId", 0);
            int quarterId = form.getParamInt("param" + parameter.getId() + "valueQuarterId", 0);
            int cityId = form.getParamInt("param" + parameter.getId() + "valueCityId", 0);

            String paramAlias = " paramAddress" + parameter.getId();
            String cityAlias = paramAlias + "city";
            String streetAlias = paramAlias + "street";
            String houseAlias = paramAlias + "house";
            String quarterAlias = paramAlias + "quarter";

            if (cityId > 0 || flat > 0 || houseId > 0 || streetId > 0 || quarterId > 0 ||
                    Utils.notEmptyString(city) || Utils.notEmptyString(street) || Utils.notEmptyString(quarter) ||  Utils.notEmptyString(houseAndFrac)) {
                joinPart.append(SQL_INNER_JOIN);
                joinPart.append(org.bgerp.dao.param.Tables.TABLE_PARAM_ADDRESS);
                joinPart.append(" AS " + paramAlias + " ON process.id=" + paramAlias + ".id AND " + paramAlias
                        + ".param_id=" + parameter.getId() + " ");

                if (flat > 0) {
                    joinPart.append(SQL_AND);
                    joinPart.append(paramAlias + ".flat='" + flat + "' ");
                }

                if (houseId > 0) {
                    joinPart.append(SQL_AND);
                    joinPart.append(paramAlias + ".house_id=" + houseId + " ");
                } else {
                    joinPart.append(SQL_INNER_JOIN);
                    joinPart.append(org.bgerp.dao.param.Tables.TABLE_ADDRESS_HOUSE);
                    joinPart.append(" AS " + houseAlias + " ON " + paramAlias + ".house_id=" + houseAlias + ".id ");

                    if (Utils.notEmptyString(houseAndFrac)) {
                        AddressHouse houseFrac = new AddressHouse().withHouseAndFrac(houseAndFrac);
                        if (houseFrac.getHouse() > 0) {
                            joinPart.append(SQL_AND);
                            joinPart.append(houseAlias + ".house=" + houseFrac.getHouse() + " ");
                        }
                        if (Utils.notEmptyString(houseFrac.getFrac())) {
                            joinPart.append(SQL_AND);
                            joinPart.append(houseAlias + ".frac='" + houseFrac.getFrac() + "' ");
                        }
                    }

                    if (quarterId > 0) {
                        joinPart.append(SQL_INNER_JOIN);
                        joinPart.append(org.bgerp.dao.param.Tables.TABLE_ADDRESS_QUARTER);
                        joinPart.append(" AS " + quarterAlias + " ON " + houseAlias + ".quarter_id="
                                + quarterAlias + ".id AND" + quarterAlias + ".id=" + quarterId);
                    } else if (Utils.notBlankString(quarter)) {
                        //TODO: Сделать по запросу.
                    }

                    if (streetId > 0) {
                        joinPart.append(SQL_INNER_JOIN);
                        joinPart.append(org.bgerp.dao.param.Tables.TABLE_ADDRESS_STREET);
                        joinPart.append(" AS " + streetAlias + " ON " + houseAlias + ".street_id=" + streetAlias
                                + ".id AND " + streetAlias + ".id=" + streetId);
                    } else if (Utils.notEmptyString(street)) {
                        joinPart.append(SQL_INNER_JOIN);
                        joinPart.append(org.bgerp.dao.param.Tables.TABLE_ADDRESS_STREET);
                        joinPart.append(" AS " + streetAlias + " ON " + houseAlias + ".street_id=" + streetAlias
                                + ".id AND " + streetAlias + ".title LIKE '%" + street + "%' ");
                    }

                    Runnable addStreetJoin = () -> {
                        // JOIN может быть уже добавлен фильтром по названию улицы
                        if (!joinPart.toString().contains(streetAlias)) {
                            joinPart.append(SQL_INNER_JOIN);
                            joinPart.append(org.bgerp.dao.param.Tables.TABLE_ADDRESS_STREET);
                            joinPart.append(" AS " + streetAlias + " ON " + houseAlias + ".street_id=" + streetAlias + ".id ");
                        }
                    };

                    if (streetId <= 0 && quarterId <= 0) {
                        if (cityId > 0) {
                            addStreetJoin.run();
                            // добавка к фильтру по названию улицы
                            joinPart.append(" AND " + streetAlias + ".city_id=" + cityId);
                        } else if (Utils.notBlankString(city)) {
                            addStreetJoin.run();
                            // добавка джойна города
                            joinPart.append(SQL_INNER_JOIN);
                            joinPart.append(org.bgerp.dao.param.Tables.TABLE_ADDRESS_CITY);
                            joinPart.append(" AS " + cityAlias + " ON " + cityAlias + ".id=" + streetAlias +
                                    ".city_id AND " + cityAlias + ".title LIKE '%" + city + "%' ");
                        }
                    }
                }
            }
        } else if (Parameter.TYPE_DATE.equals(paramType) || Parameter.TYPE_DATETIME.equals(paramType)) {
            final String paramPrefix = "dateTimeParam" + parameter.getId();

            final boolean orEmpty = configMap.getBoolean("orEmpty", false);

            Date dateFrom = form.getParamDate(paramPrefix + "From");
            Date dateTo = form.getParamDate(paramPrefix + "To");

            if (configMap.get("valueFrom", "").equals("curdate"))
                dateFrom = new Date();
            if (configMap.get("valueTo", "").equals("curdate"))
                dateTo = new Date();

            final String tableAlias = "param_dx_" + parameter.getId();

            if (dateFrom != null || dateTo != null || orEmpty) {
                joinPart.append(SQL_INNER_JOIN + "param_" + parameter.getType() + " AS " + tableAlias + " ON process.id=" + tableAlias + ".id AND " + tableAlias + ".param_id=" + parameter.getId());

                if (orEmpty)
                    joinPart.append(" AND (" + tableAlias + ".param_id IS NULL OR (1>0 ");

                if (dateFrom != null)
                    joinPart.append(" AND " + tableAlias + ".value>=" + TimeUtils.formatSqlDate(dateFrom));

                if (dateTo != null)
                    joinPart.append(" AND " + tableAlias + ".value<" + TimeUtils.formatSqlDate(TimeUtils.getNextDay(dateTo)));

                if (orEmpty)
                    joinPart.append("))");
            }
        } else if (Parameter.TYPE_LIST.equals(paramType) || Parameter.TYPE_LISTCOUNT.equals(paramType)) {
            String values = getValues(form, "param" + paramId + "value");

            if (Utils.isBlankString(values)) {
                return;
            }

            String tableAlias = "param_lx_" + paramId;

            joinPart.append(SQL_INNER_JOIN);
            if (Parameter.TYPE_LIST.equals(paramType)) {
                joinPart.append(org.bgerp.dao.param.Tables.TABLE_PARAM_LIST);
            } else {
                joinPart.append(org.bgerp.dao.param.Tables.TABLE_PARAM_LISTCOUNT);
            }
            joinPart.append("AS " + tableAlias + " ON process.id=" + tableAlias + ".id AND " + tableAlias + ".param_id="
                    + paramId + " AND " + tableAlias + ".value IN(" + values + ")");
        } else if (Parameter.TYPE_MONEY.equals(paramType)) {
            String tableAlias = "param_money_" + paramId;

            if (joinPart.toString().contains(tableAlias) || wherePart.toString().contains(tableAlias))
                log.error("Duplicated filter on param type 'money' with ID: ", getId());
            else {
                boolean empty = form.getParamBoolean("param" + paramId + "empty");
                var from = Utils.parseBigDecimal(form.getParam("param" + paramId + "From"), null);
                var to = Utils.parseBigDecimal(form.getParam("param" + paramId + "To"), null);

                if (empty || from != null || to != null) {
                    if (empty)
                        joinPart.append(SQL_LEFT_JOIN);
                    else
                        joinPart.append(SQL_INNER_JOIN);

                    joinPart.append(org.bgerp.dao.param.Tables.TABLE_PARAM_MONEY);
                    joinPart.append(
                            "AS " + tableAlias + " ON process.id=" + tableAlias + ".id AND " + tableAlias + ".param_id=" + paramId);

                    if (empty)
                        wherePart.append(" AND " + tableAlias + ".value IS NULL ");
                    else {
                        if (from != null)
                            joinPart.append(" AND " + Utils.format(from) + "<=" + tableAlias + ".value ");

                        if (to != null)
                            joinPart.append(" AND " + tableAlias + ".value<=" + Utils.format(to));
                    }
                }
            }
        } else if (Parameter.TYPE_TEXT.equals(paramType) || Parameter.TYPE_BLOB.equals(paramType)) {
            String mode = configMap.get("mode");
            String value = form.getParam("param" + paramId + "value");

            if (Utils.notBlankString(value)) {
                joinPart.append(SQL_INNER_JOIN);

                if (Parameter.TYPE_BLOB.equals(paramType)) {
                    joinPart.append(org.bgerp.dao.param.Tables.TABLE_PARAM_BLOB);
                } else if (Parameter.TYPE_TEXT.equals(paramType)) {
                    joinPart.append(org.bgerp.dao.param.Tables.TABLE_PARAM_TEXT);
                }

                if ("regexp".equals(mode)) {
                    joinPart.append("AS param_text ON process.id=param_text.id AND param_text.param_id="
                            + paramId + " AND param_text.value RLIKE '" + value + "'");
                }
                if ("numeric".equals(mode)) {
                    joinPart.append("AS param_text ON process.id=param_text.id AND param_text.param_id="
                            + paramId + " AND ( 1>1 ");
                    for (String val : value.split(",")) {
                        if (val.contains("-")) {
                            String[] bVal = val.split("-");
                            joinPart.append(
                                    " OR param_text.value between '" + bVal[0] + "' AND '" + bVal[1] + "'");
                        } else {
                            joinPart.append(" OR param_text.value = '" + val + "'");
                        }
                    }
                    joinPart.append(" )");
                } else {
                    joinPart.append(" AS param_text ON process.id=param_text.id AND param_text.param_id="
                            + paramId + " AND param_text.value LIKE '" + LikePattern.SUB.get(value) + "'");
                }
            }
        }
    }
}
