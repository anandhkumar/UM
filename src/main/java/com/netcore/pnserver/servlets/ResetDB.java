package com.netcore.pnserver.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.netcore.pnserver.util.ClientDBMap;

/**
 * Servlet implementation class ResetDB
 */
@WebServlet("/resetDB")
public class ResetDB extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private WebApplicationContext ctx;
	private ClientDBMap clientDbMap;
	final static Logger logger = Logger.getLogger(ResetDB.class);
	
	public void init() throws ServletException {
        ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
		logger.info("Context Loaded");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter pw = response.getWriter();
		response.setContentType("text/html;charset=UTF-8");
		clientDbMap = (ClientDBMap) ctx.getBean("clientdbmap");
		String message = clientDbMap.updateMap(request.getParameter("clientid"));
		pw.println("<html>");
		pw.println("<body>");
		pw.println(message);
		pw.println("</body>");
		pw.println("</html>");
	}
}