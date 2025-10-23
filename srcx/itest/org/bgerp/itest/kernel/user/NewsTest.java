package org.bgerp.itest.kernel.user;

import org.bgerp.cache.UserCache;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.testng.annotations.Test;

import ru.bgcrm.dao.NewsDAO;
import ru.bgcrm.model.News;
import ru.bgcrm.util.Utils;

@Test(groups = "news", dependsOnGroups = "user")
public class NewsTest {
    @Test
    public void news() throws Exception {
        News news = new News();
        news.setUserId(UserTest.USER_ADMIN_ID);
        news.setTitle("Update and Restart");
        news.setText(ResourceHelper.getResource(this, "news.ur.txt"));
        news.setPopup(true);
        news.setLifeTime(30);
        news.setReadTime(100);

        var dao = new NewsDAO(DbTest.conRoot);
        dao.updateNews(news);
        dao.updateNewsUsers(news, Utils.getObjectIdsSet(UserCache.getActiveUsers()));

        news = new News();
        news.setUserId(UserTest.USER_ADMIN_ID);
        news.setTitle("Team Building Party");
        news.setText(ResourceHelper.getResource(this, "news.tb.txt"));
        news.setPopup(false);
        news.setLifeTime(30);
        news.setReadTime(100);

        dao.updateNews(news);
        dao.updateNewsUsers(news, Utils.getObjectIdsSet(UserCache.getActiveUsers()));
    }
}
