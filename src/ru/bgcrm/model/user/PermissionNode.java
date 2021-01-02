package ru.bgcrm.model.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.plugin.Plugin;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class PermissionNode {
    @VisibleForTesting
    protected static String FILE_NAME = "action.xml";
    private static String DELIMITER = " -> ";

    private String title;
    private String titlePath;
    private String action;
    private List<String> actionList = Collections.emptyList();
    private String description;
    private boolean allowAll = false;
    private boolean notLogging = false;
    private List<PermissionNode> children;

    public PermissionNode() {
        children = new ArrayList<PermissionNode>();
    }

    public void addChild(PermissionNode child) {
        children.add(child);
    }

    @Deprecated
    public List<PermissionNode> getChilds() {
        return children;
    }

    public List<PermissionNode> getChildren() {
        return children;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return (title != null) ? title : action;
    }

    public String getTitlePath() {
        return titlePath;
    }

    public void setTitlePath(String titlePath) {
        this.titlePath = titlePath;
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

    public void setAllowAll(boolean allowAll) {
        this.allowAll = allowAll;
    }

    @VisibleForTesting
    protected static void buildTree(Node element, PermissionNode parentNode) {
        NamedNodeMap attrs = element.getAttributes();
        String title = attrs.getNamedItem("title") != null ? attrs.getNamedItem("title").getNodeValue() : null;
        String action = attrs.getNamedItem("action") != null ? attrs.getNamedItem("action").getNodeValue() : null;
        boolean allowAll = attrs.getNamedItem("allowAll") != null;
        boolean notLogging = attrs.getNamedItem("notLogging") != null;

        PermissionNode node = new PermissionNode();
        parentNode.addChild(node);

        node.setTitle(title);
        if (Utils.notBlankString(parentNode.getTitle())) {
            node.setTitlePath(parentNode.getTitlePath() + DELIMITER + title);
        } else {
            node.setTitlePath(title);
        }
        node.setAction(action);
        node.setAllowAll(allowAll);
        node.setNotLogging(notLogging);
        if (Utils.notEmptyString(action)) {
            node.setDescription(XMLUtils.getElementText(element));
            return;
        }

        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                buildTree(nodeList.item(i), node);
            }
        }
    }

    public static List<PermissionNode> getPermissionTrees() {
        List<PermissionNode> permissionNodes = new ArrayList<>();

       for (Plugin p : PluginManager.getInstance().getPluginList()) {
            Document doc = p.getXml(FILE_NAME, null);
            if (doc == null) continue;

            var node = new PermissionNode();
            buildTree(doc.getDocumentElement(), node);
            permissionNodes.add(node);
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
            node = findPermissionNode(treeNode, action);
            if (node != null) {
                return node;
            }
        }
        return null;
    }

    private static PermissionNode findPermissionNode(PermissionNode node, String action) {
        if (action.equals(node.getAction()) || node.getActionList().contains(action)) {
            return node;
        }

        for (PermissionNode child : node.getChilds()) {
            PermissionNode permNode = findPermissionNode(child, action);
            if (permNode != null) {
                return permNode;
            }
        }

        return null;
    }

    public boolean isNotLogging() {
        return notLogging;
    }

    public void setNotLogging(boolean notLogging) {
        this.notLogging = notLogging;
    }
}