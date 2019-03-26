/**
 * 
 */
package com.netcore.pnserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.netcore.pnserver.dao.MySqlDao;
import com.netcore.pnserver.dao.MySqlDaoImpl;
import com.netcore.pnserver.gateway.APNSConnector;
import com.netcore.pnserver.pojo.Content;
import com.netcore.pnserver.pojo.Message;
import com.netcore.pnserver.pojo.Msgbody;
import com.netcore.pnserver.pojo.PushNotification;
import com.netcore.pnserver.pojo.PushNotificationRequest;

/**
 * @author Swapnil Srivastav
 *
 */
public class Test_APNSConnector_SendSingleMessage {
	
	
	private static MySqlDaoImpl mySqlDaoImplObj;
	private static ApplicationContext applicationContext = null;
	private static APNSConnector apnsConnector=null;
	private static MySqlDao mysqlDao;
	private static PushNotification pn,pnR;
	private PushNotificationRequest pnr;
	private Message message;
	private Content contentObj;
	private Msgbody msgbody2;
	int userid;
	int identifiedFlag;
	int ttl;
	int cid;
	int msgid;
	List<Map<String, String>> actions;
	
	String guid,appid,msgBody,identity,trid,content, imageUrl,deeplink,msgText,title,os,pushid,eventType,icon;
	@BeforeClass
	public static void setup(){
		applicationContext =
				new ClassPathXmlApplicationContext("applicationContextTest.xml");
		apnsConnector = 	(APNSConnector) applicationContext.getBean("10");	
	}
	
	@Before
	public void initialize(){
		guid="C71846BF-A71E-4CEB-B54F-D8962BE98371";
		appid ="2714dd99a4b547c56ec8c276bd4a5e87";
		userid = 8;
		ttl = 16062812;
		identity="";trid="15260-116-8-72-160628145501";
		imageUrl="";deeplink="http://www.jabong.com";msgText="Text Msg";title="Test Title";
		icon = "https://image.flaticon.com/sprites/new_packs/148705-essential-collection.png";
		os="10";identifiedFlag=1;msgid=116;cid=15260;eventType="app";

		contentObj = new Content(msgText, title, deeplink, imageUrl,icon, null, null);
		
		message = new Message(trid, identity, contentObj);
		
		msgbody2 = new Msgbody(trid, identity, contentObj);
		actions = new ArrayList<>();
		String siteid="", browser="",url="";
		//public PushNotificationRequest(String url, Message message, String os,int cid, int uid, String pushid, String appid, int msgid, int ttl,
		//Msgbody msgbody,int identified,String siteid, String browser,List<Map<String, String>> actions, boolean autohide) {
		pnr = new PushNotificationRequest(url, message, os, cid, userid, guid, appid, msgid, ttl, msgbody2, identifiedFlag, siteid, browser,actions,true,null, null,0,0);
		
		pn = new PushNotification(userid,guid,cid,msgid,0, trid,0);
	}

	@Test
	public void test() {
		/*String data="",collapse_key="",msg="";
		boolean soundEnabled=true;
		try {
			pnR = apnsConnector.sendSingleMessage(pn, trid, title, msg, imageUrl, deeplink, soundEnabled, null, null, cid, appid, ttl, eventType, identifiedFlag);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//assertEquals(guid, pnR.pushid);
		//assertEquals(pn.token, pnR.token);
		fail();*/
	}

}
