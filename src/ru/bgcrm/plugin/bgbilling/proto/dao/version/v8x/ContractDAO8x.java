package ru.bgcrm.plugin.bgbilling.proto.dao.version.v8x;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bgerp.model.base.IdStringTitle;

import com.fasterxml.jackson.databind.JsonNode;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.DirectoryDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractMemo;
import ru.bgcrm.plugin.bgbilling.proto.model.UserInfo;

// TODO: Создать отдельный класс ContractMemoDAO, куда вынести оставшиеся методы
@Deprecated
public class ContractDAO8x extends ContractDAO {
    public ContractDAO8x(User user, String billingId) {
        super(user, billingId);
    }

    public ContractDAO8x(User user, DBInfo dbInfo) {
        super(user, dbInfo);
    }

    @Override
    public List<ContractMemo> getMemoList(int contractId) {
        RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractNoteService", "contractNoteList");
        req.setParamContractId(contractId);
        req.setParam("customer", false);
        JsonNode res = transferData.postDataReturn(req, user);
        List<ContractMemo8x> contractMemos = readJsonValue(res.traverse(), jsonTypeFactory.constructCollectionType(List.class, ContractMemo8x.class));
        DirectoryDAO directoryDAO = new DirectoryDAO(user, dbInfo);
        Map<Integer, UserInfo> users = directoryDAO.getUsersInfo();
        return contractMemos.stream().map(i -> {
            ContractMemo item = i.convertToContractMemo();
            UserInfo userInfo = users.get(i.getUserId());
            if (userInfo != null) {
                item.setUser(userInfo.getName());
            }
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public ContractMemo getMemo(int contractId, int memoId) {
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

    private ContractMemo8x getContractMemo8x(int memoId) {
        RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractNoteService", "getContractNote");
        req.setParam("contractNoteId", memoId);
        JsonNode res = transferData.postDataReturn(req, user);
        return jsonMapper.convertValue(res, ContractMemo8x.class);
    }

    @Override
    public void updateMemo(int contractId, int memoId, String memoTitle, String memoText) {
        updateMemo(contractId, memoId, memoTitle, memoText, false);
    }

    @Override
    public void updateMemo(int contractId, int memoId, String memoTitle, String memoText, boolean visible) {
        RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractNoteService", "contractNoteUpdate");
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

    @Override
    public void deleteMemo(int contractId, int memoId) {
        //contractId почемуто потерялся!
        ContractMemo8x memo = getContractMemo8x(memoId);
        if (memo != null) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractNoteService", "contractNoteDelete");
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

    @Override
    public List<String[]> getContractCardTypes(int contractId) {
        List<String[]> result = new ArrayList<>();

        RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", "contractCardList");
        req.setParamContractId(contractId);

        List<IdStringTitle> list = readJsonValue(transferData.postDataReturn(req, user).traverse(),
                jsonTypeFactory.constructCollectionType(List.class, IdStringTitle.class));
        for (var item : list)
            result.add(new String[] { item.getId(), item.getTitle() });

        return result;
    }
}
