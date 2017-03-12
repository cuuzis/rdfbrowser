# RDF Ski Resort Browser
Project for Semantic Technologies study course at [Free University of Bolzano](https://www.unibz.it). Project's [presentation](https://docs.google.com/presentation/d/1uSvkDx7_u_tnBN93Ye8ZBS5vlDruVXY7_aZWz9PVwVk/edit?usp=sharing)

### Project Idea
Developing a World's ski resort ontology and represent it on an interactive system. System users must be able to traverse the RDF objects and properties. Objects that have coordinates must be shown on a map. The system combines Ski Resort data from [skimap.org](https://www.skimap.org) into a turtle rdf resource for sparql querying.

### Ontology
The [ontology](https://github.com/cuuzis/rdfbrowser/blob/master/src/main/webapp/ontology.owl) ([visualized in webvowl](http://visualdataweb.de/webvowl/#iri=https://github.com/cuuzis/rdfbrowser/blob/master/src/main/webapp/ontology.owl?raw=true)) defines a relationship of world Regions and Ski Resorts within those regions.

The ontology was designed using [Protégé](http://protege.stanford.edu/), with the help of a very useful guide by The University Of Manchester: [A Practical Guide To Building OWL Ontologies
Using Protégé](http://mowl-power.cs.man.ac.uk/protegeowltutorial/resources/ProtegeOWLTutorialP4_v1_3.pdf)

### System design
![System overview](https://github.com/cuuzis/rdfbrowser/blob/master/rdf_proj.png?raw=true "System overview")

The system is packaged as a Java servlet and runs on Tomcat server. Its presentation logic is handled within index.jsp, and business logic contains 3 packages:
- webscraper: Downloads xml data from skimap.org API
- rdfbuilder: Converts scraped data into a turtle file, which fits the ski resort ontology. Uses RDF4J and Jena.
- endpoint: Provides endpoints to local turtle file and dbpedia.org/sparql. Uses RDF4J.

System users interact with the endpoint package, which gives information about a chosen class, instance or property. It is done by executing simple sparql queries.

For example, here is a generated query asking for all the object properties for the instance :Tyrol. It also gives the object coordinates, if there are any.
```
PREFIX : <http://inf.unibz.it/2017/ski-resort-ontology#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>
SELECT DISTINCT ?property ?hasValue ?lat ?long WHERE {
  :Tyrol ?property ?hasValue
  OPTIONAL {
    ?hasValue geo:lat ?lat .
    ?hasValue geo:long ?long .
  }
  FILTER (isIRI(?hasValue)
  && (?property != rdf:type))
}
```