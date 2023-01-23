package ru.bgcrm.plugin.bgbilling.proto.dao.version.v8x;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractStatusDAO;

import java.util.Collections;
import java.util.Date;

public class ContractStatusDAO8x extends ContractStatusDAO {
    public ContractStatusDAO8x(User user, String billingId) throws BGException {
        super(user, billingId);
    }

    public ContractStatusDAO8x(User user, DBInfo dbInfo) throws BGException {
        super(user, dbInfo);
    }

    public void updateStatus(int contractId, int statusId, Date dateFrom, Date dateTo, String comment)
            throws BGException {
        if (dbInfo.versionCompare("6.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(ContractDAO.KERNEL_CONTRACT_API, "ContractStatusService", "changeContractStatus");
            req.setParam("cid", Collections.singletonList(contractId));
            req.setParam("statusId", statusId);
            req.setParam("dateFrom", dateFrom);
            req.setParam("dateTo", dateTo);
            req.setParam("comment", comment);
            req.setParam("confirmChecked", true);

            transferData.postDataReturn(req, user);
        }
    }
}
