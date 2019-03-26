package com.netcore.pnserver;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import com.netcore.pnserver.dao.*;
import com.netcore.pnserver.pojo.PushNotification;
import com.netcore.pnserver.util.ClientDBMap;
import com.netcore.pnserver.util.Util;

/**
 * @author Swapnil Srivastav
 *
 */
public class TEST_MySQLDaoImpl {
	
	private static MySqlDaoImpl mySqlDaoImplObj;
	private static ApplicationContext applicationContext = null;
	private PushNotification pn=null;
	private static MySqlDaoTestHelper mySqlDaoTestHelper = null;
	private List<PushNotification> pns;
	
	@BeforeClass
	public static void before(){
		
		 applicationContext =
	    		new ClassPathXmlApplicationContext("applicationContextTest.xml");
		mySqlDaoImplObj = (MySqlDaoImpl) applicationContext.getBean("mysqlDaoTest");
		
		mySqlDaoTestHelper = (MySqlDaoTestHelper) applicationContext.getBean("mySqlDaoTestHelper");
		
	}
	/**
	 * @author Swapnil Srivastav
	 * This method covers testing of getTokenForGuid(),getDB() method also.
	 */
	@Test
	public void testGetAppTokenForGuid() {
		//int clientId, int userId, String pushid, String osType, int identifiedFlag
		String appToken = mySqlDaoImplObj.getAppTokenForGuid(1, 1234,"105a9e67-7fd0-4635-9617-349cd4dc8d4b", "android", 1);
		assertEquals("dmblSk8gPHk:APA91bHUd_6Tc2SgqItpkkq1VF7LJvmicK9oSGnTiUuuqNIwV83srTfeRg3KaWWhuw0O3fqpIU3PFwInU2qG3TBhxayYVsbkzIe6vVa0Tmmd_E_UioH0IGK1UEP1Z9pnXKRzvCcIUgX4", appToken);
		// (int clientId, int userId, String pushid, String osType, int identifiedFlag)
		assertNotNull(mySqlDaoImplObj.getAppTokenForGuid(1, 1234,"105a9e67-7fd0-4635-9617-349cd4dc8d4b", "android", 1));
		
		String anonAppToken = mySqlDaoImplObj.getAppTokenForGuid(1,45445, "0c9bf743-793f-4a81-b0ed-0a6f8cd6e3ba", "android", 0);
		assertEquals("cx0FFQJWafo:APA91bGyDKToBVJvBGDiQuIM1SBIHFxHLRCimGOl3E-eYa9cOlEZOzZu30S6EEQtq_dZz_p-KS_zXs62lTqIV_QOZIus1SchzpsoM9zgiRCJW7KmnT0FMO1rNSMnckDpFqllFQEVVrvy",anonAppToken);
		assertNotNull(mySqlDaoImplObj.getAppTokenForGuid(1, 1234,"0c9bf743-793f-4a81-b0ed-0a6f8cd6e3ba", "android", 0));
		//int clientId, int userId, String pushid, String browserType, int identifiedFlag
		String webToken = mySqlDaoImplObj.getWebTokenForGuid(1,0, "", "", 1);
	}
	
	@Test
	public void testUpdateAppToken(){
		int flag = mySqlDaoImplObj.updateAppToken(1, "dmblSk8gPHk:APA91bHUd_6Tc2SgqItpkkq1VF7LJvmicK9oSGnTiUuuqNIwV83srTfeRg3KaWWhuw0O3fqpIU3PFwInU2qG3TBhxayYVsbkzIe6vVa0Tmmd_E_UioH0IGK1UEP1Z9pnXKRzvCcIUgX4", "New Token", 1,"", Util.OS_TYPE_ANDROID);
		assertEquals(1, flag);
		
		
	}
	
	
	
	@Test
	public void testGetAppTokensForGuids(){
		//pn = new PushNotification(userid, pushid);
		
	}
	
	@Test
	public void testPublishSummary(){
	//	assertEquals(1, mySqlDaoImplObj.publishSummary(1, 1, 1, 0,""));
		
	}
	
	
	@AfterClass
	public static void destroy(){
		int undoflag = mySqlDaoImplObj.updateAppToken(1, "New Token", "dmblSk8gPHk:APA91bHUd_6Tc2SgqItpkkq1VF7LJvmicK9oSGnTiUuuqNIwV83srTfeRg3KaWWhuw0O3fqpIU3PFwInU2qG3TBhxayYVsbkzIe6vVa0Tmmd_E_UioH0IGK1UEP1Z9pnXKRzvCcIUgX4", 1,"", Util.OS_TYPE_ANDROID);
		assertEquals(1, undoflag);
		
		mySqlDaoTestHelper.deletePublishSummary(1);
		
	}
	
}
