package ru.bgcrm.model.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.model.process.wizard.base.Step;

public class Wizard {
    // steps that need to be filled in when creating a process
    private final List<Step> createStepList = new ArrayList<>();

    // steps opened when accessing the process card
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