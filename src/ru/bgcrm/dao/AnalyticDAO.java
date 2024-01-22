package ru.bgcrm.dao;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.dao.param.Tables;

import ru.bgcrm.model.Period;
import ru.bgcrm.model.analytic.HouseCapacity;
import ru.bgcrm.model.analytic.HouseCapacityItem;
import ru.bgcrm.util.TimeUtils;

public class AnalyticDAO
    extends AddressDAO
{
    public static final String TABLE_ANALYTIC_HOUSE_CAPACITY = "analytic_house_capacity";

    public AnalyticDAO( Connection con )
    {
        super( con );
    }

    public List<HouseCapacity> getHouseCapacityList( String name, Period period )
        throws Exception
    {
        List<HouseCapacity> list = new ArrayList<HouseCapacity>();

        if( name != null && !"none".equals( name ) && period != null )
        {
            // список городов
            PreparedStatement ps = con.prepareStatement( "SELECT * FROM " + Tables.TABLE_ADDRESS_CITY + " ORDER BY title" );
            ResultSet rs = ps.executeQuery();
            while( rs.next() )
            {
                HouseCapacity houseCapacity = new HouseCapacity();
                houseCapacity.setCityId( rs.getInt( "id" ) );
                houseCapacity.setCityTitle( rs.getString( "title" ) );
                list.add( houseCapacity );
            }

            ps.close();

            // создаем Map по списку
            Map<Integer, HouseCapacity> map = new HashMap<Integer, HouseCapacity>();
            for( HouseCapacity houseCapacity : list )
            {
                map.put( houseCapacity.getCityId(), houseCapacity );
            }
            // кол-во квартир всего в домах на заданую дату
            Method method = HouseCapacity.class.getMethod( "setCount1", int.class );
            setCount( con, map, ".b.service." + name, period.getDateFrom(), method );
            method = HouseCapacity.class.getMethod( "setCount2", int.class );
            setCount( con, map, ".b.service." + name, period.getDateTo(), method );
            // кол-во квартир доступных на заданную дату
            method = HouseCapacity.class.getMethod( "setAvailable1", int.class );
            setAvailable( con, map, name, period.getDateFrom(), method );
            method = HouseCapacity.class.getMethod( "setAvailable2", int.class );
            setAvailable( con, map, name, period.getDateFrom(), method );
        }

        return list;
    }

    private void setCount( Connection con, Map<Integer, HouseCapacity> map, String name, Date date, Method method )
        throws Exception
    {
        ResultSet rs = null;
        PreparedStatement ps = null;
        StringBuilder query = new StringBuilder();
        query.append( "SELECT street.city_id, sum(IF(isNull(c.value), 0, c.value)) FROM address_house AS house" );
        query.append( " LEFT JOIN address_street AS street ON house.street_id=street.id" );
        query.append( " LEFT JOIN address_config AS c ON c.table_id='address_house' AND c.key='.i.flat.amount' AND house.id=c.record_id" );
        query.append( " LEFT JOIN address_config AS d ON d.table_id='address_house' AND d.key='.d.date' AND house.id=d.record_id" );
        query.append( " LEFT JOIN address_config AS e ON e.table_id='address_house' AND e.key=? AND house.id=e.record_id" );
        query.append( " WHERE e.value=1" );
        if( date == null )
        {
            query.append( " AND isNull(d.value)" );
        }
        else
        {
            query.append( " AND STR_TO_DATE(IF(isNull(d.value),'00.00.0000', d.value), '%d.%m.%Y')<=?" );
        }
        query.append( " GROUP BY street.city_id" );
        ps = con.prepareStatement( query.toString() );
        ps.setString( 1, name );
        if( date != null )
        {
            ps.setDate( 2, TimeUtils.convertDateToSqlDate( date ) );
        }
        rs = ps.executeQuery();
        while( rs.next() )
        {
            HouseCapacity houseCapacity = map.get( rs.getInt( 1 ) );
            if( houseCapacity != null )
            {
                method.invoke( houseCapacity, rs.getInt( 2 ) );
            }
        }
        ps.close();
    }

    private void setAvailable( Connection con, Map<Integer, HouseCapacity> map, String name, Date date, Method method )
        throws Exception
    {
        ResultSet rs = null;
        PreparedStatement ps = null;
        StringBuilder query = new StringBuilder();
        query.append( "CREATE TEMPORARY TABLE analytic_house" );
        query.append( " SELECT house_id, max(dt) AS date FROM " );
        query.append( TABLE_ANALYTIC_HOUSE_CAPACITY );
        query.append( " WHERE service_type=?" );
        if( date != null )
        {
            query.append( " AND dt<=?" );
        }
        query.append( " GROUP BY house_id" );
        ps = con.prepareStatement( query.toString() );
        ps.setString( 1, name );
        if( date != null )
        {
            ps.setDate( 2, TimeUtils.convertDateToSqlDate( date ) );
        }
        ps.executeUpdate();
        ps.close();

        query = new StringBuilder();
        query.append( "SELECT street.city_id, sum(ahc.value) FROM analytic_house AS ah" );
        query.append( " LEFT JOIN address_house AS house ON ah.house_id=house.id" );
        query.append( " LEFT JOIN address_street AS street ON house.street_id=street.id" );
        query.append( " LEFT JOIN analytic_house_capacity AS ahc ON ahc.house_id=ah.house_id AND ahc.date=ah.date" );
        query.append( " GROUP BY street.city_id" );
        ps = con.prepareStatement( query.toString() );
        rs = ps.executeQuery();
        while( rs.next() )
        {
            HouseCapacity houseCapacity = map.get( rs.getInt( 1 ) );
            if( houseCapacity != null )
            {
                method.invoke( houseCapacity, rs.getInt( 2 ) );
            }
        }
        ps.close();

        con.createStatement().executeUpdate( "DROP TABLE analytic_house" );
    }

    public List<HouseCapacityItem> getHouseCapacityItemList( int houseId )
        throws SQLException
    {
        List<HouseCapacityItem> list = new ArrayList<HouseCapacityItem>();

        ResultSet rs = null;
        StringBuilder query = null;
        PreparedStatement ps = null;

        query = new StringBuilder( "SELECT * FROM " );
        query.append( TABLE_ANALYTIC_HOUSE_CAPACITY );
        query.append( " WHERE house_id=? ORDER BY dt" );

        ps = con.prepareStatement( query.toString() );
        ps.setInt( 1, houseId );
        rs = ps.executeQuery();
        while( rs.next() )
        {
            HouseCapacityItem houseCapacityItem = new HouseCapacityItem();
            setHouseCapacityItemData( houseCapacityItem, rs );
            list.add( houseCapacityItem );
        }
        ps.close();

        return list;
    }

    public HouseCapacityItem getHouseCapacityItem( int houseId, String serviceType, Date date )
        throws SQLException
    {
        HouseCapacityItem houseCapacityItem = null;

        int index = 1;
        ResultSet rs = null;
        StringBuilder query = null;
        PreparedStatement ps = null;

        query = new StringBuilder( "SELECT * FROM " );
        query.append( TABLE_ANALYTIC_HOUSE_CAPACITY );
        query.append( " WHERE house_id=? AND service_type=? AND dt=?" );

        ps = con.prepareStatement( query.toString() );
        ps.setInt( index++, houseId );
        ps.setString( index++, serviceType );
        ps.setDate( index++, TimeUtils.convertDateToSqlDate( date ) );
        rs = ps.executeQuery();
        while( rs.next() )
        {
            houseCapacityItem = new HouseCapacityItem();
            setHouseCapacityItemData( houseCapacityItem, rs );
        }
        ps.close();

        return houseCapacityItem;
    }

    public void updateHouseCapacityItem( HouseCapacityItem houseCapacityItem )
        throws SQLException
    {
        if( houseCapacityItem != null )
        {
            int index = 1;
            StringBuilder query = null;
            PreparedStatement ps = null;

            query = new StringBuilder();
            query.append( SQL_UPDATE );
            query.append( TABLE_ANALYTIC_HOUSE_CAPACITY );
            query.append( SQL_SET );
            query.append( "value=?" );
            query.append( SQL_WHERE );
            query.append( "house_id=? AND service_type=? AND dt=?" );
            ps = con.prepareStatement( query.toString() );
            ps.setInt( index++, houseCapacityItem.getValue() );
            ps.setInt( index++, houseCapacityItem.getHouseId() );
            ps.setString( index++, houseCapacityItem.getServiceType() );
            ps.setDate( index++, TimeUtils.convertDateToSqlDate( houseCapacityItem.getDate() ) );

            int rowsUpdated = ps.executeUpdate();
            ps.close();

            if( rowsUpdated == 0 )
            {
                index = 1;
                query = new StringBuilder();
                query.append( SQL_INSERT );
                query.append( TABLE_ANALYTIC_HOUSE_CAPACITY );
                query.append( SQL_SET );
                query.append( "house_id=?, service_type=?, dt=?, value=?" );
                ps = con.prepareStatement( query.toString() );
                ps.setInt( index++, houseCapacityItem.getHouseId() );
                ps.setString( index++, houseCapacityItem.getServiceType() );
                ps.setDate( index++, TimeUtils.convertDateToSqlDate( houseCapacityItem.getDate() ) );
                ps.setInt( index++, houseCapacityItem.getValue() );
                ps.executeUpdate();
                ps.close();
            }
        }
    }

    public void deleteHouseCapacityItem( int houseId )
        throws SQLException
    {
        int index = 1;
        StringBuilder query = null;
        PreparedStatement ps = null;

        query = new StringBuilder();
        query.append( SQL_DELETE_FROM );
        query.append( TABLE_ANALYTIC_HOUSE_CAPACITY );
        query.append( SQL_WHERE );
        query.append( "house_id=?" );
        ps = con.prepareStatement( query.toString() );
        ps.setInt( index++, houseId );
        ps.executeUpdate();
        ps.close();
    }

    private void setHouseCapacityItemData( HouseCapacityItem houseCapacityItem, ResultSet rs )
        throws SQLException
    {
        houseCapacityItem.setHouseId( rs.getInt( "house_id" ) );
        houseCapacityItem.setServiceType( rs.getString( "service_type" ) );
        houseCapacityItem.setDate( rs.getDate( "dt" ) );
        houseCapacityItem.setValue( rs.getInt( "value" ) );
    }
}
