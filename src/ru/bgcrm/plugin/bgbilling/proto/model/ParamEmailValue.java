package ru.bgcrm.plugin.bgbilling.proto.model;

import java.util.List;

import org.bgerp.model.base.tree.IdStringTitleTreeItem;

import ru.bgcrm.plugin.bgbilling.proto.model.entity.EntityAttrEmail;
import ru.bgcrm.util.Utils;

public class ParamEmailValue {
    private List<String> emails; // Емейлы
    private int eid; //id списка рассылок о_О
    private List<String> subscrs; // Активированные подписки
    private List<IdStringTitleTreeItem> subscrsTree; // Список существующих рассылок

    public EntityAttrEmail getEntityAttrEmail() {
        return entityAttrEmail;
    }

    public void setEntityAttrEmail(EntityAttrEmail entityAttrEmail) {
        this.entityAttrEmail = entityAttrEmail;
    }

    private EntityAttrEmail entityAttrEmail;

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public String getEmailsAsString() {
        return Utils.toText(emails, "\n");
    }

    public int getEid() {
        return eid;
    }

    public void setEid(int eid) {
        this.eid = eid;
    }

    public List<String> getSubscrs() {
        return subscrs;
    }

    public void setSubscrs(List<String> subscrs) {
        this.subscrs = subscrs;
    }

    public List<IdStringTitleTreeItem> getSubscrsTree() {
        return subscrsTree;
    }

    public void setSubscrsTree(List<IdStringTitleTreeItem> subscrsTree) {
        this.subscrsTree = subscrsTree;
    }
}