/**
 * 
 */
package com.netcore.pnserver;

import static org.junit.Assert.assertEquals;

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
import com.netcore.pnserver.gateway.GCMConnector;
import com.netcore.pnserver.pojo.Content;
import com.netcore.pnserver.pojo.Message;
import com.netcore.pnserver.pojo.Msgbody;
import com.netcore.pnserver.pojo.PushNotification;
import com.netcore.pnserver.pojo.PushNotificationRequest;

/**
 * @author Swapnil Srivastav
 *
 */
public class Test_GCMConnector_SendSingleMessage {
	
	private static MySqlDaoImpl mySqlDaoImplObj;
	private static ApplicationContext applicationContext = null;
	private static GCMConnector gcmConnector=null;
	private static MySqlDao mysqlDao;
	private static PushNotification pn,pnR;
	private PushNotificationRequest pnr;
	private Message message;
	private Content contentObj;
	private Msgbody msgbody2;
	private int userid;
	private int identifiedFlag;
	private int ttl;
	private int cid;
	private int msgid;
	
	String guid,appid,msgBody,identity,trid,content, imageUrl,deeplink,msgText,title,os,pushid,eventType,icon;
	@BeforeClass
	public static void setup(){
		applicationContext =
				new ClassPathXmlApplicationContext("applicationContextTest.xml");
		gcmConnector = 	(GCMConnector) applicationContext.getBean("01");	
	}
	
	@Before
	public void initialize(){
		guid="2ec5171c-c305-4aca-b8c0-1850db01d8c5";
		appid ="7dfc71aa22582444eb0f8cbdf1912186";
		userid = 8;
		ttl = 16062812;
		identity="";trid="15260-116-8-72-160628145501";imageUrl="";deeplink="";msgText="Text Msg";title="Test Title";
		os="01";identifiedFlag=0;msgid=116;cid=15260;eventType="app";
		icon = "https://image.flaticon.com/sprites/new_packs/148705-essential-collection.png";
		contentObj = new Content(msgText, title, deeplink, imageUrl,icon,null, null);
		
		message = new Message(trid, identity, contentObj);
		
		msgbody2 = new Msgbody(trid, identity, contentObj);
		
		String siteid="", browser="",url="";
		List<Map<String, String>> actions = new ArrayList<>();
		pnr = new PushNotificationRequest(url, message, os, cid, userid, guid, appid, msgid, ttl, msgbody2, identifiedFlag, siteid, browser,actions,true,null, null,0,0);
		
		pn = new PushNotification(userid,guid,cid,msgid,0,trid,0);
	}
	@Test
	public void test() {
		String data="",collapse_key="",msg="";
		boolean soundEnabled=true;
		/*try {
			//pnR = gcmConnector.sendSingleMessage(pn, trid, title, msg, imageUrl, deeplink, soundEnabled, null, null, cid, appid, ttl, eventType, identifiedFlag);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		assertEquals(guid, pnR.pushid);
		//assertEquals(pn.token, pnR.token); Replace with status
		
	}

}
