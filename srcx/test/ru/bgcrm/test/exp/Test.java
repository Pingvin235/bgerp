package ru.bgcrm.test.exp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Test {
    //Test

    public static void main(String[] args) throws Exception {
        //final TimeZone tzRemote = TimeZone.getTimeZone( "Europe/Moscow" );
        final TimeZone tzCurrent = TimeZone.getDefault();

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        format.setTimeZone(tzCurrent);

        Date date = format.parse("2014-12-09T12:10:38+03:00");

        System.out.println(date);

        /*Date movedDate = new Date( date.getTime() + tzRemote.getRawOffset() - tzCurrent.getRawOffset() ) ;
        
        System.out.println( movedDate );
        System.out.println( new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssXXX" ).format( movedDate ) );*/

        /*for( String zone : TimeZone.getAvailableIDs() )
        {
            System.out.println( zone );
        }*/

        //JexlEngine jexl = new JexlEngine();

        //System.out.println( jexl.isLenient() + " " + jexl.isSilent() + " " + jexl.isStrict() );

        /*jexl.setLenient( false );
        jexl.setSilent( false );*/
        //jexl.setStrict( true );

        //System.out.println( jexl.isLenient() + " " + jexl.isSilent() + " " + jexl.isStrict() );

        /*JexlContext context = new MapContext();
        context.set( "t", Test.class );
        
        Expression e = jexl.createExpression( "pvd = new('ru.bgcrm.dao.ParamValueDAO', con ); " );
        e.evaluate( context );*/

        /*
        UnifiedJEXL ujexl = new UnifiedJEXL( jexl );
        UnifiedJEXL.Expression expr = ujexl.parse( "${user} && ${user}" );
        */
        //System.out.println( e.evaluate( context ) );

        //new TabelDAO( null );

        /*final String orgName = "ОАО \"Уфанет\"";
        
        
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet( "Table" );
        
        //sheet.setco
        
        HSSFFont font10b = workbook.createFont();
        font10b.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font10b.setFontHeightInPoints( (short)10 );
        
        HSSFCellStyle style = null;
        
        // наименование организации
        HSSFRow row = sheet.createRow( 0 );
        Cell cell = row.createCell( 1 );
        
        style = workbook.createCellStyle();
        style.setFont(font10b);
        style.setAlignment( HSSFCellStyle.ALIGN_CENTER );
        style.setBorderBottom( HSSFCellStyle.BORDER_THIN );
        
        CellRangeAddress region = new CellRangeAddress( 0, 1, 1, 19 );
        for( int colNum = region.getFirstColumn() + 1; colNum <= region.getLastColumn(); colNum++ )
        {
           row.createCell( colNum ).setCellStyle( style );
        }
        sheet.addMergedRegion( region );
        
        cell.setCellStyle( style );
        cell.setCellValue( orgName );		
        
        FileOutputStream out = new FileOutputStream( new File( "/home/shamil/tmp/new.xls" ) );
        workbook.write( out );
        out.close();
        System.out.println( "Excel written successfully.." );*/

        /*Setup setup = Setup.getSetup( "bgcrm_ufanet", true );
        
        Connection con = setup.getDBConnectionFromPool();
        try
        {
            String query = "SELECT id FROM _customer_jur_incor_20130304";
            ResultSet rs = con.createStatement().executeQuery( query );
            
            SphinxDAO sphinxDao = new SphinxDAO( con );
            while( rs.next() )
            {
                sphinxDao.delete( rs.getInt( 1 ) );			
            }
        }
        catch( Exception e )
        {
            SQLUtils.closeConnection( con );
        }*/

        /*context.put( Process.OBJECT_TYPE, process );
        context.put( Process.OBJECT_TYPE + ParamValueFunction.PARAM_FUNCTION_SUFFIX, new ParamValueFunction( con, process.getId() ) );
        context.put( ProcessLinkFunction.PROCESS_LINK_FUNCTION, new ProcessLinkFunction( con, process.getId() ) );
        UtilsFunction.registerUtils( context );*/

        //System.out.println( new ru.bgcrm.dao.expression.Expression( context, context ).getString( "ut.maskNull( 'еуы ' + p.getId() )" ) );

        /*String config = 
            "db.driver=com.mysql.jdbc.Driver\n" +
            "db.url=jdbc:mysql://failover-bill-tks.core.ufanet.ru:3306/bgbilling?jdbcCompliantTruncation=false&useUnicode=true&characterEncoding=UTF-8\n" +
            //"db.url=jdbc:mysql://failover-bill-tks.core.ufanet.ru:3306/bgbilling?jdbcCompliantTruncation=false&useUnicode=true&characterEncoding=windows-1251\n" +
            "db.user=bgcrm\n" +
            "db.pswd=KIUwL8hF12OL7o7";
        
        ConnectionPool pool = new ConnectionPool( new Preferences( config ) );
        
        String query =
            "SELECT contract.id, contract.title, contract.comment, contract.status, contract.status_date, addr.address " +
            "FROM contract_parameter_type_2 AS addr " +
            "INNER JOIN contract ON contract.id=addr.cid AND contract.date2 IS NULL AND contract.del=0 " +
            "WHERE addr.pid=9 AND addr.hid=3992 AND addr.flat='21' AND addr.room='а'";
        
         //AND addr.room='а'
        
        Connection con = pool.getDBConnectionFromPool();
        
        PreparedStatement ps = con.prepareStatement( query );
        ps.setInt( 1, 9 );
        ps.setInt( 2, 3992 );
        ps.setString( 3, "21" );
        ps.setString( 4, "а" );
        
        ResultSet rs = ps.executeQuery();
        
        ResultSet rs = con.createStatement().executeQuery( query );
        if( rs.next() )
        {
            System.out.println( rs.getString( 1 ) + " " + rs.getString( 2 ) );
        }
        rs.close();
        
        con.close();*/

        /*JexlContext context = new JexlContext()
        {
            @Override
            public void set( String name, Object value )
            {}
        
            @Override
            public boolean has( String name )
            {
                return true;
            }
        
            @Override
            public Object get( String name )
            {
                return false;
            }
        };
        
        
        JexlEngine jexl = new JexlEngine();
        Expression e = jexl.createExpression( "user && user" );*/

        /*
        UnifiedJEXL ujexl = new UnifiedJEXL( jexl );
        UnifiedJEXL.Expression expr = ujexl.parse( "${user} && ${user}" );
        */
        //System.out.println( e.evaluate( context ) );

        /*	ScriptEngineManager factory = new ScriptEngineManager();
            ScriptEngine engine = factory.getEngineByName( "JavaScript" );
        
            ScriptContext context = new SimpleScriptContext()
            {
        
                @Override
                public Object getAttribute( String name )
                {
                    return super.getAttribute( name );
                }
        
                @Override
                public Object getAttribute( String name, int scope )
                {
                    return super.getAttribute( name, scope );
                }
                
            };
            
            engine.setContext( context );
            
            System.out.println( engine.eval( "foo OR (bar AND !baz)" ) );	
            
            System.out.println( "dd:dfdf:".replaceAll( "$", " ddd" ) );*/

        /*Setup setup = Setup.getSetup( "bgcrm", true );
        
        Connection con = setup.getDBConnectionFromPool();
        
        String query = "SHOW VARIABLES LIKE '%col%'";
        ResultSet rs = con.prepareStatement( query ).executeQuery();
        while( rs.next() )
        {
            System.out.println( rs.getString( 1 ) + " => " + rs.getString( 2 ) ); 
        }*/

        /*String var = "Иванов Иван Иванович";
        System.out.println( var.replaceAll( "([А-Я][а-я]+)\\s+([А-Я])[а-я]+\\s+([А-Я])[а-я]+", "$1 $2. $3." ) );*/

        /*String query = "SELECT CONCAT(house, frac), frac FROM address_house WHERE id=4";
        
        ResultSet rs = con.prepareStatement( query ).executeQuery();
        while( rs.next() )
        {
            System.out.println( rs.getString( 1 ) + " " + rs.getString( 2 ) ); 
        }*/

        /*String str1 = "Исламова Рамиля Замовна";
        String str2 = "Исламова Рамиля Замановна";
        
        long time = System.currentTimeMillis();
        
        System.out.println( LevenshteinDistance.computeLevenshteinDistance( str1, str2 ) );*/

        //System.out.println( "+7 927-943-33-38 [];+7 927-943-33-38 [];".replaceAll( "[^\\d;]", "" ) ); 	

        //System.out.println( System.currentTimeMillis() - time );

        /*
        String user = "shamil";
        String pswd = "cnf,bkbnhjy";
        
        // Set up environment for creating initial context
        Hashtable env = new Hashtable();
        env.put( Context.SECURITY_AUTHENTICATION, "simple" );
        env.put( Context.SECURITY_PRINCIPAL, user + "@core.ufanet.ru" );
        env.put( Context.SECURITY_CREDENTIALS, pswd );
        //
        env.put( Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory" );
        // Connect to my domain controller
        env.put( Context.PROVIDER_URL, "ldap://core.ufanet.ru:389" );
                
        try
        {
            // Create the initial directory context
            DirContext ctx = new InitialDirContext( env );
        
            // Create the search controls 		
            SearchControls searchCtls = new SearchControls();
        
            //Specify the search scope
            searchCtls.setSearchScope( SearchControls.SUBTREE_SCOPE );
            
            //Specify the attributes to return
            //searchCtls.setReturningAttributes( new String[]{ "name", "memberOf", "objectSid" } );
        
            //specify the LDAP search filter
            String searchFilter = "(&(objectClass=user)(sAMAccountName=" + user + "))";
        
            //Specify the Base for the search
            String searchBase = "CN=Users,DC=core,DC=ufanet,DC=ru";
            //initialize counter to total the results
            int totalResults = 0;
        
            // Search for objects using the filter
            NamingEnumeration answer = ctx.search( searchBase, searchFilter, searchCtls );
        
            //Loop through the search results
            while( answer.hasMoreElements() )
            {
                SearchResult sr = (SearchResult)answer.next();
        
                totalResults++;
        
                Attributes attrs = sr.getAttributes();
                System.out.println( attrs.get( "memberOf" ) );
        
                System.out.println( ">>>" + sr.getName() + " " );
        
            }
        
            System.out.println( "Total results: " + totalResults );
        
            ctx.close();
        }
        catch( NamingException e )
        {
            e.printStackTrace();
        }
        */

        /*for( int i = 0; i < 1000; i++ )
        {
            System.out.println( System.currentTimeMillis() );
        }
        */

        /*System.out.println( new String[]{} instanceof Object[] );
        
        Locale[] locales = new Locale[] {
                Locale.US,
                new Locale( "RU", "ru" ) };
        
        // Get an instance of current date time
        Date today = new Date();
        
        //System.out.println( TimeUtils.format( today, TimeUtils.FORMAT_TYPE_SQL_YMD ) );
        System.out.println( new SimpleDateFormat( "''yyyy-MM-dd''" ).format( today ) );*/

        //
        // Iterates the entire Locale defined above and create a long 
        // formatted date using the SimpleDateFormat.getDateInstance() 
        // with the format, the Locale and the date information.
        //
        /*    for( Locale locale : locales ) 
            {
                    SimpleDateFormat.getDateInstance( SimpleDateFormat.LONG, locale)
                              .format(today).toUpperCase());
            }*/

        /*Response resp = new Response();
        resp.setMessage( "test" );
        resp.setData( "customer", new Customer() );
        resp.addEvent( new CustomerTitleChangedEvent( 1, "sdfghggh" ) );
        
        
        ObjectMapper mapper = new ObjectMapper();
        try 
        {
            mapper.setFilters( filterProvider )
            mapper.writeValue( System.out, resp );
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }*/

        //CollectionUtils.co

        /*String pattern = "+(${part1})([${part2}])(${part3})( ${comment});";
        
        String val = PatternFormatter.insertPatternPart( pattern, "part1", "7" );
        val = PatternFormatter.insertPatternPart( val, "part2", "347" );
        val = PatternFormatter.insertPatternPart( val, "part3", "2924823" );
        val = PatternFormatter.insertPatternPart( val, "comment", "домашний" );
        
        System.out.println( val.toString() );*/

        //System.out.println( Utils.toTranslit( "Тестовый документ.txt" ) );

        /*for( VersionInfo vi :  VersionInfo.getInstalledVersions() )
        {
            System.out.println( vi.getModuleName() + ":" + vi.getBuildNumber() );
        }*/

        /*try 
        {
            JSONObject jsonEvent = new JSONObject();
            jsonEvent.put( "event" , "CustomerTitleChanged" );
            jsonEvent.put( "id", 1 );
            jsonEvent.put( "title", "dddd" );
            
            System.out.println( jsonEvent.toString() );
        } 
        catch( Exception e )
        {
            e.printStackTrace();
        }*/
    }
    /*
        private static String toDC( String domainName )
        {
            StringBuilder buf = new StringBuilder();
            for( String token : domainName.split( "\\." ) )
            {
                if( token.length() == 0 ) continue; // defensive check
                if( buf.length() > 0 ) buf.append( "," );
                buf.append( "DC=" ).append( token );
            }
            return buf.toString();
        }*/
}
