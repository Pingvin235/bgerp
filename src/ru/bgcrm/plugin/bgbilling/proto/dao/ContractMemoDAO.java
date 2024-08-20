package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractMemo;
import ru.bgcrm.plugin.bgbilling.proto.model.UserInfo;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class ContractMemoDAO extends ContractDAO {
    public ContractMemoDAO(User user, String billingId) {
        super(user, billingId);
    }

    public List<ContractMemo> getMemoList(int contractId) {
        if (dbInfo.versionCompare("8.0") > 0) {
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
        } else {
            Request request = new Request();
            request.setModule("contract");
            request.setAction("ContractMemo");
            request.setContractId(contractId);

            Document document = transferData.postData(request, user);

            List<ContractMemo> contractMemos = new ArrayList<>();
            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("row");

            for (int index = 0; index < nodeList.getLength(); index++) {
                Element rowElement = (Element) nodeList.item(index);
                ContractMemo memo = new ContractMemo();
                memo.setId(Utils.parseInt(rowElement.getAttribute("f0")));
                memo.setTitle(rowElement.getAttribute("f1"));
                memo.setTime(TimeUtils.parse(rowElement.getAttribute("f3"), TimeUtils.PATTERN_DDMMYYYYHHMMSS));
                memo.setUser(rowElement.getAttribute("f4"));
                memo.setText(getMemo(contractId, memo.getId()).getText());

                contractMemos.add(memo);
            }

            return contractMemos;
        }
    }

    public ContractMemo getMemo(int contractId, int memoId) {
        if (dbInfo.versionCompare("8.0") > 0) {
            ContractMemo8x contractMemo = getContractMemo8x(memoId);
            DirectoryDAO directoryDAO = new DirectoryDAO(user, dbInfo);
            Map<Integer, UserInfo> users = directoryDAO.getUsersInfo();
            ContractMemo item = contractMemo.convertToContractMemo();
            UserInfo userInfo = users.get(contractMemo.getUserId());
            if (userInfo != null) {
                item.setUser(userInfo.getName());
            }
            return item;
        } else {
            ContractMemo memo = null;

            Request request = new Request();
            request.setModule("contract");
            request.setAction("GetContractMemo");
            request.setContractId(contractId);
            request.setAttribute("id", memoId);

            Document doc = transferData.postData(request, user);

            Element commentEl = XMLUtils.selectElement(doc, "/data/comment");
            if (commentEl != null) {
                memo = new ContractMemo();

                memo.setTitle(commentEl.getAttribute("subject"));
                memo.setText(linesToString(commentEl));
                memo.setVisibleForUser(Utils.parseBoolean(commentEl.getAttribute("visibled")));
            }

            return memo;
        }
    }

    private ContractMemo8x getContractMemo8x(int memoId) {
        RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractNoteService", "getContractNote");
        req.setParam("contractNoteId", memoId);
        JsonNode res = transferData.postDataReturn(req, user);
        return jsonMapper.convertValue(res, ContractMemo8x.class);
    }

    public void updateMemo(int contractId, int memoId, String memoTitle, String memoText) {
        updateMemo(contractId, memoId, memoTitle, memoText, false);
    }

    public void updateMemo(int contractId, int memoId, String memoTitle, String memoText, boolean visible) {
        if (dbInfo.versionCompare("8.0") > 0) {
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
        } else {
            Request request = new Request();
            request.setModule("contract");
            request.setAction("UpdateContractMemo");
            request.setContractId(contractId);
            request.setAttribute("subject", memoTitle);
            request.setAttribute("comment", memoText);
            request.setAttribute("visibled", visible);
            if (memoId == 0) {
                request.setAttribute("id", "new");
            } else {
                request.setAttribute("id", memoId);
            }

            transferData.postData(request, user);
        }
    }

    public void deleteMemo(int contractId, int memoId) {
        if (dbInfo.versionCompare("8.0") > 0) {
            //contractId почемуто потерялся!
            ContractMemo8x memo = getContractMemo8x(memoId);
            if (memo != null) {
                RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractNoteService", "contractNoteDelete");
                req.setParamContractId(memo.getContractId());
                req.setParam("id", memoId);
                transferData.postDataReturn(req, user);
            }
        } else {
            Request request = new Request();
            request.setModule("contract");
            request.setAction("DeleteContractMemo");
            request.setContractId(contractId);
            request.setAttribute("id", memoId);

            transferData.postData(request, user);
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
