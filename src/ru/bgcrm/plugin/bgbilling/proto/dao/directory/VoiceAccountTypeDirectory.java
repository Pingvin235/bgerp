package ru.bgcrm.plugin.bgbilling.proto.dao.directory;

import java.util.List;
import java.util.stream.Collectors;

import org.bgerp.model.base.IdTitle;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.proto.dao.VoiceDAO;

public class VoiceAccountTypeDirectory extends Directory<IdTitle> {
    public VoiceAccountTypeDirectory(DBInfo dbInfo, int moduleId) {
        super(dbInfo, moduleId, "ru.bitel.bgbilling.modules.voice.common.bean.VoiceAccountType");
    }

    @Override
    protected List<IdTitle> list(User user) {
        return new VoiceDAO(user, dbInfo.getId(), moduleId).getAccountTypes().stream()
            .map(item -> new IdTitle(item.getId(), item.getTitle()))
            .collect(Collectors.toList());
    }

    @Override
    protected IdTitle missingValue(int id) {
        return new IdTitle(id, IdTitle.unknown(id));
    }
}
