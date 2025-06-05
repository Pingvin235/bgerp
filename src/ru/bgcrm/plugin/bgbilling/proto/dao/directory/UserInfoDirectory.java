package ru.bgcrm.plugin.bgbilling.proto.dao.directory;

import java.util.List;

import org.bgerp.model.base.IdTitle;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.proto.dao.DirectoryDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.UserInfo;

public class UserInfoDirectory extends Directory<UserInfo> {
    public UserInfoDirectory(DBInfo dbInfo, int moduleId) {
        super(dbInfo, moduleId, "ru.bitel.bgbilling.kernel.bgsecure.common.bean.UserInfo");
    }

    @Override
    protected List<UserInfo> list(User user) {
        return new DirectoryDAO(user, dbInfo).getUserInfoList();
    }

    @Override
    protected UserInfo missingValue(int id) {
        return new UserInfo(id, IdTitle.unknown(id));
    }
}
