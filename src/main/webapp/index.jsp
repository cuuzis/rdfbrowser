<%@ page import="Secret.SecretApiKeys" %>
<%@ page import="endpoint.LocalFileSparqlEndpoint" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no">
    <meta charset="utf-8">
    <title>Marker Clustering</title>
    <style>
      html, body {
        height: 100%;
        margin: 0;
        padding: 0;
      }
	  @media screen and (max-width: 820px) {
		div#map {
			width:100%;
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
		  padding: 10px;
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
		<ul>
		
          <%
		  boolean foundItems = false;
		  if (className != null) {
		    List<String> triples = LocalFileSparqlEndpoint.getClassInstances(className);
		    if (!triples.isEmpty()) {
			  foundItems = true;
		      for (String str : triples) {
                out.write("<li><a href=\"?Instance=" + str + "\">" + str + "</a></li>");
              }
		    }
		  } else if (propertyName != null) {
			List<String[]> triples = LocalFileSparqlEndpoint.getPropertyInstances(propertyName);
			if (!triples.isEmpty()) {
			  foundItems = true;
			  for (String[] str : triples) {
				out.write("<ul><li><a href=\"?Instance=" + str[0] + "\">" + str[0] + "</a></li>");
				if (str[1].startsWith(":"))
				  out.write("<li><a href=\"?Property=" + str[1] + "\">" + str[1] + "</a></li>");
			    else
				  out.write("<li><a href=\"" + str[1] + "\">" + str[1] + "</a></li>");
				out.write("<li><a href=\"?Instance=" + str[2] + "\">" + str[2] + "</a></li></ul>");
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
			triples = LocalFileSparqlEndpoint.getInstanceProperties(instanceName);
			if (!triples.isEmpty()) {
			  foundItems = true;
			  out.write("<h3>Object Properties</h3>");
			  for (String[] str : triples) {
				out.write("<ul><li><a href=\"?Instance=" + str[0] + "\">" + str[0] + "</a></li>");
				if (str[1].startsWith(":"))
				  out.write("<li><a href=\"?Property=" + str[1] + "\">" + str[1] + "</a></li>");
			    else
				  out.write("<li><a href=\"" + str[1] + "\">" + str[1] + "</a></li>");
				out.write("<li><a href=\"?Instance=" + str[2] + "\">" + str[2] + "</a></li></ul>");
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
          zoom: 3,
          center: {lat: -28.024, lng: 140.887}
        });

        // Create an array of alphabetical characters used to label the markers.
        var labels = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';

        // Add some markers to the map.
        // Note: The code uses the JavaScript Array.prototype.map() method to
        // create an array of markers based on a given "locations" array.
        // The map() method here has nothing to do with the Google Maps API.
        var markers = locations.map(function(location, i) {
          return new google.maps.Marker({
            position: location,
            label: labels[i % labels.length]
          });
        });

        // Add a marker clusterer to manage the markers.
        var markerCluster = new MarkerClusterer(map, markers,
            {imagePath: 'https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m'});
      }
      var locations = [
        {lat: -31.563910, lng: 147.154312},
        {lat: -33.718234, lng: 150.363181},
        {lat: -33.727111, lng: 150.371124},
        {lat: -33.848588, lng: 151.209834},
        {lat: -33.851702, lng: 151.216968},
        {lat: -34.671264, lng: 150.863657},
        {lat: -35.304724, lng: 148.662905},
        {lat: -36.817685, lng: 175.699196},
        {lat: -36.828611, lng: 175.790222},
        {lat: -37.750000, lng: 145.116667},
        {lat: -37.759859, lng: 145.128708},
        {lat: -37.765015, lng: 145.133858},
        {lat: -37.770104, lng: 145.143299},
        {lat: -37.773700, lng: 145.145187},
        {lat: -37.774785, lng: 145.137978},
        {lat: -37.819616, lng: 144.968119},
        {lat: -38.330766, lng: 144.695692},
        {lat: -39.927193, lng: 175.053218},
        {lat: -41.330162, lng: 174.865694},
        {lat: -42.734358, lng: 147.439506},
        {lat: -42.734358, lng: 147.501315},
        {lat: -42.735258, lng: 147.438000},
        {lat: -43.999792, lng: 170.463352}
      ]
    </script>
    <script src="https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/markerclusterer.js">
    </script>
    <script async defer
    src="https://maps.googleapis.com/maps/api/js?key=
    <%
       out.write(SecretApiKeys.MAPS_JAVASCRIPT_KEY);
      %>
      &callback=initMap">
    </script>
  </body>
</html>
