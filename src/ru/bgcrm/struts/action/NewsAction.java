package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.cache.UserNewsCache;
import ru.bgcrm.dao.NewsDAO;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.News;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;

public class NewsAction extends BaseAction {
    public static final String UNREAD_NEWS_PERSONAL_KEY = "unreadNews";

    public ActionForward newsUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        int requestUserId = form.getParamInt("requestUserId", 0);
        String kindOf = requestUserId > 0 ? "сообщение" : "новость";

        // 0 - обычная, 1 - всплывающая
        int type = form.getParamInt("type");
        if (type < 0) {
            throw new BGMessageException("Не выбран тип новости");
        }

        News news = new News();
        news.setId(form.getId());
        news.setUserId(form.getUserId());
        news.setTitle(form.getParam("title", ""));
        news.setDescription(form.getParam("description", ""));
        news.setPopup(type == 1);
        news.setLifeTime(form.getParamInt("lifeTime", 30));
        news.setReadTime(form.getParamInt("readTime", 24));

        if (Utils.isBlankString(news.getTitle())) {
            throw new BGMessageException("Нельзя добавить " + kindOf + " без заголовка");
        }

        if (Utils.isBlankString(news.getDescription())) {
            throw new BGMessageException("Нельзя добавить " + kindOf + " без текста");
        }

        Set<Integer> groups = form.getSelectedValues("group");

        ParameterMap perm = form.getPermission();

        Set<Integer> allowedGroups = Utils.toIntegerSet(perm.get("allowedGroupIds", ""));

        if (requestUserId == 0) {
            if (groups.size() > 0) {
                if (allowedGroups.size() > 0 && !CollectionUtils.containsAny(groups, allowedGroups)) {
                    throw new BGMessageException("У вас нет прав добавлять новости для выбранных групп");
                }

                new NewsDAO(con).updateNewsGroups(news, groups);
            } else {
                if (allowedGroups.size() > 0) {
                    throw new BGMessageException("Вы не можете добавлять новости для всех пользователей");
                }

                new NewsDAO(con).updateNewsUsers(news, Utils.getObjectIdsSet(UserCache.getActiveUsers()));
            }
        } else {
            Set<Integer> userSet = new HashSet<Integer>();
            userSet.add(requestUserId);

            new NewsDAO(con).updateNewsUsers(news, userSet);
        }

        return json(con, form);
    }

    public ActionForward newsEdit(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        News news = new NewsDAO(con).getNews(form.getId());
        if (news != null) {
            form.getResponse().setData("news", news);
        }

        return html(con, mapping, form, "update");
    }

    public ActionForward newsList(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        Boolean read = form.getParamBoolean("read", null);

        SearchResult<News> searchResult = new SearchResult<News>(form);

        new NewsDAO(con).searchNewsList(searchResult, form.getUserId(), read, form.getParam("text"));

        if (read != null && !read) {
            User user = form.getUser();

            Preferences persMap = user.getPersonalizationMap();
            String configBefore = persMap.getDataString();

            String newsIds = Utils.getObjectIds(searchResult.getList());
            persMap.put(UNREAD_NEWS_PERSONAL_KEY, newsIds);

            new UserDAO(con).updatePersonalization(configBefore, user);
        }

        return html(con, mapping, form, "list");
    }

    public ActionForward newsGet(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        NewsDAO newsDAO = new NewsDAO(con);
        newsDAO.setNewsRead(form.getParamInt("newsId", -1), form.getUserId(), true);
        News news = newsDAO.getNews(form.getParamInt("newsId", -1));

        form.getResponse().setData("item", news);

        UserNewsCache.flushCache(con, Collections.singleton(form.getUserId()));

        return html(con, mapping, form, "newsBody");
    }

    public ActionForward newsSetRead(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        new NewsDAO(con).setNewsRead(form.getParamInt("newsId", -1), form.getUserId(),
                form.getParamBoolean("value", true));

        UserNewsCache.flushCache(con, Collections.singleton(form.getUserId()));

        return json(con, form);
    }

    public ActionForward newsSetAllRead(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        new NewsDAO(con).setNewsAllRead(form.getUserId());

        UserNewsCache.flushCache(con, Collections.singleton(form.getUserId()));

        return json(con, form);
    }

    public ActionForward newsDelete(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        new NewsDAO(con).deleteNews(form.getId());
        
        return json(con, form);
    }
}