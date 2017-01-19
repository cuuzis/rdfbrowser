<%@ page import="endpoint.HttpSparqlEndpoint" %>
<html>
<body>
<h2>Hello World!</h2>
<p>
<%
   out.write(HttpSparqlEndpoint.getResults());
  %>
</p>
</body>
</html>
