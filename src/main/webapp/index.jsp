<%@ page import="Secret.SecretApiKeys" %>
<%@ page import="endpoint.LocalFileSparqlEndpoint" %>
<%@ page import="endpoint.HttpSparqlEndpoint" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no">
    <meta charset="utf-8">
    <title>RDF Ski Resort Browser</title>
    <style>
      html, body {
        height: 100%;
        margin: 0;
        padding: 0;
      }
	  @media screen and (max-width: 1000px) {
		div#map {
			width:100%;
			height: 60vh;
		}
		div#sidebar {
			width:100%;
		    height: 40vh;
		}
	  }
      #map {
		float: left;
        height: 100%;
        width: 60%;
		min-width: 600px;
      }
	  #sidebar {
		float: left;
		width: 40%;
		min-width: 400px;
        overflow-y: auto;
        height: 100vh;
	  }
	  #sidebar ul {
		  padding-left: 10px;
		  padding-bottom: 5px;
	  }
	  #sidebar li {
		  display: block;
	  }
	  #sidebar h2 {
		  padding-left: 20px;
	  }
    </style>
  </head>
  <body>
    <div id="map"></div>
	<div id="sidebar">
		<ul>
		  <li>Ontology 
		    <a href="<% out.write("http://visualdataweb.de/webvowl/#iri=http://" + request.getServerName() + "/ski-resort-browser/ontology.owl");%>">visualized (in WebVOWL)</a>
			[<a href="<% out.write("http://" + request.getServerName() + "/webvowl/#iri=http://" + request.getServerName() + "/ski-resort-browser/ontology.owl");%>"> locally </a>]
		  </li>
		  <li>Ontology file: <a href="ontology.owl">ontology.owl</a></li>
		  <li>Data file: <a href="model.ttl">model.ttl</a></li>
		  <li>Github: <a href="https://github.com/cuuzis/rdfbrowser">https://github.com/cuuzis/rdfbrowser</a></li>
		  <li>Inspiration + xml data: <a href="https://skimap.org/">https://skimap.org/</a></li>
		</ul>
		<h2>
		<%
		String className = request.getParameter("Class");
		String propertyName = request.getParameter("Property");
		String instanceName = request.getParameter("Instance");
		if (className != null) 
		  out.write("Class "+className);
	    else if (propertyName != null)
		  out.write("Property " + propertyName);
	    else if (instanceName != null)
		  out.write("Instance " + instanceName);
	    else
		  out.write("Nothing");
		%>
		</h2>
		<%
		if (instanceName != null) {
			out.write("<ul><li>");
			String dbpediaEntry = HttpSparqlEndpoint.getDbpediaSkiArea(instanceName.substring(1));
			if (dbpediaEntry == "")
				out.write("No corresponding dbpedia ski resort found");
			else
				out.write("Dbpedia entry: <a href=\"" + dbpediaEntry + "\">" + dbpediaEntry + "</a>");
			out.write("</li></ul>");
		}
		%>
		<ul>
          <%
		  boolean foundItems = false;
		  List<String[]> classInstances = new ArrayList<>();
		  List<String[]> propertyInstances = new ArrayList<>();
		  List<String[]> instInstances = new ArrayList<>();
		  if (className != null) {
		    classInstances = LocalFileSparqlEndpoint.getClassInstances(className);
		    if (!classInstances.isEmpty()) {
			  foundItems = true;
			  out.write("<h3>Instances</h3>");
		      for (String str[] : classInstances) {
                out.write("<li><ul>");
				out.write("<li><a href=\"?Instance=" + str[0] + "\">" + str[0] + "</a><li>");
				if (str[1].length() > 0) {
                  out.write("<li>" + str[1] + "</li>");
				}
			    out.write("</ul></li>");
              }
		    }
		  } else if (propertyName != null) {
			propertyInstances = LocalFileSparqlEndpoint.getPropertyInstances(propertyName);
			if (!propertyInstances.isEmpty()) {
			  foundItems = true;
			  out.write("<h3>Instances</h3>");
			  for (String[] str : propertyInstances) {
				out.write("<li><ul>");
				out.write("<li><a href=\"?Instance=" + str[0] + "\">" + str[0] + "</a></li>");
				if (str[1].startsWith(":"))
				  out.write("<li><a href=\"?Property=" + str[1] + "\">" + str[1] + "</a></li>");
			    else
				  out.write("<li><a href=\"" + str[1] + "\">" + str[1] + "</a></li>");
				out.write("<li><a href=\"?Instance=" + str[2] + "\">" + str[2] + "</a></li>");
				if (str[3].length() > 0) {
                  out.write("<li>" + str[3] + "</li>");
				}
				out.write("</ul></li>");
			  }
			}
		  } else if (instanceName != null) {
			List<String> classes = LocalFileSparqlEndpoint.getInstanceClasses(instanceName);
		    if (!classes.isEmpty()) {
			  foundItems = true;
			  out.write("<h3>Class/Classes</h3><ul>");
		      for (String str : classes) {
                out.write("<li><a href=\"?Class=" + str + "\">" + str + "</a></li>");
              }
			  out.write("</ul>");
		    }
			List<String[]> triples = LocalFileSparqlEndpoint.getInstanceLiterals(instanceName);
			if (!triples.isEmpty()) {
			  foundItems = true;
			  out.write("<h3>Data Properties (Literals)</h3>");
			  for (String[] str : triples) {
				out.write("<ul><li><a href=\"?Instance=" + str[0] + "\">" + str[0] + "</a></li>");
				if (str[1].startsWith(":"))
				  out.write("<li><a href=\"?Property=" + str[1] + "\">" + str[1] + "</a></li>");
			    else
				  out.write("<li><a href=\"" + str[1] + "\">" + str[1] + "</a></li>");
				out.write("<li>\"" + str[2] + "\"</li></ul>");
			  }
			}
			instInstances = LocalFileSparqlEndpoint.getInstanceProperties(instanceName);
			if (!instInstances.isEmpty()) {
			  foundItems = true;
			  out.write("<h3>Object Properties</h3>");
			  for (String[] str : instInstances) {
				out.write("<li><ul>");
				out.write("<li><a href=\"?Instance=" + str[0] + "\">" + str[0] + "</a></li>");
				if (str[1].startsWith(":"))
				  out.write("<li><a href=\"?Property=" + str[1] + "\">" + str[1] + "</a></li>");
			    else
				  out.write("<li><a href=\"" + str[1] + "\">" + str[1] + "</a></li>");
				out.write("<li><a href=\"?Instance=" + str[2] + "\">" + str[2] + "</a></li>");
				if (str[3].length() > 0) {
                  out.write("<li>" + str[3] + "</li>");
				}
				out.write("</ul></li>");
			  }
			}
		  }
		  if (!foundItems)
		    out.write("<li>No entries. Try browsing <a href=\"?Class=:Continent\">:Continent</a></li>");
		  %>
		</ul>
	</div>
    <script>

      function initMap() {

        var map = new google.maps.Map(document.getElementById('map'), {
		  <%
		    String center = "";
			String zoom = "7";
			String coordinates = "";
		    if (className != null) 
			  center = LocalFileSparqlEndpoint.getInstanceCoordinates(className);
		    else if (instanceName != null) {
			  coordinates = LocalFileSparqlEndpoint.getInstanceCoordinates(instanceName);
			  center = coordinates;
			}
		    if (center == "") {
			  center = "{lat: 46.498198, lng: 11.351372}";
			  zoom = "2";
			}
		    out.write("zoom: " + zoom + ",");
		    out.write("center: " + center);
		  %>
        });
		
		
      var markers = [];
	  <%
	    if (coordinates != "") {
		  out.write("markers = [getInstanceMarker(" + center + ",\"" + instanceName + "\")];");
		} else if (!classInstances.isEmpty()) {
		  for (String[] str : classInstances) {
			if (str[1].length() > 0)
			  out.write("markers.push(getInstanceMarker(" + str[1] + ",\"" + str[0] +"\"));");
		  }
		} else if (!propertyInstances.isEmpty()) {
		  for (String[] str : propertyInstances) {
			if (str[3].length() > 0)
			  out.write("markers.push(getInstanceMarker(" + str[3] + ",\"" + str[2] +"\"));");
		  }
		} else if (!instInstances.isEmpty()) {
		  for (String[] str : instInstances) {
			if (str[3].length() > 0)
			  out.write("markers.push(getInstanceMarker(" + str[3] + ",\"" + str[2] +"\"));");
		  }
		}
	  %>

        // Add a marker clusterer to manage the markers.
        var markerCluster = new MarkerClusterer(map, markers,
            {imagePath: 'https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m'});
      }
	  
	  function getInstanceMarker(pos, instanceName) {
		var marker = new google.maps.Marker({position: pos, title: instanceName});
		var contentString = '<a href="?Instance=' + instanceName + '">' + instanceName + '</a>';
		var infowindow = new google.maps.InfoWindow({content: contentString});
		marker.addListener('click', function() {infowindow.open(map, marker);});
		return marker;
	  }
    </script>
    <script src="https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/markerclusterer.js">
    </script>
    <script async defer
    src="https://maps.googleapis.com/maps/api/js?key=<% out.write(SecretApiKeys.MAPS_JAVASCRIPT_KEY); %>&callback=initMap">
    </script>
  </body>
</html>
