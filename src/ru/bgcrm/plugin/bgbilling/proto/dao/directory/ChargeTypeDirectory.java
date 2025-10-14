package ru.bgcrm.plugin.bgbilling.proto.dao.directory;

import java.util.List;

import org.bgerp.model.base.IdTitle;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.proto.dao.DirectoryDAO;

public class ChargeTypeDirectory extends Directory<IdTitle> {
    public ChargeTypeDirectory(DBInfo dbInfo, int moduleId) {
        super(dbInfo, moduleId, "ru.bitel.bgbilling.kernel.contract.balance.common.bean.ChargeType");
    }

    @Override
    protected List<IdTitle> list(User user) {
        return new DirectoryDAO(user, dbInfo).chargeTypeList();
    }

    @Override
    protected IdTitle missingValue(int id) {
        return new IdTitle(id, IdTitle.unknown(id));
    }
}
