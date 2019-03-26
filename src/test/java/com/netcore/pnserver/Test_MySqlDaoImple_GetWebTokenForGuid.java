/**
 * 
 */
package com.netcore.pnserver;

import static org.junit.Assert.assertEquals;

import java.util.List;

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
public class Test_MySqlDaoImple_GetWebTokenForGuid {
	
	private static MySqlDaoImpl mySqlDaoImplObj;
	private static ApplicationContext applicationContext = null;
	private PushNotification pn=null;
	private static MySqlDaoTestHelper mySqlDaoTestHelper = null;
	private List<PushNotification> pns;
	private static String guid,token,appid,status,table,anonTable,dbName,pushid;
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
		 pushid = "4d2f871e-bbb8-4370-8a64-2a8ea22541c0";
	}

	@Test
	public void testGetAppTokenForGuid() {
			
		String webToken = mySqlDaoImplObj.getWebTokenForGuid(1, 456,pushid, "chrome", 1);
		assertEquals(webToken, "fOiaNPqxA60:APA91bG5bn4M0MmX6SBRZWLA2n0QtH3hEikl9c8BmtUteI4AuwlLALTae5YiFZz2CQHGRkPQWRwI9tnMmk405PYVAetYy7wyB8XeivMxN41YwdTtM0Oh2EGWFJiq7K449GPYne5aGHSt");
	}
}
