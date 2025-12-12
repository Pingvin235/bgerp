package org.bgerp.model.process;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.action.ProcessAction;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.model.base.tree.TreeItem;
import org.bgerp.model.process.config.ProcessCreateInConfig;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Process creation type
 *
 * @author Shamil Vakhitov
 */
public class ProcessCreateType extends TreeItem<Integer, ProcessCreateType>  {
    private static final Log log = Log.getLog();

    /**
     * Recursive copy of tree of process types, allowed for creation, filtered by 'create.in' configuration and process isolation
     * @param form current request form user and permission
     * @param area creation area
     * @param ids optional additional restricting process IDs
     * @return
     */
    public static ProcessCreateType treeRoot(DynActionForm form, String area, Set<Integer> ids) {
        var types = ProcessAction.processCreateTypes(form, area, ids);
        Set<Integer> typeIds = types.stream().map(ProcessCreateType::getId).collect(Collectors.toSet());
        log.debug("treeRoot area: {}, ids: {}, typeIds: {}", area, ids, typeIds);
        return new ProcessCreateType(ProcessTypeCache.getTypeTreeRoot(), area).children(typeIds);
    }

    private final ProcessType type;
    private final String area;
    private final ProcessCreateInConfig config;

    public ProcessCreateType(ProcessType type, String area) {
        setId(type.getId());
        setTitle(type.getTitle());
        this.type = type;
        this.area = area;
        this.config = type.getProperties() == null ? null : type.getProperties().getConfigMap().getConfig(ProcessCreateInConfig.class);
    }

    public ProcessType getType() {
        return type;
    }

    /**
     * @return {@code treeItemSelected} if the item or any of children is selected
     */
    @Dynamic
    public String getSelectedClass() {
        final String className = "treeItemSelected";

        if (config != null && config.selected(area))
            return className;

        for (var child : children)
            if (className.equals(child.getSelectedClass()))
                return className;

        return "";
    }

    public boolean check() {
        return config.check(area);
    }

    public String getCopyParams() {
        return config.getCopyParams();
    }

    public boolean openCreated() {
        return config.openCreated(area);
    }

    @Override
    public String toString() {
        return String.valueOf(type.getId());
    }

    @Override
    protected boolean isRootNode() {
        throw new UnsupportedOperationException("Unimplemented method 'isRootNode'");
    }

    private ProcessCreateType children(Set<Integer> ids) {
        var children = new ArrayList<ProcessCreateType>(type.getChildren().size());

        for (var childType : type.getChildren()) {
            var child = new ProcessCreateType(childType, area);
            if (childType.isInPathTo(ids))
                children.add(child.children(ids));
        }

        setChildren(children);

        return this;
    }
}