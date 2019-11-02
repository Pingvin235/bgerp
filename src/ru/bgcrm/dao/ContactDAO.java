package ru.bgcrm.dao;

/*
import static ru.bgcrm.dao.Tables.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ru.bgcrm.model.Contact;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.Period;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.util.Utils;

public class ContactDAO
    extends CommonDAO
{
    public ContactDAO( Connection con )
    {
        super( con );
        log = Logger.getLogger( ContactDAO.class );
    }

    public void searchContactList( SearchResult<Contact> searchResult,
                                   String title,
                                   Period period )
    	throws SQLException
    {
        if ( searchResult != null )
        {
            Page page = searchResult.getPage();
            List<Contact> list = searchResult.getList();
            
            int index = 1;
            ResultSet rs = null;
            PreparedStatement ps = null;
            StringBuilder sql = new StringBuilder();
            
            sql.append( "SELECT * FROM contact WHERE 1=1" );
            sql.append( getLikeSql( title, "title" ) );
            sql.append( getPeriodSql( period, "date_created" ) );
            sql.append( "ORDER BY title" );
            sql.append( getMySQLLimit( page ) );
            ps = con.prepareStatement( sql.toString() );
            if ( title != null && !title.isEmpty() )
            {
            	ps.setString( index++, title );
            }
            index = setPeriodParamValue( period, ps, index );
            rs = ps.executeQuery();
            while ( rs.next() )
            {
            	list.add( getContactFromRs( rs ) );
            }
            rs.close();
            if ( page != null )
            {
            	page.setRecordCount( getFoundRows( ps ) );
            }
            ps.close();
        }
    }
    
    public void searchContactListForCustomerId( SearchResult<Contact> searchResult, int customerId, Date onDate, boolean deleted )
    	throws SQLException
    {
        if ( searchResult != null )
        {
            Page page = searchResult.getPage();
            List<Contact> list = searchResult.getList();
     
            int index = 1;
            ResultSet rs = null;
            PreparedStatement ps = null;
            StringBuilder sql = new StringBuilder();

            sql.append( "SELECT SQL_CALC_FOUND_ROWS c.* FROM customer_contact_link AS l " );
            sql.append( "LEFT JOIN contact AS c ON c.id=l.contact_id " );
            sql.append( "WHERE l.customer_id=? ");
            if ( onDate != null )
            {
            	sql.append( "AND l.date_from<=? AND ( isNull(l.date_to) OR l.date_to>=? ) " );
            }
            sql.append( getMySQLLimit( page ) );
            ps = con.prepareStatement( sql.toString() );
            ps.setInt( index++, customerId );
            if ( onDate != null )
            {
            	java.sql.Date sqlDate = convertDateToSqlDate( onDate );
            	ps.setDate( index++, sqlDate );
            	ps.setDate( index++, sqlDate );
            }
            rs = ps.executeQuery();
            while ( rs.next() )
            {
            	list.add( getContactFromRs( rs ) );
            }
            rs.close();
            if ( page != null )
            {
            	page.setRecordCount( getFoundRows( ps ) );
            }
            ps.close();
        }
    }
    
    public void searchContactListByPhone( SearchResult<Contact> searchResult,
                                           List<Integer> phoneParamIdList,
                                           String phoneNumber )
    	throws SQLException
    {
        if ( searchResult != null )
        {
            Page page = searchResult.getPage();
            List<Contact> list = searchResult.getList();
        
            int index = 1;
            ResultSet rs = null;
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();
            StringBuilder ids = new StringBuilder();
            if ( phoneParamIdList != null )
            	for ( Integer id : phoneParamIdList )
            	{
            		if ( ids.length() > 0 )
            		{
            			ids.append( ", " );
            		}
            		ids.append( id );
            	}

            query.append( SQL_SELECT );
            query.append( "DISTINCT c.*" );
            query.append( SQL_FROM );
            query.append( TABLE_CONTACT );
            query.append( " AS c LEFT JOIN param_phone AS p ON c.id=p.id" );
            query.append( SQL_WHERE );
            query.append( "true " );
            if ( ids.length() > 0 )
            {
            	query.append( "AND p.param_id IN ( " );
            	query.append( ids );
            	query.append( " ) " );
            }
            if ( phoneNumber != null )
            {
            	query.append( " AND ( phone_1 REGEXP ? OR phone_2 REGEXP ? OR phone_3 REGEXP ? OR phone_4 REGEXP ? OR phone_5 REGEXP ? )" );
            }
            query.append( " ORDER BY c.title" );
            query.append( getMySQLLimit( page ) );
            ps = con.prepareStatement( query.toString() );
            if ( phoneNumber != null )
            {
            	for ( int i = 1; i < 6; i++ )
            	{
            		ps.setString( index++, phoneNumber );
            	}
            }                
            rs = ps.executeQuery();
            while ( rs.next() )
            {
            	list.add( getContactFromRs( rs ) );
            }
            rs.close();
            if ( page != null )
            {
            	page.setRecordCount( getFoundRows( ps ) );
            }
            ps.close();
        }
    }
    
    public void searchContactListByEmail( SearchResult<Contact> searchResult,
                                          List<Integer> emailParamIdList,
                                          String email )
    	throws SQLException
    {
        if ( searchResult != null )
        {
            Page page = searchResult.getPage();
            List<Contact> list = searchResult.getList();
        
            ResultSet rs = null;
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();
            String ids = Utils.toString( emailParamIdList );

            query.append( SQL_SELECT );
            query.append( "DISTINCT c.*" );
            query.append( SQL_FROM );
            query.append( TABLE_CONTACT );
            query.append( "AS c" );
            query.append( SQL_INNER_JOIN );
            query.append( TABLE_PARAM_EMAIL );
            query.append( "AS param ON c.id=param.id AND param.value=?" );
            if( Utils.notBlankString( ids ) )
            {
            	query.append( " AND param.param_id IN(" );
            	query.append( ids );
            	query.append( ") " );
            }
            query.append( SQL_ORDER_BY );
            query.append( "c.title" );
            query.append( getMySQLLimit( page ) );

            ps = con.prepareStatement( query.toString() );
            ps.setString( 1, email );

            rs = ps.executeQuery();
            while ( rs.next() )
            {
            	list.add( getContactFromRs( rs ) );
            }
            rs.close();
            if ( page != null )
            {
            	page.setRecordCount( getFoundRows( ps ) );
            }
            ps.close();
        }
    }
    
    public Contact getContactById( int contactId )
    	throws SQLException
    {
        Contact contact = null;
   
        int index = 1;
        String sql = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        sql = "SELECT * FROM contact WHERE id=?";
        ps = con.prepareStatement( sql );
        ps.setInt( index++, contactId );
        rs = ps.executeQuery();
        while ( rs.next() )
        {
        	contact = getContactFromRs( rs );
        }
        ps.close();

        return contact;
    }

    public void updateContact( Contact contact )
    	throws SQLException
    {
        if ( contact != null )
        {
        	int index = 1;
        	int affectedRows = 0;
        	PreparedStatement ps = null;
        	StringBuilder query = new StringBuilder();

        	if ( contact.getId() > 0 )
        	{
        		query.append( SQL_UPDATE );
        		query.append( TABLE_CONTACT );
        		query.append( " SET title=?, title_pattern=?, title_pattern_id=?, param_group_id=?" );
        		query.append( " WHERE id=? AND ( title<>? OR title_pattern<>? OR title_pattern_id<>? OR param_group_id<>? )" );
        		ps = con.prepareStatement( query.toString() );
        		ps.setString( index++, contact.getTitle() );
        		ps.setString( index++, contact.getTitlePattern() );
        		ps.setInt( index++, contact.getTitlePatternId() );
        		ps.setInt( index++, contact.getParamGroupId() );
        		ps.setInt( index++, contact.getId() );
        		ps.setString( index++, contact.getTitle() );
        		ps.setString( index++, contact.getTitlePattern() );
        		ps.setInt( index++, contact.getTitlePatternId() );
        		ps.setInt( index++, contact.getParamGroupId() );
        		affectedRows = ps.executeUpdate();
        	}
        	else
        	{
        		query.append( SQL_INSERT );
        		query.append( TABLE_CONTACT );
        		query.append( " SET title=?, title_pattern=?, title_pattern_id=?, param_group_id=?, date_created=now(), user_id_created=?" );
        		ps = con.prepareStatement( query.toString(), Statement.RETURN_GENERATED_KEYS  );
        		ps.setString( index++, contact.getTitle() );
        		ps.setString( index++, contact.getTitlePattern() );
        		ps.setInt( index++, contact.getTitlePatternId() );
        		ps.setInt( index++, contact.getParamGroupId() );
        		ps.setInt( index++, contact.getCreatedUserId() );
        		affectedRows = ps.executeUpdate();
        		contact.setId( lastInsertId( ps ) );
        	}
        	if ( affectedRows > 0 )
        	{
        		index = 1;
        		query = new StringBuilder();
        		query.append( SQL_INSERT );
        		query.append( TABLE_CONTACT_LOG );
        		query.append( " SET id=?, title=?, title_pattern=?, title_pattern_id=?, param_group_id=?, date_changed=now(), user_id_changed=?" );
        		ps = con.prepareStatement( query.toString() );
        		ps.setInt( index++, contact.getId() );
        		ps.setString( index++, contact.getTitle() );
        		ps.setString( index++, contact.getTitlePattern() );
        		ps.setInt( index++, contact.getTitlePatternId() );
        		ps.setInt( index++, contact.getParamGroupId() );
        		ps.setInt( index++, contact.getCreatedUserId() );
        		ps.executeUpdate();
        	}
        	ps.close();
        }
    }
    
    public void deleteContact( int id )
    	throws SQLException
    {
    	StringBuilder query = new StringBuilder();
    	query.append( SQL_DELETE );
    	query.append( TABLE_CONTACT );
    	query.append( SQL_WHERE );
    	query.append( "id=?" );
    	
    	PreparedStatement ps = con.prepareStatement( query.toString() );
    	ps.setInt( 1, id );
    	ps.executeUpdate();
    	ps.close();    	
    }
    
    public static Contact getContactFromRs( ResultSet rs )
	    throws SQLException
	{
	    Contact contact = new Contact();
	    
	    contact.setId( rs.getInt( "id" ) );
	    contact.setTitle( rs.getString( "title" ) );
	    contact.setTitlePattern( rs.getString( "title_pattern" ) );
	    contact.setTitlePatternId( rs.getInt( "title_pattern_id" ) );
	    contact.setParamGroupId( rs.getInt( "param_group_id" ) );
	    contact.setCreatedDate( rs.getTimestamp( "date_created" ) );
	    contact.setCreatedUserId( rs.getInt( "user_id_created" ) );
	    
	    return contact;
	}
}*/