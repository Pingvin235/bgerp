package ru.bgcrm.model.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;

import org.bgerp.action.TitledAction;
import org.bgerp.action.TitledActionFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.plugin.Plugin;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;
import ru.bgerp.l10n.Localization;
import ru.bgerp.l10n.Localizer;

/**
 * Node of permissions tree.
 * 
 * @author Shamil Vakhitov
 */
public class PermissionNode {
    @VisibleForTesting
    static String FILE_NAME = "action.xml";
    private static String DELIMITER = " -> ";

    private String title;
    private String titlePath;
    private String action;
    private List<String> actionList = new ArrayList<>();
    private String description;
    private boolean allowAll;
    private boolean notLogging;
    private List<PermissionNode> children = new ArrayList<>();

    private PermissionNode() {}

    /**
     * Simplified constructor, no children supported.
     * 
     * @param action
     * @param title
     */
    public PermissionNode(String action, String title) {
        setAction(action);
        this.title = title;
    }

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
       
        if (Utils.notEmptyString(action)) {
            description = XMLUtils.getElementText(node);
            return;
        }

        loadChildren(l, node);
    }

    private void loadChildren(Localizer l, Element node) {
        var actionFactory = node.getAttribute("actionFactory");
        if (Utils.notBlankString(actionFactory)) {
            for (TitledAction action : TitledActionFactory.create(actionFactory, l))
                addChild(new PermissionNode(action.getAction(), action.getTitle(l)));
        } else {
            for (Element child : XMLUtils.elements(node.getChildNodes())) {
                addChild(new PermissionNode(this, l, child));
            }
        }
    }

    void addChild(PermissionNode child) {
        children.add(child);
    }

    public List<PermissionNode> getChildren() {
        return children;
    }

    public String getTitle() {
        return title;
    }

    public String getTitlePath() {
        return titlePath;
    }

    public void removeChild(PermissionNode node) {
        children.remove(node);
    }

    public String getAction() {
        return action;
    }

    public List<String> getActionList() {
        return actionList;
    }

    public void setAction(String action) {
        List<String> actionSet = Utils.toList(action, ";,");
        if (actionSet.size() > 1) {
            this.action = actionSet.get(0);
            this.actionList = actionSet;
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

    public static List<PermissionNode> getPermissionTrees() {
        List<PermissionNode> permissionNodes = new ArrayList<>();

       for (Plugin p : PluginManager.getInstance().getPluginList()) {
            Document doc = p.getXml(FILE_NAME, null);
            if (doc == null) continue;

            var emptyParent = new PermissionNode();
            emptyParent.addChild(new PermissionNode(null,
                Localization.getLocalizer(p.getId(), Localization.getSysLang()),
                doc.getDocumentElement()));
            permissionNodes.add(emptyParent);
        }
       
        return permissionNodes;
    }

    public static Map<String, ParameterMap> addPermissionsSynonyms(Map<String, ParameterMap> permMap) {
        Map<String, ParameterMap> synonymsMap = new HashMap<String, ParameterMap>();

        for (Map.Entry<String, ParameterMap> perm : permMap.entrySet()) {
            String action = perm.getKey();

            PermissionNode node = PermissionNode.getPermissionNode(action);
            if (node != null) {
                for (String alterName : node.getActionList()) {
                    if (!alterName.equals(action)) {
                        synonymsMap.put(alterName, perm.getValue());
                    }
                }
            }
        }

        permMap.putAll(synonymsMap);

        return permMap;
    }

    public static PermissionNode getPermissionNode(String action) {
        PermissionNode node = null;
        for (PermissionNode treeNode : UserCache.getAllPermTree()) {
            node = treeNode.findPermissionNode(action);
            if (node != null) {
                return node;
            }
        }
        return null;
    }

    PermissionNode findPermissionNode(String action) {
        PermissionNode node = this;
        if (action.equals(node.getAction()) || node.getActionList().contains(action)) {
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