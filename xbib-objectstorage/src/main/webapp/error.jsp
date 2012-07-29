<%@ page isErrorPage="true" session="false" import="org.xbib.util.ExceptionFormatter"  contentType="text/plain; charset=UTF-8"%>
<%
         response.setHeader("X-Powered-By", "");
         response.setHeader("Server", "");
         out.print("Error\n");
         out.print("URI: " + request.getAttribute("javax.servlet.error.request_uri") + "\n");
         out.print("Servlet: " + request.getAttribute("javax.servlet.error.servlet_name") + "\n");
         out.print("Status: " + request.getAttribute("javax.servlet.error.status_code") + "\n");
         out.print("Exception Type: " + request.getAttribute("javax.servlet.error.exception_type") + "\n");
         out.print("Error Message: " + request.getAttribute("javax.servlet.error.message") + "\n");
         if (request.getAttribute("javax.servlet.error.exception") != null) {            
            out.print(ExceptionFormatter.format((Throwable)request.getAttribute("javax.servlet.error.exception")));
         }
%>