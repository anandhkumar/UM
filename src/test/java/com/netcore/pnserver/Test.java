/**
 * 
 */
package com.netcore.pnserver;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.netcore.pnserver.dao.MySqlDaoImpl;

/**
 * @author Swapnil Srivastav
 *
 */
public class Test implements ApplicationContextAware {
	
	public static void main(String[] args) {
		
		
		ApplicationContext context =
	    		new ClassPathXmlApplicationContext("applicationContextTest.xml");
		MySqlDaoImpl mySqlDaoImplObj = (MySqlDaoImpl) context.getBean("mysqlDaoTest");
		if(mySqlDaoImplObj.equals(null)){
			System.out.println("Null object");
			
		}
		System.out.println(mySqlDaoImplObj);
		//mySqlDaoImplObj.getAppTokenForGuid(0, null, null, 0);
		String token = mySqlDaoImplObj.getAppTokenForGuid(1, 1234,"105a9e67-7fd0-4635-9617-349cd4dc8d4b", "android", 1);
		System.out.println(token);
		
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		// TODO Auto-generated method stub
		
	}

}
