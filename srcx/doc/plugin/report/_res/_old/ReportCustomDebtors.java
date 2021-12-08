package ru.bgcrm.dyn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.util.Utils;

public class ReportCustomDebtors
{
    private static final Logger log = Logger.getLogger(ReportCustomDebtors.class);
    
    private String billingId = "test";  // идентификатор биллинга, значение из конфига bgbilling.X.id
    private int adressParamId = 19;     // код параметра договора типа адрес
    private int nameParamId = 51;       // код параметра договора типа строка( имя абонента )
    private int cityId = 1;             // код города
    
    private String tableBalance = "temp_balance";
    private String tableGroupName = "temp_contract_group_select";
    
    // Предполагалось, что объект будет жить и можно будет переиспользовать его поля, но пока это не так, так как нельзя указать scope.
    private List<IdTitle> streetList = null;
    private Map<Integer, String> streetMap = null;
    private List<IdTitle> groups = null;
    private List<IdTitle> statuses = null;
    private List<IdTitle> sorts = null;
    private Map<Integer, IdTitle> sortMap = null;

    public String getBillingId()
    {
        return billingId;
    }
    
    private Connection getConnection() throws BGException
    {
        DBInfo dbInfo = DBInfoManager.getInstance().getDbInfoMap().get( billingId );
        if ( dbInfo == null )
        {
            throw new BGException( "Не найден биллинг: " + billingId );
        }
        
        Connection con = dbInfo.getConnectionPool().getConnectionSet().getConnection();
        if( con == null )
        {
            throw new BGException( "Connection is NULL ! Billing - " + billingId );
        }
        return con;
    }
    
    public List<IdTitle> getStreetList() throws SQLException, BGException
    {
        if( streetList == null )
        {
//            System.out.println( "Intit Streets!"  );
            streetList = new ArrayList<>();
            try( Connection con = getConnection() ) 
            {
                try( PreparedStatement ps = con.prepareStatement( "SELECT id,title FROM address_street WHERE cityid=" + cityId + " ORDER BY title" ) ) 
                {
                    ResultSet rs = ps.executeQuery();
                    while( rs.next() )
                    {
                        streetList.add( new IdTitle( rs.getInt( 1 ), rs.getString( 2 ) ) );
                    }
                }
            }
        }
        return streetList;
    }
    
    public List<IdTitle> getGroups() throws SQLException, BGException
    {
        if( groups == null )
        {
            groups = new ArrayList<>();
            try( Connection con = getConnection() ) 
            {
                try( PreparedStatement ps = con.prepareStatement( "SELECT id,title FROM contract_group WHERE enable=1" ) ) 
                {
                    ResultSet rs = ps.executeQuery();
                    while( rs.next() )
                    {
                        groups.add( new IdTitle( rs.getInt( 1 ), rs.getString( 2 ) ) );
                    }
                }
            }
        }
        return groups;
    }
    
    public List<IdTitle> getStatus() throws SQLException, BGException
    {
        if( statuses == null )
        {
            statuses = new ArrayList<>();
            statuses.add( new IdTitle(0,"Активен") );
            statuses.add( new IdTitle(1,"В отключении") );
            statuses.add( new IdTitle(2,"Отключен_за_долги") );
            statuses.add( new IdTitle(3,"Закрыт") );
            statuses.add( new IdTitle(4,"Приостановлен") );
            statuses.add( new IdTitle(5,"В подключении") );
        }
        return statuses;
    }
    
    public List<IdTitle> getSortList() throws SQLException, BGException
    {
        if( sorts == null )
        {
            sorts = new ArrayList<>();
            sorts.add( new IdTitle( 1, "Улица" ) );
            sorts.add( new IdTitle( 2, "Дом" ) );
            sorts.add( new IdTitle( 3, "Квартира" ) );
            sorts.add( new IdTitle( 4, "Баланс" ) );
        }
        return sorts;
    } 
    
    public Map<Integer, IdTitle> getSortMap() throws SQLException, BGException
    {
        if( sortMap == null )
        {
            sortMap = new HashMap<Integer, IdTitle>();
            for( IdTitle item : getSortList() )
            {
                sortMap.put( item.getId(), item );
            }
        }
        return sortMap;
    }
    
    //////////   Отчет   ///////////////
    
    public List<String[]> getDebters( Set<Integer> groups, Set<Integer> statuses, int streetId, String homeNumber, String balanceFromStr, String balanceToStr, List<Integer> sort ) throws BGException 
    {
        List<String[]> result = new ArrayList<String[]>();
        try( Connection con = getConnection() ) 
        {
            if( streetMap == null )
            {
                streetMap = new HashMap<>();
                for( IdTitle idTitle : getStreetList() )
                {
                    streetMap.put( idTitle.getId(), idTitle.getTitle() );
                }
            }

            dropTables( con );
            
            long gr = getGroupMask( groups );
            String status = Utils.toString( statuses );
            int balanceFrom = Utils.parseInt( balanceFromStr, Integer.MIN_VALUE );
            int balanceTo = Utils.parseInt( balanceToStr, Integer.MAX_VALUE );
            String balance = " IF( temp_balance.mm IS NULL, 0, ( b.summa1 + b.summa2 - b.summa3 - b.summa4) ) ";
//            
            // Предназначение временной таблицы дабы дважды не делать выборку по группе. А так же раз выборка уже производится, то пихаю туда еще и адрес
            String query = "CREATE TEMPORARY TABLE " + tableGroupName + " (UNIQUE(contractId)) SELECT c.id AS contractId, ah.streetid AS streetId, concat(ah.house,ah.frac) AS homeN, cpa.flat AS flat FROM contract AS c ";
            query += " LEFT JOIN contract_parameter_type_2 AS cpa ON cpa.cid=c.id AND cpa.pid=" + adressParamId
            + ( streetId > 0 || Utils.notBlankString( homeNumber ) ? " INNER" : " LEFT" ); // INNER если обязателен
            query += " JOIN address_house AS ah ON ah.id=cpa.hid"; 
            if( streetId > 0 )
            {
                query += " AND ah.streetid=" + streetId;
            }
            query += " WHERE 1=1";
            if( gr > 0 )// если группы нет, то все.
            {
                query += " AND c.gr&" + gr + ">0 ";
            }
            if ( Utils.notBlankString( status ) )
            {
                query += " AND c.status IN ( " + status + " )";
            }
            if( Utils.notBlankString( homeNumber ) )
            {
                query += " AND concat(ah.house,ah.frac) LIKE ?";
            }
            PreparedStatement psg = con.prepareStatement( query );
            if( Utils.notBlankString( homeNumber ) )
                psg.setString( 1, homeNumber );
            psg.executeUpdate();
            
            // Создание временной таблицы содержащей максимальный месяц где был баланс. Для расчета баланса договора в основной выборке.
            String queryCreate = "CREATE TEMPORARY TABLE " + tableBalance + " ( UNIQUE(cid) ) SELECT cid, MAX(yy*12+(mm-1))%12 + 1 AS mm, FLOOR(MAX(yy*12+(mm-1)) / 12) AS yy FROM contract_balance";
            queryCreate += " RIGHT JOIN " + tableGroupName + " AS tgn ON tgn.contractId=cid GROUP BY cid";
            con.createStatement().executeUpdate( queryCreate );
            
         // Основная выборка
            query = "SELECT c.id, tgn.flat, tgn.homeN, tgn.streetId, cpS.val AS name, " + balance + "as balance, c.title as title, c.status FROM contract c";
            query += " LEFT JOIN contract_parameter_type_1 AS cpS ON cpS.cid=c.id AND cpS.pid=" + nameParamId
                     + " LEFT JOIN temp_balance ON temp_balance.cid=c.id"
                     + " LEFT JOIN contract_balance AS b ON b.cid=c.id AND b.mm = temp_balance.mm AND b.yy = temp_balance.yy"
                     + " RIGHT JOIN " + tableGroupName + " AS tgn ON tgn.contractId=c.id WHERE 1=1";

            if( balanceFrom != Integer.MIN_VALUE && balanceTo != Integer.MAX_VALUE )
            {
                if( balanceTo < 0 && balanceFrom > balanceTo )// меняем местами, чтобы смысл не ломался
                    query += " AND" + balance + "BETWEEN " + balanceTo + " AND " + balanceFrom;
                else
                    query += " AND" + balance + "BETWEEN " + balanceFrom + " AND " + balanceTo;
            }
            else
            {
                if( balanceFrom != Integer.MIN_VALUE )
                {
                    query += " AND" + balance + ">= " + balanceFrom;
                }
                if( balanceTo != Integer.MAX_VALUE )
                {
                    query += " AND " + balance + "<=" + balanceTo;
                }
            }

            query += " GROUP BY c.id";
            if( sort != null && sort.size() > 0 )
            {
                query += " ORDER BY " + getOrder( sort );
            }
            // лимит на всякий случай.
            query += " LIMIT 1000";
            
            try ( PreparedStatement ps = con.prepareStatement( query ) )
            {
                ResultSet rs = ps.executeQuery();
                int n = 0;
                while( rs.next() )
                {
                    n++;

                    String address = "";
                    int strId = rs.getInt( "streetId" );
                    if( strId > 0 )
                    {                        
                        String streetTitle = Utils.maskNull( streetMap.get( strId ) );
                        address = streetTitle + ", " + rs.getString( "homeN" ) + ", " + rs.getString( "flat" );
                    }

                    String cid = rs.getString( 1 );
                    String title = rs.getString( "title" );
                    String name = Utils.maskNull( rs.getString( "name" ) );
                    
                    String balanceContract = rs.getBigDecimal( "balance" ).toPlainString();
                    
                    result.add( new String[] { n + "", cid, title, name, address, balanceContract } );
                }
            }
        } 
        catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        return result;      
    }
    
    private String getOrder( List<Integer> sort )
    {
        String result = "";
        for( int i = 0; i < sort.size(); i++ )
        {
            String value = null;
            int order = sort.get( i );
            if( order == 1 ) // Улица
                value = " streetid";
            if( order == 2 ) // Дом
                value = " homeN*1";
            if( order == 3 ) // Квартира
                value = " flat*1";
            if( order == 4 ) // Баланс
                value = " balance"; // DESC

            if( value != null )
            {
                result += (i > 0 ? "," : "") + value;
            }
        }
        return result;
    }
    
    private long getGroupMask( Set<Integer> groups )
    {
        long result = 0L;
        if( groups != null && groups.size() > 0 )
        {
            for( int id : groups )
            {
                if( id >= 0 )
                    result |= 1L << id;
            }
        }
        return result;
    }
    
    private void dropTables( Connection con ) throws SQLException
    {
        con.createStatement().executeUpdate( "DROP TEMPORARY TABLE IF EXISTS " + tableGroupName );
        con.createStatement().executeUpdate( "DROP TEMPORARY TABLE IF EXISTS " + tableBalance );
    }

}
