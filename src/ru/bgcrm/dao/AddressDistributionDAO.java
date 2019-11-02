package ru.bgcrm.dao;

import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_DISTRIBUTION;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_HOUSE;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_QUARTER;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_QUARTER_DISTRIBUTION;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_STREET;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_ADDRESS;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_CONFIG;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.Tables;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.action.AddressDistributionAction.Distribution;
import ru.bgcrm.util.Utils;

public class AddressDistributionDAO
    extends CommonDAO
{
    public AddressDistributionDAO( Connection con )
    {
        super( con );
    }

    public void addHouseToUser( Distribution distr, int userId, String[] houseIds )
        throws SQLException, BGException
    {
        for( String houseId : houseIds )
        {
            PreparedStatement ps = con.prepareStatement( "INSERT INTO " + TABLE_ADDRESS_DISTRIBUTION + "(user_id , distr_id , hid) VALUES ( ? , ? , ? )" );
            ps.setInt( 1, userId );
            ps.setInt( 2, distr.getId() );
            ps.setInt( 3, Integer.parseInt( houseId ) );
            ps.executeUpdate();
            ps.close();

            if( distr.isUpdateProcessExecutorOnDistrChange() )
            {
                StringBuilder sb = new StringBuilder();
                sb.append( " SELECT p.id AS process_id " );
                sb.append( " FROM " ).append( Tables.TABLE_PROCESS ).append( " AS p " );
                sb.append( " INNER JOIN " ).append( TABLE_PARAM_ADDRESS ).append( " AS pa ON p.id=pa.id AND pa.param_id = ? AND pa.house_id = ? " );
                sb.append( " WHERE 1=1 " );
                sb.append( " AND p.type_id IN (" ).append( Utils.toString( distr.getProcessTypeIds() ) ).append( ") " );
                sb.append( " AND p.status_id= (" ).append( Utils.toString( distr.getProcessStatusIds() ) ).append( ") " );

                ps = con.prepareStatement( sb.toString() );
                ps.setInt( 1, distr.getAddressParamId() );
                ps.setInt( 2, Integer.parseInt( houseId ) );

                ProcessDAO processDAO = new ProcessDAO( con );
                ResultSet rs = ps.executeQuery();
                while( rs.next() )
                {
                    int processId = rs.getInt( "process_id" );

                    Set<Integer> executorIds = new HashSet<Integer>();
                    executorIds.add( userId );
                    processDAO.updateProcessExecutors( processId, executorIds );
                }
                ps.close();
            }
        }
    }

    public void removeHouse( int userId, int distrId, String[] hid )
        throws SQLException
    {
        for( String houseId : hid )
        {
            StringBuilder sb = new StringBuilder();
            sb.append( " DELETE FROM " );
            sb.append( TABLE_ADDRESS_DISTRIBUTION );
            sb.append( " WHERE user_id=? AND distr_id=? AND hid=? " );

            PreparedStatement ps = con.prepareStatement( sb.toString() );
            ps.setInt( 1, userId );
            ps.setInt( 2, distrId );
            ps.setInt( 3, Integer.parseInt( houseId ) );
            ps.executeUpdate();
            ps.close();
        }
    }

    public List<Map<String, String>> getAllHouses( List<Integer> cityIds )
        throws SQLException
    {
        List<Map<String, String>> houses = new ArrayList<Map<String, String>>();

        StringBuilder sb = new StringBuilder();
        sb.append( " SELECT " );
        sb.append( "	ah.id AS hid," );
        sb.append( "	aq.title AS quarter, " );
        sb.append( "	ass.title AS street, " );
        sb.append( "	ah.house AS house," );
        sb.append( "	ah.frac AS frac " );
        sb.append( " FROM " ).append( TABLE_ADDRESS_HOUSE ).append( " AS ah " );
        sb.append( " LEFT JOIN " ).append( TABLE_ADDRESS_QUARTER ).append( " AS aq ON ah.quarter_id=aq.id " );
        sb.append( " LEFT JOIN " ).append( TABLE_ADDRESS_STREET ).append( " AS ass ON ah.street_id=ass.id " );
        sb.append( " WHERE 1=1 AND aq.city_id IN ( " );
        sb.append( Utils.toString( cityIds ) );
        sb.append( " ) " );

        PreparedStatement ps = con.prepareStatement( sb.toString() );

        ResultSet rs = ps.executeQuery();

        while( rs.next() )
        {
            Map<String, String> house = new HashMap<String, String>();
            house.put( "hid", rs.getString( "hid" ) );
            house.put( "quarter", rs.getString( "quarter" ) );
            house.put( "street", rs.getString( "street" ) );
            house.put( "house", rs.getString( "house" ) );
            house.put( "frac", rs.getString( "frac" ) );

            houses.add( house );
        }

        ps.close();
        return houses;
    }

    public void freeUserHouses( int userId )
        throws SQLException
    {
        StringBuilder sb = new StringBuilder();
        sb.append( " DELETE FROM " ).append( TABLE_ADDRESS_DISTRIBUTION );
        sb.append( " WHERE user_id = ? " );

        PreparedStatement ps = con.prepareStatement( sb.toString() );
        ps.setInt( 1, userId );
        ps.executeUpdate();
        ps.close();
    }

    public List<IdTitle> getUserHouses( int userId, String quarterMask, String streetMask, String houseMask )
        throws SQLException
    {
        List<IdTitle> userHouses = new ArrayList<IdTitle>();

        StringBuilder sb = new StringBuilder();
        sb.append( " SELECT " );
        sb.append( " 	ah.id AS hid, " );
        sb.append( " 	aq.title AS quarter, " );
        sb.append( " 	ass.title AS street, " );
        sb.append( " 	ah.house AS house," );
        sb.append( "	ah.frac AS frac " );
        sb.append( " FROM " + TABLE_ADDRESS_DISTRIBUTION + " AS ad " );
        sb.append( " LEFT JOIN " ).append( TABLE_ADDRESS_HOUSE ).append( " AS ah ON ad.hid=ah.id " );
        sb.append( " LEFT JOIN " ).append( TABLE_ADDRESS_QUARTER ).append( " AS aq ON ah.quarter_id=aq.id " );
        sb.append( " LEFT JOIN " ).append( TABLE_ADDRESS_STREET ).append( " AS ass ON ah.street_id=ass.id " );
        sb.append( " WHERE ad.user_id=?" );

        addFilters( quarterMask, streetMask, houseMask, sb );

        PreparedStatement ps = con.prepareStatement( sb.toString() );
        ps.setInt( 1, userId );

        ResultSet rs = ps.executeQuery();
        while( rs.next() )
        {
            Integer hid = rs.getInt( "hid" );
            String quarter = rs.getString( "quarter" );
            String street = rs.getString( "street" );
            String house = rs.getString( "house" );
            String frac = rs.getString( "frac" );

            String address = quarter + ", " + street + ", " + house + frac;

            userHouses.add( new IdTitle( hid, address ) );
        }
        ps.close();

        return userHouses;
    }

    private void addFilters( String quarterMask, String streetMask, String houseMask, StringBuilder sql )
    {
        if( quarterMask != null ) sql.append( " AND aq.title LIKE '%" ).append( quarterMask ).append( "%' " );
        if( streetMask != null ) sql.append( " AND ass.title LIKE '%" ).append( streetMask ).append( "%' " );
        if( houseMask != null ) sql.append( " AND ah.house LIKE '%" ).append( houseMask ).append( "%' " );
    }

    public List<IdTitle> getUndistributedHouses( int distrId, List<Integer> cityIds, String quarterMask, String streetMask, String houseMask )
        throws SQLException
    {
        List<IdTitle> undistHouses = new ArrayList<IdTitle>();

        StringBuilder sb = new StringBuilder();
        sb.append( " SELECT " );
        sb.append( "	ah.id AS hid," );
        sb.append( "	aq.title AS quarter, " );
        sb.append( "	ass.title AS street, " );
        sb.append( "	ah.house AS house," );
        sb.append( "	ah.frac AS frac, " );
        sb.append( "    ad.hid AS ad_hid" );
        sb.append( " FROM " ).append( TABLE_ADDRESS_HOUSE ).append( " AS ah " );
        sb.append( " LEFT JOIN " ).append( TABLE_ADDRESS_QUARTER ).append( " AS aq ON ah.quarter_id=aq.id " );
        sb.append( " LEFT JOIN " ).append( TABLE_ADDRESS_STREET ).append( " AS ass ON ah.street_id=ass.id " );
        sb.append( " LEFT JOIN " ).append( TABLE_ADDRESS_DISTRIBUTION ).append( " AS ad ON ( ah.id=ad.hid AND " + distrId + ") " );
        sb.append( " WHERE 1=1 AND aq.city_id IN ( " );
        sb.append( Utils.toString( cityIds ) );
        sb.append( " ) " );

        addFilters( quarterMask, streetMask, houseMask, sb );

        sb.append( " HAVING ad_hid IS NULL" );

        PreparedStatement ps = con.prepareStatement( sb.toString() );
        ResultSet rs = ps.executeQuery();

        while( rs.next() )
        {
            Integer hid = rs.getInt( "hid" );
            String quarter = rs.getString( "quarter" );
            String street = rs.getString( "street" );
            String house = rs.getString( "house" );
            String frac = rs.getString( "frac" );

            String address = quarter + ", " + street + ", " + house + frac;

            undistHouses.add( new IdTitle( hid, address ) );
        }

        ps.close();
        return undistHouses;
    }

    public List<IdTitle> getUndistributedGroups( int distrId, List<Integer> cityIds )
        throws BGException
    {
        List<IdTitle> result = new ArrayList<IdTitle>();

        String query = " SELECT aq.id AS quarter_id, aq.title AS quarter_title, d.group_id AS group_id FROM " + TABLE_ADDRESS_QUARTER + " aq " + " LEFT JOIN " + TABLE_ADDRESS_QUARTER_DISTRIBUTION + " d ON ( aq.id=d.quarter_id AND d.distr_id=? ) " + " WHERE city_id IN ( " + Utils.toString( cityIds ) + " ) " + " HAVING group_id IS NULL ";

        try
        {
            PreparedStatement ps = con.prepareStatement( query );
            ps.setInt( 1, distrId );

            ResultSet rs = ps.executeQuery();

            while( rs.next() )
            {
                result.add( new IdTitle( rs.getInt( "quarter_id" ), rs.getString( "quarter_title" ) ) );
            }

            ps.close();
        }
        catch( SQLException ex )
        {
            throw new BGException( ex );
        }

        return result;
    }

    public User getUserByHouseId( int distrId, int houseId )
        throws BGException
    {
        try
        {
            User result = null;

            StringBuilder sb = new StringBuilder();
            sb.append( " SELECT user_id FROM " ).append( TABLE_ADDRESS_DISTRIBUTION );
            sb.append( " WHERE hid=? AND distr_id=? " );
            sb.append( " LIMIT 1 " );

            PreparedStatement ps = con.prepareStatement( sb.toString() );

            ps.setInt( 1, houseId );
            ps.setInt( 2, distrId );

            ResultSet rs = ps.executeQuery();
            if( rs.next() )
            {
                result = UserCache.getUser( rs.getInt( 1 ) );
            }
            ps.close();

            return result;
        }
        catch( SQLException e )
        {
            throw new BGException( e );
        }
    }

    public void addQuarterToGroup( Set<Integer> quarterSet, int distrId, int groupId )
        throws BGException
    {
        try
        {
            for( Integer quarter : quarterSet )
            {
                PreparedStatement ps = con.prepareStatement( "INSERT INTO " + TABLE_ADDRESS_QUARTER_DISTRIBUTION + "(distr_id , quarter_id , group_id) VALUES ( ? , ? , ? )" );
                ps.setInt( 1, distrId );
                ps.setInt( 2, quarter );
                ps.setInt( 3, groupId );
                ps.executeUpdate();
                ps.close();
            }
        }
        catch( SQLException e )
        {
            throw new BGException( e );
        }
    }

    public List<IdTitle> getGroupQuarters( int distrId, int groupId )
        throws BGException
    {
        List<IdTitle> result = new ArrayList<IdTitle>();

        String query = "SELECT aq.id, aq.title FROM " + TABLE_ADDRESS_QUARTER_DISTRIBUTION + " d " + "LEFT JOIN " + TABLE_ADDRESS_QUARTER + " aq ON d.quarter_id = aq.id " + "WHERE d.distr_id=? AND d.group_id=?";

        try
        {
            PreparedStatement ps = con.prepareStatement( query );
            ps.setInt( 1, distrId );
            ps.setInt( 2, groupId );

            ResultSet rs = ps.executeQuery();

            while( rs.next() )
            {
                result.add( new IdTitle( rs.getInt( "id" ), rs.getString( "title" ) ) );
            }

            ps.close();
        }
        catch( SQLException e )
        {
            throw new BGException( e );
        }

        return result;
    }

    public void removeQuarters( int distrId )
        throws BGException
    {
        try
        {
            PreparedStatement ps = con.prepareStatement( " DELETE FROM " + TABLE_ADDRESS_QUARTER_DISTRIBUTION + " WHERE distr_id=? " );

            ps.setInt( 1, distrId );
            ps.executeUpdate();
            ps.close();
        }
        catch( SQLException e )
        {
            throw new BGException( e );
        }
    }

    public void removeQuarters( int distrId, int groupId, Set<Integer> quarters )
        throws BGException
    {
        for( Integer quarter : quarters )
        {
            try
            {
                PreparedStatement ps = con.prepareStatement( " DELETE FROM " + TABLE_ADDRESS_QUARTER_DISTRIBUTION + " WHERE distr_id=? AND group_id=? AND quarter_id=? " );

                ps.setInt( 1, distrId );
                ps.setInt( 2, groupId );
                ps.setInt( 3, quarter );
                ps.executeUpdate();
                ps.close();
            }
            catch( SQLException e )
            {
                throw new BGException( e );
            }
        }
    }

    public int getGroupIdByQuarter( int distrId, int quarter )
        throws BGException
    {
        int result = 0;

        try
        {
            PreparedStatement ps = con.prepareStatement( " SELECT group_id FROM " + TABLE_ADDRESS_QUARTER_DISTRIBUTION + " WHERE distr_id=? AND quarter_id=? " );

            ps.setInt( 1, distrId );
            ps.setInt( 2, quarter );

            ResultSet rs = ps.executeQuery();

            if( rs.first() )
            {
                result = rs.getInt( "group_id" );
            }

            ps.close();

        }
        catch( SQLException e )
        {
            throw new BGException( e );
        }

        return result;
    }

    public int getFlatCount( int userId )
        throws BGException

    {
        int flatCount = 0;

        String query = " SELECT sum(ac.value) as flat" + " FROM " + TABLE_ADDRESS_DISTRIBUTION + " AS ad " + " LEFT JOIN " + TABLE_ADDRESS_HOUSE + " AS ah ON ad.hid=ah.id " + " LEFT JOIN " + TABLE_ADDRESS_QUARTER + " AS aq ON ah.quarter_id=aq.id " + " LEFT JOIN " + TABLE_ADDRESS_STREET + " AS ass ON ah.street_id=ass.id " + " LEFT JOIN " + TABLE_ADDRESS_CONFIG + " AS ac ON ah.id=ac.record_id AND ac.`key`='.i.flat.amount' " + " WHERE ad.user_id=? ";

        try
        {
            PreparedStatement ps = con.prepareStatement( query );
            ps.setInt( 1, userId );

            ResultSet rs = ps.executeQuery();

            if( rs.next() )
            {
                flatCount = rs.getInt( "flat" );
            }

            ps.close();
        }
        catch( SQLException e )
        {
            throw new BGException( e );
        }

        return flatCount;
    }
}
