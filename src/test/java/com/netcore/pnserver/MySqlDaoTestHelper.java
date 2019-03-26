/**
 * 
 */
package com.netcore.pnserver;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Swapnil Srivastav
 *
 */
public class MySqlDaoTestHelper {
	
	private static ApplicationContext applicationContext = null;
	
	public void initializeAppContext(){
		applicationContext=
	    		new ClassPathXmlApplicationContext("applicationContextTest.xml");
	}
	
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	
	public JdbcTemplate getJdbcTemplate(String dbName){
		return (JdbcTemplate) applicationContext.getBean("test");
	}
	
	public int deletePublishSummary(int pushMsgId){
		initializeAppContext();
		
		try{
		String dbName = "test";
		String query = "Delete from "+dbName+".push_app_summary where msgid=?";
				     
		Object[] args = new Object[] {pushMsgId};
		JdbcTemplate jdbcTemplate = getJdbcTemplate(dbName);
		return jdbcTemplate.update(query, args);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return 0;	
	}
	
	public int setTokensForGuids(String guid,String token,String appid,String status,int userid,String table,String dbName){
		try{
		initializeAppContext();
		String query = new String("Insert into "+dbName+"."+table+" (guid,token,appid,status,userid)"+" values('"+guid+"','"+token+"','"+appid+"','"+status+"',"+userid+");");
		JdbcTemplate jdbcTemplate = getJdbcTemplate(dbName);
		int flag = jdbcTemplate.update(query);
		return flag;
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	public int setWebTokensForGuids(String guid,String token,String appid,String status,int userid,String table,String dbName){
		try{
		initializeAppContext();
		String query = new String("Insert into "+dbName+"."+table+" (guid,token,site,status,userid)"+" values('"+guid+"','"+token+"','"+appid+"','"+status+"',"+userid+");");
		JdbcTemplate jdbcTemplate = getJdbcTemplate(dbName);
		int flag = jdbcTemplate.update(query);
		return flag;
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	
	
	//For anon table
	public int setAnonTokensForGuids(String guid,String token,String appid,int userid,String table,String dbName){
		try{
		initializeAppContext();
		String query = new String("Insert into "+dbName+"."+table+" (guid,token,appid,userid)"+" values('"+guid+"','"+token+"','"+appid+"',"+userid+");");
		JdbcTemplate jdbcTemplate = getJdbcTemplate(dbName);
		int flag = jdbcTemplate.update(query);
		return flag;
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	
	public int deleteTokensforGuid(String token,String database,String table){
		try{
			initializeAppContext();
			String query = "Delete from "+database+"."+table+";";
			JdbcTemplate jdbcTemplate = getJdbcTemplate(database);
			int flag = jdbcTemplate.update(query);
			return flag;
			}catch(Exception e){
				e.printStackTrace();
			}
			return 0;
		
	}
}
