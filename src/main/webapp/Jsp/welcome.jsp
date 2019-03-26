<%@page import="com.netcore.pnserver.processor.AbstractTask"%>
<%@page import="com.netcore.pnserver.dequeuer.DequeueManager"%>
<%@page import="com.netcore.pnserver.dequeuer.GenericDequeuer"%>
<%@page import="java.util.Map,java.util.List,java.util.ArrayList "%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="org.springframework.web.context.WebApplicationContext"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org
/TR/html4/loose.dtd">
<html>
<head>
<script type="text/javascript" src="js/jquery-2.2.4.js"> </script>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<style>
#customers {
    font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
    border-collapse: collapse;
    width: 50%;
}

#customers td, #customers th {
    border: 1px solid #ddd;
    text-align: left;
    padding: 6px;
}

#customers tr:nth-child(even){background-color: #f2f2f2}

#customers tr:hover {background-color: #ddd;}

#customers th {
    padding-top: 8px;
    padding-bottom: 8px;
    background-color: #4CAF50;
    color: white;
}
</style>
<title>Welcome</title>
</head>

<body>

<%
	WebApplicationContext ctx;
	DequeueManager dqm =null;
	List<GenericDequeuer> genDqrLst = new ArrayList();
	try {
 		ctx = (WebApplicationContext) request.getAttribute("context");
		dqm = (DequeueManager) ctx.getBean("dequeueManager"); 
		genDqrLst = dqm.getDqrs();
	} catch (Exception e) {
		e.printStackTrace();
	}
	
	String stopAll = (String)request.getParameter("stopAll");
	String startQueue = (String)request.getParameter("startSingle");
	String stopQueue = (String)request.getParameter("stopSingle");
	
	if(stopAll != null && stopAll.equalsIgnoreCase("true")){
		/* System.out.print("stopping");
		dqm.stopAll(); */
	}
	else if(stopQueue != null){
		dqm.stopSingle(stopQueue);
	}	 
	else if(startQueue != null){
		dqm.startSingle(startQueue);
	}
	
	
	%>
<h2> Dequeuer Thread Status: </h2>
<table id="customers">
  <tr>
    <th>Thread Name</th>
    <th>Count</th>
    <th>Running</th>
  </tr>
<% for (GenericDequeuer gd: genDqrLst) { 
	boolean stop = gd.stop;
%>  
  <tr><td> <%=gd.getQueueName() %> </td> <td> <%=1 %></td> <td> <%= (stop == true) ? 0:1 %> 
  <% 
  	if(stop==true){
  %>
  		<button id=<%=gd.getQueueName()%> style="background-color:rgba(255, 0, 0, 0.62)" class="float-left submit-button mystart" >Start</button>
  	<%}
  	else {
  	%>
  		<button id=<%=gd.getQueueName()%> style="background-color:rgba(0, 128, 0, 0.57)" class="float-left submit-button mystop" >Stop</button>
  		
  </td> </tr>
<% }}%>    
</table>
<!-- <p><button id="myButton" class="float-left submit-button" >Stop All</button> -->

<script type="text/javascript">

$(function(){
	
	$(".mystop").click(function(){
		location.href = "hello?stopSingle="+this.id;
	});
	
	$(".mystart").click(function(){
		location.href = "hello?startSingle="+this.id;
	});
	
})

    /* document.getElementById("myButton").onclick = function () {
        location.href = "/p1dm/hello?stopAll=true";
    }; */
    
    
</script>
<p><a href="/PNServer">Home</a>
</body>
</html>