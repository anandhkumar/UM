/**
 * 
 */
package com.netcore.pnserver;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
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
public class Test_GCMConnector_SendMultiCast {

	private static MySqlDaoImpl mySqlDaoImplObj;
	private static ApplicationContext applicationContext = null;
	private static GCMConnector gcmConnector=null;
	private static MySqlDao mysqlDao;
	private static PushNotification pn,pn1,pnR;
	private PushNotificationRequest pnr;
	private Message message;
	private Content contentObj;
	private Msgbody msgbody2;
	private int userid,identifiedFlag,ttl,cid,msgid;
	private String guid,appid,msgBody,identity,trid,content, imageUrl,deeplink,msgText,title,os,pushid,eventType;
	private static List<PushNotification> pnList1,pnList1R;
	private String icon = "https://image.flaticon.com/sprites/new_packs/148705-essential-collection.png";
	List<Map<String, String>> actions;
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
		identity="";trid="15260-512-0-0-161202170915";imageUrl="";deeplink="";msgText="Indian Navy men in blue Men in Action.\nEdited";title="Indian Navy Men in";
		os="01";identifiedFlag=0;msgid=512;cid=15260;eventType="app";
		
		contentObj = new Content(msgText, identity, title, deeplink,icon,null, null);
		
		message = new Message(trid, identity, contentObj);
		
		msgbody2 = new Msgbody(trid, identity, contentObj);
		actions = new ArrayList<>();
		String siteid="", browser="",url="";
		//public PushNotificationRequest(String url, Message message, String os,int cid, int uid, String pushid, String appid, int msgid, int ttl,
		//Msgbody msgbody,int identified,String siteid, String browser,List<Map<String, String>> actions, boolean autohide)
		pnr = new PushNotificationRequest(url, message, os, cid, userid, guid, appid, msgid, ttl, msgbody2, identifiedFlag, siteid, browser,actions,true,null, null,0,0);
		
		pn = new PushNotification(userid,guid,cid,msgid,0,trid,0);
		pn1 = new PushNotification(userid, guid,cid,msgid,0,trid,0);
		pnList1 = new ArrayList<PushNotification>();
		pnList1.add(pn);
		
		pnList1R = new ArrayList<PushNotification>();
	}
	

	@Test
	public void test() {
		boolean soundEnabled = true;
		String data="";
		String collapse_key="";
		String deepLink="",msg="";
		/*try {
			pnList1R= gcmConnector.sendMultiCast(pnList1, trid, title, msg, imageUrl, null, soundEnabled, data, collapse_key, cid, appid, ttl, eventType, identifiedFlag);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		assertEquals(pnList1, pnList1R);
	}

}
