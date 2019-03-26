package com.netcore.pnserver;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
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
public class Test_MySqlDaoImpl_GetAppTokensForGuids {
	
	private static MySqlDaoImpl mySqlDaoImplObj;
	private static ApplicationContext applicationContext = null;
	private static PushNotification pn1=null;
	private static PushNotification pn2=null;
	private static MySqlDaoTestHelper mySqlDaoTestHelper = null;
	private static List<PushNotification> pnList1;
	private static List<PushNotification> pnList2;
	private static int flag = 0;
	private static List<String> values;
	private static String guid,token,appid,status,table,dbName;
	private static int userId;
	private static PushNotification pn;
	
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
		flag = mySqlDaoTestHelper.setTokensForGuids(guid, token, appid, status, userId, table, dbName);
		
		pn = new PushNotification(userId, guid,0,0,0,"",0);
		pnList1 = new ArrayList<PushNotification>();
		pnList1.add(pn);
		pnList2 = new ArrayList<PushNotification>();
	}
	
	
	@Test
	public void testGetAppTokensForGuids() {
		if(flag==1){
		pnList2 = mySqlDaoImplObj.getAppTokensForGuids(1, pnList1, "android", 1,"");
		PushNotification pn1 = pnList2.get(0);
		assertEquals(pn1, pn);
		}		
	}
	
	@AfterClass
	public static void destroy(){
		int flag = mySqlDaoTestHelper.deleteTokensforGuid(token, dbName, table);
		assertEquals(flag, 1);
	}

}
