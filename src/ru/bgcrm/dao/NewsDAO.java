package ru.bgcrm.dao;

import static ru.bgcrm.dao.Tables.TABLE_NEWS;
import static ru.bgcrm.dao.Tables.TABLE_NEWS_USER;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.cache.UserNewsCache;
import ru.bgcrm.model.News;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.PreparedDelay;

public class NewsDAO extends CommonDAO {
    public NewsDAO(Connection con) {
        super(con);
    }

    public void searchNewsList(SearchResult<News> searchResult, int userId, Boolean read, String text) throws SQLException {
        Page page = searchResult.getPage();
        List<News> result = searchResult.getList();

        PreparedDelay pd = new PreparedDelay(con);
        pd.addQuery(SQL_SELECT_COUNT_ROWS + " * FROM " + TABLE_NEWS + " AS n " + "INNER JOIN " + TABLE_NEWS_USER + " AS u ON n.id=u.news_id "
                + "WHERE u.user_id=? ");
        pd.addInt(userId);

        if (read != null) {
            if (read) {
                pd.addQuery("AND is_read ");
            } else {
                pd.addQuery("AND NOT(is_read) ");
            }
        }
        if (Utils.notBlankString(text)) {
            pd.addQuery(" AND (POSITION(? IN n.title)>0 OR POSITION(? IN n.description)>0) ");
            pd.addString(text);
            pd.addString(text);
        }

        pd.addQuery("ORDER BY n.create_dt DESC ");
        pd.addQuery(getPageLimit(page));

        ResultSet rs = pd.executeQuery();
        while (rs.next()) {
            News news = getNewsFromRs(rs);
            news.setRead(rs.getBoolean("is_read"));
            result.add(news);
        }

        if (page != null) {
            page.setRecordCount(getFoundRows(pd.getPrepared()));
        }
        pd.close();
    }

    public News getNews(int newsId) throws SQLException {
        News news = new News();
        String query = "SELECT * FROM " + TABLE_NEWS + " WHERE id=?";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, newsId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            news = getNewsFromRs(rs);
        }

        ps.close();

        return news;
    }

    public int updateNews(News news) throws SQLException {
        int index = 1;
        String query = null;
        PreparedStatement ps = null;

        if (news.getId() <= 0) {
            query = "INSERT INTO " + TABLE_NEWS + " SET create_dt=now(), title=?, description=?, user_id=?, is_popup=?, life_time=?, read_time=?";
            ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(index++, news.getTitle());
            ps.setString(index++, news.getDescription());
            ps.setInt(index++, news.getUserId());
            ps.setBoolean(index++, news.isPopup());
            ps.setInt(index++, news.getLifeTime());
            ps.setInt(index++, news.getReadTime());
            ps.executeUpdate();
            news.setId(lastInsertId(ps));
        } else {
            query = "UPDATE " + TABLE_NEWS
                    + " SET update_dt=now(), title=?, description=?, user_id=?, is_popup=?, life_time=?, read_time=? WHERE id=?";
            ps = con.prepareStatement(query);
            ps.setString(index++, news.getTitle());
            ps.setString(index++, news.getDescription());
            ps.setInt(index++, news.getUserId());
            ps.setBoolean(index++, news.isPopup());
            ps.setInt(index++, news.getLifeTime());
            ps.setInt(index++, news.getId());
            ps.setInt(index++, news.getReadTime());
            ps.executeUpdate();
        }
        ps.close();

        return news.getId();
    }

    /**
     * Отправляет новость выбранным пользователям, сбрасывает кэш новостей.
     * @param news
     * @param userIds
     * @throws SQLException
     */
    public void updateNewsUsers(News news, Set<Integer> userIds) throws SQLException {
        deleteNewsUser(news.getId());

        news.setId(updateNews(news));
        for (Integer userId : userIds) {
            updateNewsUser(news, userId);
        }
        UserNewsCache.flushCache(con, userIds);
    }

    public void updateNewsGroups(News news, Set<Integer> groupIds) throws SQLException {
        Set<Integer> userIds = new HashSet<Integer>();
        for (User user : UserCache.getUserList()) {
            if (CollectionUtils.intersection(user.getGroupIds(), groupIds).size() > 0) {
                userIds.add(user.getId());
            }
        }
        updateNewsUsers(news, userIds);

        String query = "UPDATE " + TABLE_NEWS + " SET `groups`=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, Utils.toString(groupIds));
        ps.setInt(2, news.getId());
        ps.executeUpdate();
        ps.close();
    }

    public void setNewsRead(int newsId, int userId, boolean value) throws SQLException {
        int index = 1;

        String query = "UPDATE " + TABLE_NEWS_USER + " SET is_read=? WHERE news_id=? AND user_id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setBoolean(index++, value);
        ps.setInt(index++, newsId);
        ps.setInt(index++, userId);
        ps.executeUpdate();

        ps.close();
    }

    public void setNewsAllRead(int userId) throws SQLException {
        int index = 1;

        String query = "UPDATE " + TABLE_NEWS_USER + " SET is_read=1 WHERE user_id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(index++, userId);
        ps.executeUpdate();

        ps.close();
    }

    private void updateNewsUser(News news, int userId) throws SQLException {
        int index = 1;

        String query = "INSERT INTO " + TABLE_NEWS_USER + " SET news_id=?, user_id=?";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(index++, news.getId());
        ps.setInt(index++, userId);
        ps.executeUpdate();

        ps.close();
    }

    private News getNewsFromRs(ResultSet rs) throws SQLException {
        News news = new News();

        news.setId(rs.getInt("id"));
        news.setUserId(rs.getInt("user_id"));
        news.setCreateDate(rs.getTimestamp("create_dt"));
        news.setUpdateDate(rs.getTimestamp("update_dt"));
        news.setTitle(rs.getString("title"));
        news.setDescription(rs.getString("description"));
        news.setPopup(rs.getBoolean("is_popup"));
        news.setLifeTime(rs.getInt("life_time"));
        news.setReadTime(rs.getInt("read_time"));
        news.setGroupIds(Utils.toIntegerSet(rs.getString("groups")));

        return news;
    }

    public void deleteNews(int newsId) throws SQLException {
        PreparedStatement ps = con.prepareStatement("DELETE FROM " + TABLE_NEWS + " WHERE id=?");
        ps.setInt(1, newsId);
        ps.executeUpdate();
        ps.close();

        Set<Integer> newsUserIds = new HashSet<Integer>();

        ps = con.prepareStatement("SELECT user_id FROM " + TABLE_NEWS_USER + " WHERE news_id=?");
        ps.setInt(1, newsId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            newsUserIds.add(rs.getInt(1));
        }
        ps.close();

        UserNewsCache.flushCache(con, newsUserIds);

        deleteNewsUser(newsId);
    }

    private void deleteNewsUser(int newsId) throws SQLException {
        PreparedStatement ps;
        ps = con.prepareStatement("DELETE FROM " + TABLE_NEWS_USER + " WHERE news_id=?");
        ps.setInt(1, newsId);
        ps.executeUpdate();
        ps.close();
    }
}