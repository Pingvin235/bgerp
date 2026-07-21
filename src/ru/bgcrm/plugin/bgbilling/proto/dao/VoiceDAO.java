package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.model.base.IdTitle;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.dao.directory.VoiceAccountTypeDirectory;
import ru.bgcrm.plugin.bgbilling.proto.model.voice.PhoneResource;
import ru.bgcrm.plugin.bgbilling.proto.model.voice.PhoneCategory;
import ru.bgcrm.plugin.bgbilling.proto.model.voice.VoiceAccount;
import ru.bgcrm.plugin.bgbilling.proto.model.voice.VoiceAccountType;
import ru.bgcrm.plugin.bgbilling.proto.model.voice.VoiceDevice;

public class VoiceDAO extends BillingModuleDAO {
    private static final String MODULES_VOICE = "ru.bitel.bgbilling.modules.voice";
    private static final String INVENTORY_RESOURCE = "ru.bitel.oss.systems.inventory.resource";

    public VoiceDAO(User user, String billingId, int moduleId) {
        super(user, billingId, moduleId);
    }

    public VoiceDAO(User user, DBInfo dbInfo, int moduleId) {
        super(user, dbInfo, moduleId);
    }

    public List<VoiceAccount> getAccounts(int contractId) {
        var req = new RequestJsonRpc(MODULES_VOICE, moduleId, "VoiceAccountService", "voiceAccountList");
        req.setParamContractId(contractId);

        var dir = dbInfo.directory(VoiceAccountTypeDirectory.class, moduleId);

        List<VoiceAccount> result = readJsonValue(transferData.postDataReturn(req, user).traverse(),
                jsonTypeFactory.constructCollectionType(List.class, VoiceAccount.class));
        for (var account : result)
            account.setTypeTitle(dir.get(user, account.getTypeId()).getTitle());

        return result;
    }

    public VoiceAccount getAccount(int id) {
        var req = new RequestJsonRpc(MODULES_VOICE, moduleId, "VoiceAccountService", "voiceAccountGet");
        req.setParam("accountId", id);

        return jsonMapper.convertValue(transferData.postDataReturn(req, user), VoiceAccount.class);
    }

    public void updateAccount(VoiceAccount account) {
        var req = new RequestJsonRpc(MODULES_VOICE, moduleId, "VoiceAccountService", "voiceAccountUpdate");
        req.setParam("account", account);

        account.setId(jsonMapper.convertValue(transferData.postDataReturn(req, user), int.class));
    }

    public void generateAccountPassword(int id) {
        var req = new RequestJsonRpc(MODULES_VOICE, moduleId, "VoiceAccountService", "voiceAccountPasswordGenerate");
        req.setParam("accountId", id);

        transferData.postData(req, user);
    }

    public void deleteAccount(int id) {
        var req = new RequestJsonRpc(MODULES_VOICE, moduleId, "VoiceAccountService", "voiceAccountDelete");
        req.setParam("accountId", id);

        transferData.postData(req, user);
    }

    public List<VoiceAccountType> getAccountTypes() {
        var req = new RequestJsonRpc(MODULES_VOICE, moduleId, "VoiceAccountService", "voiceAccountTypeList");
        return readJsonValue(transferData.postDataReturn(req, user).traverse(), jsonTypeFactory.constructCollectionType(List.class, VoiceAccountType.class));
    }

    public List<IdTitle> getDeviceTypes() {
        var req = new RequestJsonRpc(MODULES_VOICE, moduleId, "VoiceDeviceService", "deviceTypeTitleList");
        return readJsonValue(transferData.postDataReturn(req, user).traverse(), jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));
    }

    public VoiceDevice getDevice(int deviceId) {
        RequestJsonRpc req = new RequestJsonRpc(MODULES_VOICE, moduleId, "VoiceDeviceService", "deviceGet");
        req.setParam("id", deviceId);

        return jsonMapper.convertValue(transferData.postDataReturn(req, user), VoiceDevice.class);
    }

    public VoiceDevice getDeviceRoot() {
        RequestJsonRpc req = new RequestJsonRpc(MODULES_VOICE, moduleId, "VoiceDeviceService", "deviceRoot");
        req.setParam("deviceTreeFilter", Map.of("loadDeviceGroupLink", "false"));

        return jsonMapper.convertValue(transferData.postDataReturn(req, user), VoiceDevice.class);
    }

    public PhoneCategory getCategoryRoot() {
        RequestJsonRpc req = new RequestJsonRpc(INVENTORY_RESOURCE, moduleId, "PhoneResourceService", "phoneCategoryRoot");

        return jsonMapper.convertValue(transferData.postDataReturn(req, user), PhoneCategory.class);
    }

    public List<PhoneResource> getResources(int categoryId) {
        RequestJsonRpc req = new RequestJsonRpc(INVENTORY_RESOURCE, moduleId, "PhoneResourceService", "phoneResourceList");
        req.setParam("phoneCategoryId", categoryId);

        return readJsonValue(transferData.postDataReturn(req, user).traverse(), jsonTypeFactory.constructCollectionType(List.class, PhoneResource.class));
    }

    public List<Long> getFreeList(int categoryId, int resourceId) {
        RequestJsonRpc req = new RequestJsonRpc(INVENTORY_RESOURCE, moduleId, "PhoneResourceService", "phoneFreeList");
        req.setParam("categoryId", categoryId);
        req.setParam("resourceIds", Set.of(resourceId));
        req.setParam("count", 100);

        return readJsonValue(transferData.postDataReturn(req, user).traverse(), jsonTypeFactory.constructCollectionType(List.class, Long.class));
    }
}
