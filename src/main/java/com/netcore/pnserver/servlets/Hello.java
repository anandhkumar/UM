package com.netcore.pnserver.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.netcore.pnserver.dequeuer.DequeueManager;
import com.netcore.pnserver.init.ConfigManager;
import com.netcore.pnserver.init.InitLogger;

public class Hello extends HttpServlet {

	private static final long serialVersionUID = 1L;
	final static Logger logger = Logger.getLogger(Hello.class);
	private WebApplicationContext ctx;
	private DequeueManager dqm;

	public void init() throws ServletException {
		try {
			long t1 = System.currentTimeMillis();
			initLogger();
			logger.info("Starting PN Server");

			List<String> myList = getQueues();

			ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
			logger.info("Context Loaded");

			this.dqm = (DequeueManager) ctx.getBean("dequeueManager");
			dqm.setDqConf(myList);
			dqm.startDequeuers();

			long t2 = System.currentTimeMillis();
			logger.info("PN server loading done in " + (t2 - t1) + "ms.");

		} catch (NullPointerException npe) {
			logger.error("Please define 'deq.list' in the property file", npe);
			throw new IllegalArgumentException("Please define 'deq.list' in the property file");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		}
	}

	public List<String> getQueues() {
		ConfigManager cfm = ConfigManager.getInstance();
		String deq = cfm.getProperty("deq.list").trim();
		List<String> myList = new ArrayList<String>(Arrays.asList(deq.split(",")));
		return myList;
	}

	public void initLogger() {
		ConfigManager cfm = ConfigManager.getInstance();
		Properties loggerProperties = cfm.getProperties("log4j");
		InitLogger.initLogger(loggerProperties);
	}

	public void contextInitialized(ServletContextEvent event) {

	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		req.setAttribute("context", ctx);
		req.getRequestDispatcher("/Jsp/welcome.jsp").forward(req, res);
	}
}