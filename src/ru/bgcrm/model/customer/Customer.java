package ru.bgcrm.model.customer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import ru.bgcrm.cache.CustomerGroupCache;
import ru.bgcrm.dao.ParamGroupDAO;
import ru.bgcrm.dao.PatternDAO;
import ru.bgcrm.model.SearchableIdTitle;
import ru.bgcrm.model.param.ParameterGroup;
/**
 * Customer object.
 *
 * @author Shamil Vakhitov.
 */
public class Customer extends SearchableIdTitle implements Comparable<Customer> {
    public static final String OBJECT_TYPE = "customer";

    // уникальные параметры к названиею
    private String reference;
    private String password;
    private int titlePatternId = -1;
    private String titlePattern = "";
    private int paramGroupId = 0;
    private Date createdDate;
    private int createdUserId = -1;
    private Set<Integer> groupIds;

    public String getReference() {
        return reference;
    }

    public void setReference(String uniqueKeys) {
        this.reference = uniqueKeys;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTitlePatternId() {
        return titlePatternId;
    }

    public void setTitlePatternId(int titlePatternId) {
        this.titlePatternId = titlePatternId;
    }

    public String getTitlePattern() {
        return titlePattern;
    }

    public void setTitlePattern(String titlePattern) {
        this.titlePattern = titlePattern;
    }

    public int getParamGroupId() {
        return paramGroupId;
    }

    public void setParamGroupId(int paramGroupId) {
        this.paramGroupId = paramGroupId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public int getCreatedUserId() {
        return createdUserId;
    }

    public void setCreatedUserId(int createdUserId) {
        this.createdUserId = createdUserId;
    }

    public Set<Integer> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(Set<Integer> groupIds) {
        this.groupIds = groupIds;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("[ id=");
        str.append(getId());
        str.append(", title=");
        str.append(getTitle());
        str.append(", createdDate=");
        str.append(getCreatedDate());
        str.append(", createdUserId=");
        str.append(getCreatedUserId());
        str.append(" ]");
        return str.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return ((Customer) obj).getId() == id;
    }

    @Override
    public int compareTo(Customer o) {
        return o.getTitle().compareTo(title);
    }

    @Override
    public int hashCode() {
        return id;
    }

    public String toLog(Connection con, Customer oldCustomer) throws SQLException {
        StringBuffer result = new StringBuffer();

        if (groupIds != null && oldCustomer.getGroupIds() != null) {
            if (!CollectionUtils.isEqualCollection(groupIds, oldCustomer.getGroupIds())) {
                String textGroups = "";
                Map<Integer, CustomerGroup> groups = CustomerGroupCache.getGroupMap();

                for (Integer item : groupIds) {
                    textGroups += groups.get(item).getTitle() + ", ";
                }

                if (textGroups.length() > 0) {
                    textGroups = textGroups.substring(0, textGroups.length() - 2);
                    result.append("Группы: [" + textGroups + "]; ");
                } else {
                    result.append("Группы: []; ");
                }
            }
        }

        if (title != null && oldCustomer.getTitle() != null) {
            if (!title.equals(oldCustomer.getTitle()) && title.length() > 0) {
                result.append("Название: " + title + "; ");
            }
        }

        if (paramGroupId != oldCustomer.getParamGroupId()) {
            ParamGroupDAO paramGroupDAO = new ParamGroupDAO(con);
            ParameterGroup paramGroup = paramGroupDAO.getParameterGroup(paramGroupId);

            if (paramGroup != null) {
                result.append("Группа параметров: " + paramGroupDAO.getParameterGroup(paramGroupId).getTitle() + "; ");
            }

            else {
                result.append("Группа параметров: группа не установлена; ");
            }
        }

        if (titlePatternId != oldCustomer.getTitlePatternId()) {
            String patternTitle = "";
            PatternDAO patternDAO = new PatternDAO(con);

            switch (titlePatternId) {
                case -1: {
                    patternTitle = "Без шаблона";
                    break;
                }

                case 0: {
                    patternTitle = "Персональный шаблон";
                    break;
                }

                default: {
                    patternTitle = patternDAO.getPattern(titlePatternId).getTitle();
                }
            }

            result.append("Шаблон названия: " + patternTitle + "; ");
        }

        if (titlePattern != null && oldCustomer.getTitlePattern() != null) {
            if (titlePattern.length() > 0 && !titlePattern.equals(oldCustomer.getTitlePattern())) {
                result.append("Персональный шаблон названия: " + titlePattern + "; ");
            }
        }

        return result.toString();
    }
}
