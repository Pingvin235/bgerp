package ru.bgcrm.model.process.wizard;

import java.sql.Connection;
import java.util.List;
import java.util.SortedMap;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.dao.param.ParamValueDAO;

import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.process.wizard.base.Step;
import ru.bgcrm.model.process.wizard.base.StepData;
import ru.bgcrm.model.process.wizard.base.WizardData;
import ru.bgcrm.struts.form.DynActionForm;

@Bean
public class AddressInfoStep extends Step {
    private final int addressParamId;

    public AddressInfoStep(ConfigMap config) {
        super(config);
        addressParamId = config.getInt("addressParamId", 0);
    }

    public int getAddressParamId() {
        return addressParamId;
    }

    @Override
    public String getJsp() {
        return PATH_JSP + "/step_address_info.jsp";
    }

    @Override
    public StepData<?> data(WizardData data) {
        return new AddressInfoStepData(this, data);
    }

    public static class AddressInfoStepData extends StepData<AddressInfoStep> {
        private int houseId;

        private AddressInfoStepData(AddressInfoStep step, WizardData data) {
            super(step, data);
        }

        public int getHouseId() {
            return houseId;
        }

        @Override
        public boolean isFilled(DynActionForm form, Connection con) throws Exception {
            List<StepData<?>> stepDataList = data.getStepDataList();

            // находим первый предшествующий шаг заполнения параметров
            // TODO: может в последствии сделать первый предшествующий с нужным параметром
            for (int i = stepDataList.indexOf(this); i >= 0; i--) {
                StepData<?> stepData = stepDataList.get(i);

                if (stepData instanceof FillParamsStep.Data fpStepData) {
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
    }
}
