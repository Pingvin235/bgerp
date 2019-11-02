package ru.bgcrm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.model.BGMessageException;

public class PermissionNode {
    private static final Logger log = Logger.getLogger(PermissionNode.class);

    private static String DIRECTORY = "plugin/action";
    private static String DELIMETER = " -> ";

    private int id = -1;
    private String title;
    private String titlePath;
    private String action;
    private List<String> actionList = Collections.emptyList();
    private String description;
    private boolean allowAll = false;
    private boolean notLogging = false;
    private List<PermissionNode> childs;

    public PermissionNode() {
        childs = new ArrayList<PermissionNode>();
    }

    public void addChild(PermissionNode child) {
        childs.add(child);
    }

    public List<PermissionNode> getChilds() {
        return childs;
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
        childs.remove(node);
    }

    public int getId() {
        return id;
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

    private static int nodeId = 0;

    private static void buildTree(Node element, PermissionNode parentNode) {
        NamedNodeMap attrs = element.getAttributes();
        String title = attrs.getNamedItem("title") != null ? attrs.getNamedItem("title").getNodeValue() : null;
        String action = attrs.getNamedItem("action") != null ? attrs.getNamedItem("action").getNodeValue() : null;
        boolean allowAll = attrs.getNamedItem("allowAll") != null;
        boolean notLogging = attrs.getNamedItem("notLogging") != null;

        PermissionNode node = new PermissionNode();
        parentNode.addChild(node);

        node.id = nodeId++;
        node.setTitle(title);
        if (Utils.notBlankString(parentNode.getTitle())) {
            node.setTitlePath(parentNode.getTitlePath() + DELIMETER + title);
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

    public static List<PermissionNode> getPermissionTrees() throws BGMessageException {
        nodeId = 0;

        List<String> actionFiles = Arrays.asList(new File(DIRECTORY).list());

        // сортировка, чтобы kernel.xml оказался первым
        Collections.sort(actionFiles, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1.startsWith("kernel")) {
                    return -1;
                }
                if (o2.startsWith("kernel")) {
                    return 1;
                }
                return o1.compareTo(o2);
            }
        });

        List<PermissionNode> permissionNodes = new ArrayList<PermissionNode>();
        for (String actionFile : actionFiles) {
            if (actionFile.endsWith(".xml")) {
                permissionNodes.add(getPermissionTree(DIRECTORY + "/" + actionFile));
            }
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

    private static PermissionNode getPermissionTree(String filename) throws BGMessageException {
        PermissionNode rootPermissionNode = new PermissionNode();

        try {
            Document doc = XMLUtils.parseDocument(new InputSource(new FileInputStream(filename)));
            Element element = doc.getDocumentElement();

            buildTree(element, rootPermissionNode);
        } catch (NullPointerException ex) {
            throw new BGMessageException("Неверный формат файла " + filename);
        } catch (FileNotFoundException ex) {
            log.error(ex.getMessage(), ex);
        }

        return rootPermissionNode;
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