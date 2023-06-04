package ru.bgcrm.plugin.bgbilling.proto.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bgerp.model.base.IdTitle;

import ru.bgcrm.model.Pair;

public class ContractInfo extends Contract {
    public static class ModuleInfo {
        private final int moduleId;
        private final String title;
        private final String clientPackage;
        private final String status;

        public ModuleInfo(int moduleId, String title, String clientPackage, String status) {
            this.moduleId = moduleId;
            this.title = title;
            this.clientPackage = clientPackage;
            this.status = status;
        }

        public int getModuleId() {
            return moduleId;
        }

        public String getTitle() {
            return title;
        }

        public String getClientPackage() {
            return clientPackage;
        }

        public String getStatus() {
            return status;
        }
    }

    private int mode = 0;
    private int face = 0;
    private boolean deleted = false;
    private String comment;
    @Deprecated
    private String statisticPassword;
    private Pair<Integer, Integer> objects;
    private String status;
    private String hierarchy;
    private int hierarchyDep;
    private int hierarchyIndep;
    private Date dateFrom;
    private Date dateTo;
    private Date balanceDate;
    private BigDecimal balanceIn = BigDecimal.ZERO;
    private BigDecimal balancePayment = BigDecimal.ZERO;
    private BigDecimal balanceAccount = BigDecimal.ZERO;
    private BigDecimal balanceCharge = BigDecimal.ZERO;
    private BigDecimal balanceOut = BigDecimal.ZERO;
    private BigDecimal balanceLimit = BigDecimal.ZERO;
    private List<IdTitle> groupList = new ArrayList<IdTitle>();
    private List<ModuleInfo> moduleList = new ArrayList<ModuleInfo>();
    private List<IdTitle> tariffList = new ArrayList<IdTitle>();
    private List<IdTitle> scriptList = new ArrayList<IdTitle>();
    private int comments = 0;
    private List<Integer> subContractIds = new ArrayList<Integer>();

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getBalanceDate() {
        return balanceDate;
    }

    public void setBalanceDate(Date balanceDate) {
        this.balanceDate = balanceDate;
    }

    public BigDecimal getBalanceIn() {
        return balanceIn;
    }

    public void setBalanceIn(BigDecimal balanceIn) {
        this.balanceIn = balanceIn;
    }

    public BigDecimal getBalancePayment() {
        return balancePayment;
    }

    public void setBalancePayment(BigDecimal balancePayment) {
        this.balancePayment = balancePayment;
    }

    public BigDecimal getBalanceAccount() {
        return balanceAccount;
    }

    public void setBalanceAccount(BigDecimal balanceAccount) {
        this.balanceAccount = balanceAccount;
    }

    public BigDecimal getBalanceCharge() {
        return balanceCharge;
    }

    public void setBalanceCharge(BigDecimal balanceCharge) {
        this.balanceCharge = balanceCharge;
    }

    public BigDecimal getBalanceOut() {
        return balanceOut;
    }

    public void setBalanceOut(BigDecimal balanceOut) {
        this.balanceOut = balanceOut;
    }

    public BigDecimal getBalanceLimit() {
        return balanceLimit;
    }

    public void setBalanceLimit(BigDecimal balanceLimit) {
        this.balanceLimit = balanceLimit;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getFace() {
        return face;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(String hierarchy) {
        this.hierarchy = hierarchy;
    }

    public int getHierarchyDep() {
        return hierarchyDep;
    }

    public void setHierarchyDep(int hierarchyDep) {
        this.hierarchyDep = hierarchyDep;
    }

    public int getHierarchyIndep() {
        return hierarchyIndep;
    }

    public void setHierarchyIndep(int hierarchyIndep) {
        this.hierarchyIndep = hierarchyIndep;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date fromDate) {
        this.dateFrom = fromDate;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date toDate) {
        this.dateTo = toDate;
    }

    public List<IdTitle> getTariffList() {
        return tariffList;
    }

    public void setTariffList(List<IdTitle> tariffList) {
        this.tariffList = tariffList;
    }

    public List<IdTitle> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<IdTitle> groupList) {
        this.groupList = groupList;
    }

    public List<ModuleInfo> getModuleList() {
        return moduleList;
    }

    public void setModuleList(List<ModuleInfo> moduleList) {
        this.moduleList = moduleList;
    }

    public List<IdTitle> getScriptList() {
        return scriptList;
    }

    public void setScriptList(List<IdTitle> scriptList) {
        this.scriptList = scriptList;
    }

    public List<Integer> getSubContractIds() {
        return subContractIds;
    }

    public void setSubContractIds(List<Integer> subContractIds) {
        this.subContractIds = subContractIds;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public Pair<Integer, Integer> getObjects() {
        return objects;
    }

    public void setObjects(Integer active, Integer full) {
        this.objects = new Pair<Integer, Integer>(active, full);
    }

    @Deprecated
    public String getStatisticPassword() {
        return statisticPassword;
    }

    @Deprecated
    public void setStatisticPassword(String statisticPassword) {
        this.statisticPassword = statisticPassword;
    }

    public class ActionName {
        private String actionClass;
        private String actionName;

        public ActionName(String actionClass, String actionName) {
            setActionClass(actionClass);
            setActionName(actionName);
        }

        public String getActionClass() {
            return actionClass;
        }

        public void setActionClass(String action) {
            actionClass = action;
        }

        public String getActionName() {
            return actionName;
        }

        public void setActionName(String actionName) {
            this.actionName = actionName;
        }
    }

    public class ParameterValue {
        private String name;
        private String value;

        public ParameterValue(String parameterName, String parameterValue) {
            setValue(parameterValue);
            setName(parameterName);
        }

        public String getName() {
            return name;
        }

        public void setName(String parameterName) {
            this.name = parameterName;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String parameterValue) {
            this.value = parameterValue;
        }
    }
}
