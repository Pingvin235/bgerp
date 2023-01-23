package ru.bgcrm.plugin.bgbilling.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class ContractType extends IdTitle {
    private final String billingId;
    private final int patternId;
    private final List<IdTitle> tariffList;
    private final Map<Integer, String> tariffMap = new HashMap<>();
    /** -1  - тариф заменит первый существующий либо будет добавлен, если текущего нет;
     * >= 0 - тариф будет добавлен с позицией.
     */
    private final int tariffPosition;

    public ContractType(int id, ParameterMap param) {
        super(id, param.get("title"));

        this.billingId = param.get("billing");
        this.patternId = param.getInt("patternId", 0);
        this.tariffList = Utils.parseIdTitleList(param.get("tariffList", ""));
        this.tariffPosition = param.getInt("tariffPosition", -1);
        for (IdTitle it : tariffList) {
            tariffMap.put(it.getId(), it.getTitle());
        }
    }

    public String getTitle() {
        return title;
    }

    public String getBillingId() {
        return billingId;
    }

    public int getPatternId() {
        return patternId;
    }

    public List<IdTitle> getTariffList() {
        return tariffList;
    }

    public Map<Integer, String> getTariffMap() {
        return tariffMap;
    }

    public int getTariffPosition() {
        return tariffPosition;
    }
}