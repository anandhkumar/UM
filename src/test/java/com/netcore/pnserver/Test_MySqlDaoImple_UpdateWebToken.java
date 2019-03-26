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
import com.netcore.pnserver.util.Util;

/**
 * @author Swapnil Srivastav
 *
 */
public class Test_MySqlDaoImple_UpdateWebToken {
	
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
	

	@Test
	public void testUpdateAppToken(){
		int flag = mySqlDaoImplObj.updateWebToken(1, "fOiaNPqxA60:APA91bG5bn4M0MmX6SBRZWLA2n0QtH3hEikl9c8BmtUteI4AuwlLALTae5YiFZz2CQHGRkPQWRwI9tnMmk405PYVAetYy7wyB8XeivMxN41YwdTtM0Oh2EGWFJiq7K449GPYne5aGHSt", "New Token", 1,"", Util.BROWSER_TYPE_CHROME);
		assertEquals(1, flag);
		
		
	}
	
	@AfterClass
	public static void destroy(){
		int undoflag = mySqlDaoImplObj.updateWebToken(1, "New Token", "fOiaNPqxA60:APA91bG5bn4M0MmX6SBRZWLA2n0QtH3hEikl9c8BmtUteI4AuwlLALTae5YiFZz2CQHGRkPQWRwI9tnMmk405PYVAetYy7wyB8XeivMxN41YwdTtM0Oh2EGWFJiq7K449GPYne5aGHSt", 1,"", Util.BROWSER_TYPE_CHROME);
		assertEquals(1, undoflag);
		
		
		
	}
	
}
