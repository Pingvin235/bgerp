package ru.bgcrm.util.distr;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.ConnectionPool;
import ru.bgcrm.util.sql.SQLUtils;

@Ignore
public class InstallerModuleTest
{
	private static final String TEST_CONFIG = "test.bgcrm_test";
	private Setup setup;
	
	private static class InstallerModule extends ru.bgcrm.util.distr.InstallerModule
	{
		InstallerModule() {
            super(null, null);
        }

        @Override
		public ModuleInf getModuleInf( File zip )
		{
			return super.getModuleInf( zip );
		}

		@Override
		public boolean copyFiles( File zip, ModuleInf mi )
		{
			return super.copyFiles( zip, mi );
		}

		@Override
		public void executeCalls( ModuleInf mi, Setup setup, File zip )
		{
			super.executeCalls( mi, setup, zip );
		}
	}
	
	@Before
	public void init()
		throws Exception
	{
        setup = new Setup(TEST_CONFIG, false);
		
		Connection con = new ConnectionPool( "TEST", setup ).getDBConnectionFromPool();
		
		ResultSet rs = con.createStatement().executeQuery( "SELECT DATABASE()" );
		assertTrue( rs.next() );
		
		String database = rs.getString( 1 );
		
		Statement st = con.createStatement();
		
		st.executeUpdate( "DROP DATABASE " + database );
		st.executeUpdate( "CREATE DATABASE " + database );
		st.executeQuery( "USE " + database );
		st.executeUpdate( "CREATE TABLE `n_config_global` ( " +
    		  "`id` int(11) NOT NULL AUTO_INCREMENT," +
    		  "`active` tinyint(1) NOT NULL," +
    		  "`title` varchar(255) NOT NULL," +
    		  "`data` longtext," +
    		  "`dt` datetime NOT NULL," +
    		  "`user_id` int(11) NOT NULL," +
    		  "`last_modify_user_id` int(11) NOT NULL," +
    		  "`last_modify_dt` datetime NOT NULL," +
    		  "PRIMARY KEY (`id`))" );
		
		SQLUtils.closeConnection( con );
		
		setup = Setup.getSetup( TEST_CONFIG, true );
		
		con = setup.getDBConnectionFromPool();
		
		con.createStatement().executeUpdate( "DROP TABLE n_config_global" );
		
		SQLUtils.closeConnection( con );
	}
	
	@Test
	public void testParse() 
	{
		File dir = new File("build/update");
		FileFilter fileFilter = new WildcardFileFilter("update*.zip");
		File[] files = dir.listFiles(fileFilter);
		
		assertTrue( "update_..zip not found", files.length > 0 );
		
		File updateFile = files[0];
		InstallerModule installer = new InstallerModule();
		
		final ModuleInf moduleInf = installer.getModuleInf( updateFile );
		
		assertNotNull( "module inf hasn't extracted", moduleInf );
		
		installer.executeCalls( moduleInf, setup, updateFile );
	}
}
