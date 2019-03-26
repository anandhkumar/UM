package com.netcore.pnserver.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.netcore.pnserver.dequeuer.DequeueManager;
import com.netcore.pnserver.gateway.APNSConnector;

/**
 * Servlet implementation class UpdateP12
 */
@WebServlet("/updateP12")
public class UpdateP12 extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	final static Logger logger = Logger.getLogger(UpdateP12.class);
	private WebApplicationContext ctx;
	private DequeueManager dqm ;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public void init() throws ServletException {
        ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
		logger.info("Context Loaded");
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		String clientIdAppid = request.getParameter("clientid");
		clientIdAppid = clientIdAppid+"_"+request.getParameter("appid");
		String clientIdAppid_prod = clientIdAppid+"_0";
		String clientIdAppid_dev = clientIdAppid+"_1";
 		APNSConnector apns = (APNSConnector) ctx.getBean("10");
 		boolean status = false;
 		if(apns.clientMap.remove(clientIdAppid)!=null)
 		status = true;
 		if(apns.clientMap.remove(clientIdAppid_prod)!=null)
 		status = true;
 		if(apns.clientMap.remove(clientIdAppid_dev)!=null)
 	 	status = true;
 		
		if(status){
			out.println("OK");
			logger.info("Removed entry of p12 file from client map ! ClientID & app id :" + clientIdAppid+ clientIdAppid_prod+ clientIdAppid_dev);
		}
		else{
			out.println("Cleint Id App id not found");
			logger.info("Unable to remove p12 file from client map ! ClientID & app id :" + clientIdAppid+ clientIdAppid_prod+ clientIdAppid_dev);
		}
		
		
	}
}
