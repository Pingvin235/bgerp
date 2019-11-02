package ru.bgcrm.dao.message;
/*
 package ru.bgcrm.dao.message;
 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.message.MessageCategory;

public class MessageCategoryDAO
    extends CommonDAO
{
    public final static String TABLE_MESSAGE_CATEGORY = "message_category";

    public MessageCategoryDAO( DataSource con )
    {
        super( con );
        log = Logger.getLogger( MessageCategoryDAO.class );
    }

    **
     * Возвращает категорию
     * 
     * @param id
     *            код категории
     * @return категория или null если не найдена
     *
    public MessageCategory getMessageCategory( int id )
    {
        MessageCategory messageCategory = null;
        Connection con = null;
        try
        {
            int index = 1;
            ResultSet rs = null;
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();
            //
            con = con.getConnection();
            query.append( SQL_SELECT );
            query.append( "*" );
            query.append( SQL_FROM );
            query.append( TABLE_MESSAGE_CATEGORY );
            query.append( SQL_WHERE );
            query.append( "id=?" );
            ps = con.prepareStatement( query.toString() );
            ps.setInt( index++, id );
            rs = ps.executeQuery();
            while ( rs.next() )
            {
                messageCategory = new MessageCategory();
                setMessageCategoryData( messageCategory, rs );
            }
            rs.close();
            ps.close();
        }
        catch( Exception ex )
        {
            log.error( "getMessageCategory( " + id + " )", ex );
        }
        finally
        {
            closeConnection( con );
        }
        return messageCategory;
    }

    **
     * Возвращает список категорий для заданого upId
     * 
     * @param upId
     *            код ветки содержимое которой надо вернуть в списоке
     * @return список сортированный по upId и title, если upId<0 возвращает
     *         список всех категорий, при upId>-1 возвращает список категорий с
     *         заданным upId
     *
    public List<MessageCategory> getMessageCategoryByUpId( int upId )
    {
        List<MessageCategory> list = new ArrayList<MessageCategory>();
        Connection con = null;
        try
        {
            int index = 1;
            ResultSet rs = null;
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();
            //
            con = con.getConnection();
            query.append( SQL_SELECT );
            query.append( "*" );
            query.append( SQL_FROM );
            query.append( TABLE_MESSAGE_CATEGORY );
            if ( upId > -1 )
            {
                query.append( SQL_WHERE );
                query.append( "up_id=?" );
            }
            query.append( SQL_ORDER_BY );
            query.append( "up_id, title" );
            ps = con.prepareStatement( query.toString() );
            if ( upId > -1 )
            {
                ps.setInt( index++, upId );
            }
            rs = ps.executeQuery();
            while ( rs.next() )
            {
                MessageCategory messageCategory = new MessageCategory();
                setMessageCategoryData( messageCategory, rs );
                list.add( messageCategory );
            }
            rs.close();
            ps.close();
        }
        catch( Exception ex )
        {
            log.error( "getMessageCategoryByUpId( " + upId + " )", ex );
        }
        finally
        {
            closeConnection( con );
        }
        return list;
    }

    **
     * Обновляет категорию, если <b>messageCategory != null &&
     * messageCategory.getId() > 0</b>. Добавляет категорию, если
     * <b>messageCategory != null</b>
     * 
     * @param messageCategory
     *            категория
     *
    public void updateMessageCategory( MessageCategory messageCategory )
    {
        if ( messageCategory != null )
        {
            Connection con = null;
            try
            {
                int index = 1;
                PreparedStatement ps = null;
                StringBuilder query = new StringBuilder();
                //
                con = con.getConnection();
                if ( messageCategory.getId() > 0 )
                {
                    query.append( SQL_UPDATE );
                    query.append( TABLE_MESSAGE_CATEGORY );
                    query.append( " SET up_id=?, default_id=?, title=?" );
                    query.append( SQL_WHERE );
                    query.append( "id=?" );
                    ps = con.prepareStatement( query.toString() );
                    ps.setInt( index++, messageCategory.getUpId() );
                    ps.setInt( index++, messageCategory.getDefaultId() );
                    ps.setString( index++, messageCategory.getTitle() );
                    ps.setInt( index++, messageCategory.getId() );
                    ps.executeUpdate();
                    ps.close();
                }
                else
                {
                    query.append( SQL_INSERT );
                    query.append( TABLE_MESSAGE_CATEGORY );
                    query.append( " SET up_id=?, default_id=?, title=?" );
                    ps = con.prepareStatement( query.toString(), PreparedStatement.RETURN_GENERATED_KEYS );
                    ps.setInt( index++, messageCategory.getUpId() );
                    ps.setInt( index++, messageCategory.getDefaultId() );
                    ps.setString( index++, messageCategory.getTitle() );
                    ps.setInt( index++, messageCategory.getId() );
                    ps.executeUpdate();
                    messageCategory.setId( lastInsertId( ps ) );
                    ps.close();
                }
            }
            catch( Exception ex )
            {
                log.error( "updateMessageCategory( " + messageCategory + " )", ex );
            }
            finally
            {
                closeConnection( con );
            }
        }
    }

    private void setMessageCategoryData( MessageCategory messageCategory, ResultSet rs )
        throws SQLException
    {
        messageCategory.setId( rs.getInt( "id" ) );
        messageCategory.setUpId( rs.getInt( "up_id" ) );
        messageCategory.setDefaultId( rs.getInt( "default_id" ) );
        messageCategory.setTitle( rs.getString( "title" ) );
        messageCategory.setConfig( rs.getString( "config" ) );
    }

    public static void initMessageCategory( MessageCategory rootMmessageCategory,
                                            List<MessageCategory> messageCategoryList )
    {
        for ( MessageCategory messageCategory : messageCategoryList )
        {
            if ( messageCategory.getUpId() == rootMmessageCategory.getId() )
            {
                rootMmessageCategory.getChildList().add( messageCategory );
                initMessageCategory( messageCategory, messageCategoryList );
            }
        }
    }
    
}
*/