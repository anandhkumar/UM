/**
 * 
 */
package com.netcore.pnserver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.netcore.pnserver.dao.MySqlDaoImpl;
import com.netcore.pnserver.pojo.PushNotification;

/**
 * @author Swapnil Srivastav
 *
 */
public class Test_MySqlDaoImpl_GetAppTokenForGuid {
	
	private static MySqlDaoImpl mySqlDaoImplObj;
	private static ApplicationContext applicationContext = null;
	private PushNotification pn=null;
	private static MySqlDaoTestHelper mySqlDaoTestHelper = null;
	private List<PushNotification> pns;
	private static String guid,token,appid,status,table,anonTable,dbName;
	private static int userId;
	
	@BeforeClass
	public static void before(){
		
		 applicationContext =
	    		new ClassPathXmlApplicationContext("applicationContextTest.xml");
		mySqlDaoImplObj = (MySqlDaoImpl) applicationContext.getBean("mysqlDaoTest");
		
		mySqlDaoTestHelper = (MySqlDaoTestHelper) applicationContext.getBean("mySqlDaoTestHelper");
		
		guid = "testGuid";
		 token = "testToken";
		 appid = "testAppid";
		 status = "registered";
		 userId = 111;
		 table = "app_token_map_android";
		 dbName = "test";
		 anonTable = "anon_app_token_map_android";
		mySqlDaoTestHelper.setTokensForGuids(guid, token, appid, status, userId, table, dbName);
		mySqlDaoTestHelper.setAnonTokensForGuids(guid, token, appid,userId, anonTable, dbName);
	}

	@Test
	public void testGetAppTokenForGuid() {
		
		String appToken = mySqlDaoImplObj.getAppTokenForGuid(1,1234, guid, "android", 1);
		assertEquals(token, appToken);
	/*	assertNotNull(mySqlDaoImplObj.getAppTokenForGuid(1, "105a9e67-7fd0-4635-9617-349cd4dc8d4b", "android", 1));
		
		String anonAppToken = mySqlDaoImplObj.getAppTokenForGuid(1, "0c9bf743-793f-4a81-b0ed-0a6f8cd6e3ba", "android", 0);
		assertEquals("cx0FFQJWafo:APA91bGyDKToBVJvBGDiQuIM1SBIHFxHLRCimGOl3E-eYa9cOlEZOzZu30S6EEQtq_dZz_p-KS_zXs62lTqIV_QOZIus1SchzpsoM9zgiRCJW7KmnT0FMO1rNSMnckDpFqllFQEVVrvy",anonAppToken);
		assertNotNull(mySqlDaoImplObj.getAppTokenForGuid(1, "0c9bf743-793f-4a81-b0ed-0a6f8cd6e3ba", "android", 0));
		*/
		String anon_token = mySqlDaoImplObj.getAppTokenForGuid(1,2345, guid, "android", 0);
		assertEquals(token, anon_token);
	}
	
	@AfterClass
	public static void destroy(){
		mySqlDaoTestHelper.deleteTokensforGuid(token, dbName, table);
		mySqlDaoTestHelper.deleteTokensforGuid(token, dbName, anonTable);
		
	}
}
