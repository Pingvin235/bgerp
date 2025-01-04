package ru.bgcrm.model.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.action.base.Actions;
import org.bgerp.action.base.TitledAction;
import org.bgerp.action.base.TitledActionFactory;
import org.bgerp.action.util.Invoker;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.l10n.Localization;
import org.bgerp.app.l10n.Localizer;
import org.bgerp.exec.CorrectPermissions;
import org.bgerp.util.Log;
import org.bgerp.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.annotations.VisibleForTesting;

import javassist.NotFoundException;
import ru.bgcrm.plugin.Plugin;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.util.Utils;

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

    @VisibleForTesting
    static final String FILE_NAME = "action.xml";
    private static final String DELIMITER = " / ";
    public static final String ACTION_METHOD_UNSPECIFIED = "unspecified";

    /** Root tree nodes. */
    private static volatile List<PermissionNode> permissionTrees;

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
                        Localization.getLocalizer(Localization.getLang(), p.getId()),
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
    public static Map<String, ConfigMap> primaryActions(Map<String, ConfigMap> permMap) {
        Map<String, ConfigMap> result = new HashMap<>(permMap.size());

        for (Map.Entry<String, ConfigMap> perm : permMap.entrySet()) {
            String action = perm.getKey();

            PermissionNode node = PermissionNode.getPermissionNode(action);
            if (node == null) {
                log.warn("Not found action node '{}', run '{}' class to fix", action, CorrectPermissions.class.getName());
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
            if (node != null)
                return node;
        }
        return null;
    }

    /**
     * Finds permission node by action.
     * @param action semicolon separated class and method names.
     * @return not {@code null} node value.
     * @throws NotFoundException not found node.
     */
    public static PermissionNode getPermissionNodeOrThrow(String action) throws NotFoundException {
        var result = getPermissionNode(action);
        if (result == null)
            throw new NotFoundException(Log.format("Permission node not found for action '{}'", action));
        return result;
    }

    // end of static part

    private String action;
    private List<String> actions = new ArrayList<>();

    private String title;
    private String titlePath;

    private String description = "";
    private boolean allowAll;
    private boolean notLogging;

    private PermissionNode parent;
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

    PermissionNode(PermissionNode parent, Localizer l, Element node) {
        this(node.getAttribute("action"), node.getAttribute("title"));
        this.parent = parent;

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
        if (Utils.isBlankString(action)) {
            // no action is defined, directory item
            return;
        }

        var a = Actions.getById(actionId(action));
        // there is org.bgerp.action.MessageAction class not annotated and used only for permission check
        if (a != null)
            validateActionMethod(a.getTypeClass());
    }

    /**
     * Selects an action's ID (class name or path)
     * @param action primary action class and method
     * @return the class name
     */
    public static String actionId(String action) {
        int pos = action.indexOf(':');
        if (pos > 0)
            return action.substring(0, pos);
        return action;
    }

    /**
     * Validates existence of action method in {@link #action}.
     * The finings are logged with WARN level.
     * @param actionClass action class.
     */
    private void validateActionMethod(Class<?> actionClass) {
        String actionMethod = actionMethod(action);
        try {
            Invoker.find(actionClass, actionMethod);
        } catch (NoSuchMethodException e) {
            log.warnd("Missing correct action method '{}' in class '{}'", actionMethod, actionClass.getName());
        }
    }

    /**
     * Selects action method name
     * @param action primary action class and method
     * @return the method name or {@link #ACTION_METHOD_UNSPECIFIED}
     */
    public static String actionMethod(String action) {
        String result = StringUtils.substringAfter(action, ":");
        if ("null".equals(result) || Utils.isBlankString(result))
            result = ACTION_METHOD_UNSPECIFIED;
        return result;
    }

    private void loadChildren(Localizer l, Element node) {
        var actionFactory = node.getAttribute("actionFactory");
        if (Utils.notBlankString(actionFactory)) {
            for (TitledAction action : TitledActionFactory.create(actionFactory))
                children.add(new PermissionNode(action.getAction(), action.getTitle()));
        } else {
            for (Element child : XMLUtils.selectElements(node, "item")) {
                children.add(new PermissionNode(this, l, child));
            }
        }
    }

    /**
     * @return the primary node action, semicolon separated action class and method names.
     */
    public String getAction() {
        return action;
    }

    /**
     * @return list of primary and synonym actions (semicolon separated action class and method names).
     */
    public List<String> getActions() {
        return actions;
    }

    private void setAction(String action) {
        List<String> actionList = Utils.toList(action, ";,");
        if (actionList.size() > 1) {
            this.action = actionList.get(0);
            this.actions = actionList;
        } else {
            this.action = action;
        }
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

    public String getDescription() {
        return description;
    }

    public boolean isAllowAll() {
        return allowAll;
    }

    public boolean isNotLogging() {
        return notLogging;
    }

    /**
     * @return parent node;
     */
    public PermissionNode getParent() {
        return parent;
    }

    /**
     * @return children nodes.
     */
    public List<PermissionNode> getChildren() {
        return children;
    }

    @VisibleForTesting
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