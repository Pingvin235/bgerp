package ru.bgcrm.model.process.wizard;

import java.sql.Connection;
import java.util.List;
import java.util.SortedMap;

import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.struts.form.DynActionForm;

public class AddressInfoStepData extends StepData<AddressInfoStep> {
    private int houseId;

    public AddressInfoStepData(AddressInfoStep step, WizardData data) {
        super(step, data);
    }

    @Override
    public boolean isFilled(DynActionForm form, Connection con) throws BGException {
        List<StepData<?>> stepDataList = data.getStepDataList();

        // находим первый предшествующий шаг заполнения параметров
        // TODO: может в последствии сделать первый предшествующий с нужным параметром
        for (int i = stepDataList.indexOf(this); i >= 0; i--) {
            StepData<?> stepData = stepDataList.get(i);

            if (stepData instanceof FillParamsStepData) {
                FillParamsStepData fpStepData = (FillParamsStepData) stepData;

                SortedMap<Integer, ParameterAddressValue> values = new ParamValueDAO(con).getParamAddress(fpStepData.getObjectId(),
                        step.getAddressParamId());
                if (values.size() > 0) {
                    houseId = values.values().iterator().next().getHouseId();
                }

                break;
            }
        }

        return true;
    }

    public int getHouseId() {
        return houseId;
    }
}