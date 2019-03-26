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
public class Test_MySqlDaoImpl_UpdateAppToken {
	
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
		int flag = mySqlDaoImplObj.updateAppToken(1, "dmblSk8gPHk:APA91bHUd_6Tc2SgqItpkkq1VF7LJvmicK9oSGnTiUuuqNIwV83srTfeRg3KaWWhuw0O3fqpIU3PFwInU2qG3TBhxayYVsbkzIe6vVa0Tmmd_E_UioH0IGK1UEP1Z9pnXKRzvCcIUgX4", "New Token", 1,"", Util.OS_TYPE_ANDROID);
		//assertEquals(1, flag);
		
		
	}
	
	@AfterClass
	public static void destroy(){
		int undoflag = mySqlDaoImplObj.updateAppToken(1, "New Token", "dmblSk8gPHk:APA91bHUd_6Tc2SgqItpkkq1VF7LJvmicK9oSGnTiUuuqNIwV83srTfeRg3KaWWhuw0O3fqpIU3PFwInU2qG3TBhxayYVsbkzIe6vVa0Tmmd_E_UioH0IGK1UEP1Z9pnXKRzvCcIUgX4", 1,"", Util.OS_TYPE_ANDROID);
		//assertEquals(1, undoflag);
		
		
		
	}
	
}
