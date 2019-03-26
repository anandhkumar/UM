/**
 * 
 */
package com.netcore.pnserver;

import static org.junit.Assert.assertEquals;

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
public class Test_MySqlDaoImple_PublishSummary {

	private static MySqlDaoImpl mySqlDaoImplObj;
	private static ApplicationContext applicationContext = null;
	private PushNotification pn=null;
	private static MySqlDaoTestHelper mySqlDaoTestHelper = null;
	
	@BeforeClass
	public static void before(){
		
		 applicationContext =
	    		new ClassPathXmlApplicationContext("applicationContextTest.xml");
		mySqlDaoImplObj = (MySqlDaoImpl) applicationContext.getBean("mysqlDaoTest");
		
		mySqlDaoTestHelper = (MySqlDaoTestHelper) applicationContext.getBean("mySqlDaoTestHelper");
		
	}
	
	@Test
	public void testPublishSummary(){
		//assertEquals(1, mySqlDaoImplObj.publishSummary(1, 1, 1, 0,"app"));
	}
	
	@AfterClass
	public static void destroy(){
			mySqlDaoTestHelper.deletePublishSummary(1);
		
	}
	
}
