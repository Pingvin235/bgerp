package ru.bgcrm.plugin.bgbilling.proto.dao.version.v8x;

import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractHierarchyDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.DirectoryDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractInfo;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractMemo;
import ru.bgcrm.plugin.bgbilling.proto.model.UserInfo;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


public class ContractDAO8x extends ContractDAO {
    public ContractDAO8x(User user, String billingId) throws BGException {
        super(user, billingId);
    }

    public ContractDAO8x(User user, DBInfo dbInfo) throws BGException {
        super(user, dbInfo);
    }

    @Override
    public ContractInfo getContractInfo(int contractId) throws BGException {
        ContractInfo result = null;

        RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.api",
                "ContractService", "contractInfoGet");
        req.setParamContractId(contractId);
        JsonNode res = transferData.postDataReturn(req, user);
        //String contractInfoString = jsonMapper.convertValue(res, String.class);
        //JSONObject contractInfo = new JSONObject(contractInfoString);JSONObject
        JSONObject contractInfo = jsonMapper.convertValue(res, JSONObject.class);
        JSONObject contract = contractInfo.optJSONObject("contract");
        if (contract != null) {
            result = new ContractInfo();

            result.setBillingId(dbInfo.getId());
            result.setId(contractId);
            result.setComment(contract.optString("comment"));
            result.setObjects(Utils.parseInt(contract.optString("objects").split("/")[0]),
                    Utils.parseInt(contract.optString("objects").split("/")[1]));
            result.setHierarchy(contract.optString("hierarchy"));
            result.setHierarchyDep(Utils.parseInt(contract.optString("hierarchyDep", null)));
            result.setHierarchyIndep(Utils.parseInt(contract.optString("hierarchyIndep", null)));
            result.setDeleted(Utils.parseBoolean(contract.optString("del")));
            result.setFace(Utils.parseInt(contract.optString("fc")));
            result.setDateFrom(TimeUtils.parse(contract.optString("date1"), TimeUtils.PATTERN_DDMMYYYY));
            result.setDateTo(TimeUtils.parse(contract.optString("date2"), TimeUtils.PATTERN_DDMMYYYY));
            result.setMode(Utils.parseInt(contract.optString("mode")));
            result.setBalanceLimit(Utils.parseBigDecimal(contract.optString("limit"), BigDecimal.ZERO));
            result.setStatus(contract.optString("status"));
            result.setTitle(contract.optString("title"));
            result.setComments(Utils.parseInt(contract.optString("comments")));

            if ("super".equals(contract.optString("hierarchy"))) {
                result.setSubContractIds(new ContractHierarchyDAO(user, dbInfo).getSubContracts(contractId));
            }

            JSONObject infoJson = contractInfo.optJSONObject("info");
            result.setGroupList(getList(infoJson, "groups"));
            result.setTariffList(getList(infoJson, "tariff"));
            result.setScriptList(getList(infoJson, "script"));

            JSONArray modulesJson = infoJson.optJSONArray("modules");
            if (modulesJson != null && !modulesJson.isEmpty()) {
                List<ContractInfo.ModuleInfo> moduleList = new ArrayList<>();
                for (int i = 0; i < modulesJson.length(); i++) {
                    JSONObject moduleJson = modulesJson.getJSONObject(i);
                    int moduleId = moduleJson.optInt("id", -1);
                    String moduleTitle = moduleJson.optString("title", "?");
                    String status = moduleJson.optString("status", null);
                    String modulePackage = moduleJson.optString("package");
                    if (status == null) {
                        status = "";
                    }
                    moduleList.add(new ContractInfo.ModuleInfo(moduleId, moduleTitle, modulePackage, status));
                }
                result.setModuleList(moduleList);
            }


            JSONObject balanceJson = infoJson.optJSONObject("balance");
            if (balanceJson != null) {
                result.setBalanceDate(
                        new GregorianCalendar(balanceJson.optInt("yy", -1), balanceJson.optInt("mm", -1), 1)
                                .getTime());
                result.setBalanceIn(Utils.parseBigDecimal(balanceJson.optString("summa1", "0"), BigDecimal.ZERO));
                result.setBalancePayment(Utils.parseBigDecimal(balanceJson.optString("summa2", "0"), BigDecimal.ZERO));
                result.setBalanceAccount(Utils.parseBigDecimal(balanceJson.optString("summa3", "0"), BigDecimal.ZERO));
                result.setBalanceCharge(Utils.parseBigDecimal(balanceJson.optString("summa4", "0"), BigDecimal.ZERO));
                result.setBalanceOut(Utils.parseBigDecimal(balanceJson.optString("summa5", "0"), BigDecimal.ZERO));
            }
        }

        return result;
    }

    public void bgbillingUpdateContractTitleAndComment(int contractId, String comment, int patid) throws BGException {
        RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.api",
                "ContractService", "contractTitleAndCommentUpdate");

        req.setParamContractId(contractId);
        if (patid > 0) {
            req.setParam("patid", patid);
        }
        req.setParam("comment", comment);
        transferData.postDataReturn(req, user);
    }

    public void deleteLimitTask(int contractId, int id) throws BGException {
        RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.limit", "ContractLimitService",
                "cancelLimitChangeTask");

        req.setParamContractId(contractId);
        req.setParam("taskIds", Collections.singletonList(id));
        transferData.postDataReturn(req, user);
    }


    public List<ContractMemo> getMemoList(int contractId) throws BGException {
        RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.api", "ContractNoteService",
                "contractNoteList");
        req.setParamContractId(contractId);
        req.setParam("customer", false);
        JsonNode res = transferData.postDataReturn(req, user);
        List<ContractMemo8x> contractMemos = new ArrayList<>();
        contractMemos = readJsonValue(res.traverse(), jsonTypeFactory.constructCollectionType(List.class, ContractMemo8x.class));
        DirectoryDAO directoryDAO = new DirectoryDAO(user, dbInfo);
        Map<Integer, UserInfo> users = directoryDAO.getUsersInfo();
        List<ContractMemo> result = contractMemos.stream().map(i -> {
            ContractMemo item = i.convertToContractMemo();
            UserInfo userInfo = users.get(i.getUserId());
            if (userInfo != null) {
                item.setUser(userInfo.getName());
            }
            return item;
        }).collect(Collectors.toList());
        return result;
    }

    public ContractMemo getMemo(int contractId, int memoId) throws BGException {

        ContractMemo8x contractMemo = getContractMemo8x(memoId);
        DirectoryDAO directoryDAO = new DirectoryDAO(user, dbInfo);
        Map<Integer, UserInfo> users = directoryDAO.getUsersInfo();
        ContractMemo item = contractMemo.convertToContractMemo();
        UserInfo userInfo = users.get(contractMemo.getUserId());
        if (userInfo != null) {
            item.setUser(userInfo.getName());
        }
        return item;
    }

    private ContractMemo8x getContractMemo8x(int memoId) throws BGException {
        RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.api", "ContractNoteService",
                "getContractNote");
        req.setParam("contractNoteId", memoId);
        JsonNode res = transferData.postDataReturn(req, user);
        ContractMemo8x contractMemo = jsonMapper.convertValue(res, ContractMemo8x.class);
        return contractMemo;
    }


    public void updateMemo(int contractId, int memoId, String memoTitle, String memoText) throws BGException {
        updateMemo(contractId, memoId, memoTitle, memoText, false);
    }

    public void updateMemo(int contractId, int memoId, String memoTitle, String memoText, boolean visible) throws BGException {
        RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.api", "ContractNoteService",
                "contractNoteUpdate");
        req.setParamContractId(contractId);
        ContractMemo8x contractMemo = new ContractMemo8x();
        contractMemo.setId(memoId);
        contractMemo.setContractId(contractId);
        contractMemo.setTitle(memoTitle);
        contractMemo.setComment(memoText);
        contractMemo.setVisible(visible);
        contractMemo.setDateTime(new Date());
        contractMemo.setUserId(dbInfo.getBillingUserId(user));


        req.setParam("contractNote", contractMemo);
        transferData.postDataReturn(req, user);
    }

    public void deleteMemo(int contractId, int memoId) throws BGException {
        //contractId почемуто потерялся!
        ContractMemo8x memo = getContractMemo8x(memoId);
        if (memo != null) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.api", "ContractNoteService",
                    "contractNoteDelete");
            req.setParamContractId(memo.getContractId());
            req.setParam("id", memoId);
            transferData.postDataReturn(req, user);
        }


    }


    public static class ContractMemo8x {
        private int id;
        private int contractId;
        private String title;
        private int userId;
        private Date dateTime;
        private String comment;
        private boolean visible = false;

        public ContractMemo convertToContractMemo() {
            ContractMemo res = new ContractMemo();
            res.setId(id);
            res.setTitle(title);
            res.setText(comment);
            res.setTime(dateTime);
            res.setVisibleForUser(visible);
            return res;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getContractId() {
            return contractId;
        }

        public void setContractId(int contractId) {
            this.contractId = contractId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public Date getDateTime() {
            return dateTime;
        }

        public void setDateTime(Date dateTime) {
            this.dateTime = dateTime;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) { this.comment = comment; }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }
    }
}
