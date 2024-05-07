package ru.bgcrm.model.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.model.process.wizard.Step;

public class Wizard {
    // шаги, которые необходимо заполнить при создании процесса
    private final List<Step> createStepList = new ArrayList<>();

    // шаги, открываемы при доступе к карточке процесса
    private final List<Step> stepList = new ArrayList<>();

    public Wizard(TypeProperties props) {
        ConfigMap configMap = props.getConfigMap();
        ConfigMap wizardConf = configMap.subSok("wizard.", "createWizard.");

        loadSteps(wizardConf, "createStep.", createStepList);
        loadSteps(wizardConf, "step.", stepList);
    }

    protected void loadSteps(ConfigMap wizardConf, String prefix, List<Step> stepList) {
        for (Map.Entry<Integer, ConfigMap> me : wizardConf.subIndexed(prefix).entrySet()) {
            ConfigMap config = me.getValue();

            String className = config.get("class");

            Step step = Step.newInstance(className, config);
            if (step != null) {
                stepList.add(step);
            }
        }
    }

    public List<Step> getCreateStepList() {
        return createStepList;
    }

    public List<Step> getStepList() {
        return stepList;
    }
}