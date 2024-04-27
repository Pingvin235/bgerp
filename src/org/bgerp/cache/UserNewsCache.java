package org.bgerp.cache;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bgerp.action.NewsAction;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.l10n.Localization;
import org.bgerp.event.client.NewsInfoEvent;
import org.bgerp.model.Pageable;

import ru.bgcrm.dao.NewsDAO;
import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.model.News;
import ru.bgcrm.model.message.config.MessageTypeConfig;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.action.MessageAction;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SQLUtils;

public class UserNewsCache extends Cache<UserNewsCache> {
    private static CacheHolder<UserNewsCache> HOLDER = new CacheHolder<>(new UserNewsCache());

    public static NewsInfoEvent getUserEvent(ConnectionSet conSet, int userId) throws Exception {
        NewsInfoEvent result = HOLDER.getInstance().userInfoMap.get(userId);

        if (result == null) {
            final NewsDAO newsDao = new NewsDAO(conSet.getConnection());

            List<Integer> notPopupNews = new ArrayList<>();

            // непрочитанные новости пользователя
            Pageable<News> searchResult = new Pageable<>();
            newsDao.searchNewsList(searchResult, userId, false, null);

            List<Integer> popupNews = new ArrayList<>();
            for (News news : searchResult.getList()) {
                if (news.isPopup())
                    popupNews.add(news.getId());
                else
                    notPopupNews.add(news.getId());
            }

            final User user = UserCache.getUser(userId);

            String currentNonpopupUnread = Utils.toString(notPopupNews);
            String lastNonpopupUnread = user.getPersonalizationMap().get(NewsAction.UNREAD_NEWS_PERSONAL_KEY, "");

            final boolean blinkNews = !currentNonpopupUnread.equals(lastNonpopupUnread);

            // необработанные сообщения
            int currentUnprocessedMessages = 0;

            if (UserCache.getUser(userId).checkPerm(MessageAction.class.getName() + ":messageList")) {
                MessageTypeConfig mtConfig = Setup.getSetup().getConfig(MessageTypeConfig.class);
                for (MessageType type : mtConfig.getTypeMap().values()) {
                    if (type.getUnprocessedMessagesCount() != null)
                        currentUnprocessedMessages += type.getUnprocessedMessagesCount();
                }
            }

            int lastUnprocessedMessages = user.getPersonalizationMap().getInt(MessageAction.UNPROCESSED_MESSAGES_PERSONAL_KEY, 0);

            final boolean blinkMessages = currentUnprocessedMessages > 0 && currentUnprocessedMessages != lastUnprocessedMessages;

            // при возврате количества необработанных в 0 мигание прекращается, но сохраняется этот ноль в известном статусе,
            // чтобы начало мигать при любом изменении
            if (currentUnprocessedMessages == 0) {
                Preferences prefs = new Preferences();
                prefs.put(MessageAction.UNPROCESSED_MESSAGES_PERSONAL_KEY, "0");
                new UserDAO(conSet.getConnection()).updatePersonalization(user, prefs);
            }

            final var l = Localization.getLocalizer();

            result = new NewsInfoEvent(searchResult.getList().size(), currentUnprocessedMessages, popupNews, blinkNews, blinkMessages);
            result.message(l, "News");
            result.message(l, "Unprocessed messages");

            HOLDER.getInstance().userInfoMap.put(userId, result);
        }

        return result;
    }

    public static void flushCache(Connection con, Set<Integer> userIds) {
        SQLUtils.commitConnection(con);
        for (Integer userId : userIds) {
            HOLDER.getInstance().userInfoMap.remove(userId);
        }
    }

    public static void flush(Connection con) {
        HOLDER.flush(con);
    }

    // end of static part

    private final Map<Integer, NewsInfoEvent> userInfoMap = new ConcurrentHashMap<>();

    @Override
    protected UserNewsCache newInstance() {
        return new UserNewsCache();
    }

    /**
     * The cache is always valid.
     */
    @Override
    public boolean isValid() {
        return true;
    }
}