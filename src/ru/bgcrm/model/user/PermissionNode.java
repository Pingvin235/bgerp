package ru.bgcrm.model.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.action.TitledAction;
import org.bgerp.action.TitledActionFactory;
import org.bgerp.l10n.Localization;
import org.bgerp.l10n.Localizer;
import org.bgerp.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.plugin.Plugin;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

/**
 * Permissions tree node, allowing one or more actions.
 * Each action is a semicolon separated string with action class and method names.
 * For example {@code ru.bgcrm.struts.action.admin.AppAction:status}.
 * A node can contains many actions in case of name deprecations,
 * or when all of them have to be handled together.
 * All the node actions are allowed when the node is allowed.
 *
 * @author Shamil Vakhitov
 */
public class PermissionNode {
    private static final Log log = Log.getLog();

    /** Root tree nodes. */
    private static volatile List<PermissionNode> permissionTrees;

    @VisibleForTesting
    static String FILE_NAME = "action.xml";
    private static final String DELIMITER = " / ";

    private static final boolean VALIDATE_ACTION_METHOD = true;
    /* Can be enabled later for statically checking everything on start. */
    private static final boolean VALIDATE_ACTION_METHOD_SIGNATURE = false;

    private String title;
    private String titlePath;
    /** Semicolon separated action class and method names. */
    private String action;
    private List<String> actions = new ArrayList<>();
    private String description = "";
    private boolean allowAll;
    private boolean notLogging;
    private List<PermissionNode> children = new ArrayList<>();

    /**
     * Simplified constructor, no children supported.
     * @param action
     * @param title
     */
    private PermissionNode(String action, String title) {
        setAction(action);
        this.title = title;
    }

    @VisibleForTesting
    PermissionNode(PermissionNode parent, Localizer l, Element node) {
        this(node.getAttribute("action"), node.getAttribute("title"));

        var ltitle = node.getAttribute("ltitle");
        if (Utils.notBlankString(ltitle)) {
            title = l.l(ltitle);
        }

        allowAll = Utils.parseBoolean(node.getAttribute("allowAll"));
        notLogging = Utils.parseBoolean(node.getAttribute("notLogging"));

        if (parent != null && Utils.notBlankString(parent.getTitle())) {
            titlePath = parent.getTitlePath() + DELIMITER + title;
        } else {
            titlePath = title;
        }

        loadChildren(l, node);

        if (Utils.notEmptyString(action) && children.isEmpty()) {
            description = XMLUtils.getElementText(node);
        }

        validateAction();
    }

    /**
     * Validates existence of primary action class and method in {@link #action}.
     */
    private void validateAction() {
        if (Utils.isBlankString(action))
            return;

        Class<?> actionClass = null;
        try {
            actionClass = Class.forName(StringUtils.substringBefore(action, ":"));
            if (!BaseAction.class.isAssignableFrom(actionClass)) {
                log.warn("Action class '{}' doesn't extend BaseAction", actionClass.getName());
                return;
            }
        } catch (ClassNotFoundException e) {
            log.warn("Action class not found for action '{}'", action);
            return;
        }

        validateActionMethod(actionClass);
    }

    /**
     * Validates existence of action method in {@link #action}.
     * The finings are logged with WARN level.
     * @param actionClass action class.
     */
    private void validateActionMethod(Class<?> actionClass) {
        if (!VALIDATE_ACTION_METHOD)
            return;

        String name = actionMethodName();

        boolean exists = false;
        try {
            exists = Arrays
                .stream(actionClass.getMethods())
                .filter(m -> name.equals(m.getName()))
                .findAny()
                .isPresent();
        } catch (SecurityException e) {
            log.error(e);
        }

        if (!exists)
            log.warn("Action method not found for action '{}'", action);
        else
            validateActionMethodSignature(actionClass, name);
    }

    /**
     * Validates action method signature.
     * The finings are logged with WARN level.
     * @param actionClass action class.
     * @param actionMethod method name.
     */
    private void validateActionMethodSignature(Class<?> actionClass, String actionMethod) {
        if (!VALIDATE_ACTION_METHOD_SIGNATURE)
            return;

        boolean found = false;
        try {
            actionClass.getMethod(actionMethod, BaseAction.TYPES_CONSET_DYNFORM);
            found = true;
        } catch (NoSuchMethodException e) {}

        try {
            actionClass.getMethod(actionMethod, BaseAction.TYPES_CON_DYNFORM);
            found = true;
        } catch (NoSuchMethodException e) {}

        if (!found)
            log.warn("Deprecated signature of action method '{}' in class '{}'", actionMethod, actionClass.getName());
    }

    private String actionMethodName() {
        String result = StringUtils.substringAfter(action, ":");
        if ("null".equals(result))
            result = "unspecified";
        return result;
    }

    private void loadChildren(Localizer l, Element node) {
        var actionFactory = node.getAttribute("actionFactory");
        if (Utils.notBlankString(actionFactory)) {
            for (TitledAction action : TitledActionFactory.create(actionFactory, l))
                children.add(new PermissionNode(action.getAction(), action.getTitle(l)));
        } else {
            for (Element child : XMLUtils.selectElements(node, "item")) {
                children.add(new PermissionNode(this, l, child));
            }
        }
    }

    /**
     * @return children nodes.
     */
    public List<PermissionNode> getChildren() {
        return children;
    }

    /**
     * @return node title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return slash separated titles path to the node.
     */
    public String getTitlePath() {
        return titlePath;
    }

    /**
     * @return the primary node action, identifies that the node is selected in UI.
     */
    public String getAction() {
        return action;
    }

    /**
     * @return node actions.
     */
    public List<String> getActions() {
        return actions;
    }

    /**
     * Sets primary and other actions from comma separated string.
     * @param action comma separated string.
     */
    public void setAction(String action) {
        List<String> actionList = Utils.toList(action, ";,");
        if (actionList.size() > 1) {
            this.action = actionList.get(0);
            this.actions = actionList;
        } else {
            this.action = action;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAllowAll() {
        return allowAll;
    }

    public boolean isNotLogging() {
        return notLogging;
    }

    /**
     * List with root permission nodes for kernel and other enabled plugins.
     * @return
     */
    public static List<PermissionNode> getPermissionTrees() {
        if (permissionTrees != null)
            return permissionTrees;

        // avoid parallel loading
        synchronized (log) {
            if (permissionTrees != null)
                return permissionTrees;

            var newPermissionTrees = new ArrayList<PermissionNode>(20);

            for (Plugin p : PluginManager.getInstance().getPluginList()) {
                Document doc = p.getXml(FILE_NAME, null);
                if (doc == null)
                    continue;

                newPermissionTrees.add(new PermissionNode(null,
                        Localization.getLocalizer(p.getId(), Localization.getSysLang()),
                        doc.getDocumentElement()));
            }

            return permissionTrees = Collections.unmodifiableList(newPermissionTrees);
        }
    }

    /**
     * Gets map with primary actions as keys.
     * @param permMap map with any action as key.
     * @return
     */
    public static Map<String, ParameterMap> primaryActions(Map<String, ParameterMap> permMap) {
        Map<String, ParameterMap> result = new HashMap<>(permMap.size());

        for (Map.Entry<String, ParameterMap> perm : permMap.entrySet()) {
            String action = perm.getKey();

            PermissionNode node = PermissionNode.getPermissionNode(action);
            if (node == null) {
                log.warn("Not found action node: {}", action);
                continue;
            }

            result.put(node.getAction(), perm.getValue());
        }

        return result;
    }

    /**
     * Finds permission node by action.
     * @param action semicolon separated class and method names.
     * @return
     */
    public static PermissionNode getPermissionNode(String action) {
        PermissionNode node = null;
        for (PermissionNode treeNode : getPermissionTrees()) {
            node = treeNode.findPermissionNode(action);
            if (node != null) {
                return node;
            }
        }
        return null;
    }

    PermissionNode findPermissionNode(String action) {
        PermissionNode node = this;
        if (action.equals(node.getAction()) || node.getActions().contains(action)) {
            return node;
        }

        for (PermissionNode child : node.children) {
            PermissionNode permNode = child.findPermissionNode(action);
            if (permNode != null) {
                return permNode;
            }
        }

        return null;
    }
}