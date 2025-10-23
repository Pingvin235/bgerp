package org.bgerp.action;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.cache.UserCache;
import org.bgerp.cache.UserNewsCache;
import org.bgerp.model.Pageable;

import ru.bgcrm.dao.NewsDAO;
import ru.bgcrm.model.News;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

@Action(path = "/user/news", pathId = true)
public class NewsAction extends BaseAction {
    private static final String PATH_JSP = PATH_JSP_USER + "/news";

    public static final String UNREAD_NEWS_PERSONAL_KEY = "unreadNews";

    public ActionForward newsUpdate(DynActionForm form, Connection con) throws Exception {
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
        news.setText(form.getParam("description", ""));
        news.setPopup(type == 1);
        news.setLifeTime(form.getParamInt("lifeTime", 30));
        news.setReadTime(form.getParamInt("readTime", 24));

        if (Utils.isBlankString(news.getTitle())) {
            throw new BGMessageException("Нельзя добавить " + kindOf + " без заголовка");
        }

        if (Utils.isBlankString(news.getText())) {
            throw new BGMessageException("Нельзя добавить " + kindOf + " без текста");
        }

        Set<Integer> groups = form.getParamValues("group");

        ConfigMap perm = form.getPermission();

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
            Set<Integer> userSet = new HashSet<>();
            userSet.add(requestUserId);

            new NewsDAO(con).updateNewsUsers(news, userSet);
        }

        return json(con, form);
    }

    public ActionForward newsEdit(DynActionForm form, Connection con) throws Exception {
        News news = new NewsDAO(con).getNews(form.getId());
        if (news != null) {
            form.setResponseData("news", news);
        }

        return html(con, form, PATH_JSP + "/update.jsp");
    }

    public ActionForward newsList(DynActionForm form, Connection con) throws Exception {
        int readInt = form.getParamInt("read", 0);
        Boolean read = readInt >= 0 ? Utils.parseBoolean(String.valueOf(readInt)) : null;

        Pageable<News> searchResult = new Pageable<>(form);

        new NewsDAO(con).searchNewsList(searchResult, form.getUserId(), read, form.getParam("text"));

        if (read != null && !read)
            updatePersonalization(form, con, map -> map.put(UNREAD_NEWS_PERSONAL_KEY, Utils.getObjectIds(searchResult.getList())));

        return html(con, form, PATH_JSP + "/list.jsp");
    }

    public ActionForward newsGet(DynActionForm form, Connection con) throws Exception {
        NewsDAO newsDAO = new NewsDAO(con);
        newsDAO.setNewsRead(form.getParamInt("newsId", -1), form.getUserId(), true);
        News news = newsDAO.getNews(form.getParamInt("newsId", -1));

        form.setResponseData("item", news);

        UserNewsCache.flushCache(con, Collections.singleton(form.getUserId()));

        return html(con, form, PATH_JSP + "/content.jsp");
    }

    public ActionForward newsSetRead(DynActionForm form, Connection con) throws Exception {
        new NewsDAO(con).setNewsRead(form.getParamInt("newsId", -1), form.getUserId(),
                form.getParamBoolean("value", true));

        UserNewsCache.flushCache(con, Collections.singleton(form.getUserId()));

        return json(con, form);
    }

    public ActionForward newsSetAllRead(DynActionForm form, Connection con) throws Exception {
        new NewsDAO(con).setNewsAllRead(form.getUserId());

        UserNewsCache.flushCache(con, Collections.singleton(form.getUserId()));

        return json(con, form);
    }

    public ActionForward newsDelete(DynActionForm form, Connection con) throws Exception {
        new NewsDAO(con).deleteNews(form.getId());

        return json(con, form);
    }
}