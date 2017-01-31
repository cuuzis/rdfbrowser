package endpoint;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import rdfbuilder.SKIO;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LocalFileSparqlEndpoint {

    private static Repository repo;
    private static RepositoryConnection connection;

    // Demo function
    public static void main(String[] args) throws IOException {
        System.out.println("---- Class Info -----------------------------");
        for (String[] str : getClassInstances(":SkiResort")) {
            System.out.println(str[0]);
            if (str[1].length() > 0)
                System.out.println(str[1]);
        }
        System.out.println("---- Property Info -----------------------------");
        for (String[] str : getPropertyInstances(":containsSkiResort")) {
            System.out.println("Triple:");
            System.out.println(str[0]);
            System.out.println(str[1]);
            System.out.println(str[2]);
            if (str[3].length() > 0)
                System.out.println(str[3]);
        }
        System.out.println("---- Instance Instances -----------------------------");
        for (String[] str : getInstanceProperties(":Tyrol")) {
            System.out.println("Triple:");
            System.out.println(str[0]);
            System.out.println(str[1]);
            System.out.println(str[2]);
            if (str[3].length() > 0)
                System.out.println(str[3]);
        }
        System.out.println("---- Instance Literals -----------------------------");
        for (String[] str : getInstanceLiterals(":Seefeld")) {
            System.out.println("Triple:");
            System.out.println(str[0]);
            System.out.println(str[1]);
            System.out.println(str[2]);
        }
        System.out.println("---- Instance Classes -----------------------------");
        for (String str : getInstanceClasses(":Seefeld")) {
            System.out.println(str);
        }
        System.out.println("---- Instance Coordinates -----------------------------");
        System.out.println(getInstanceCoordinates(":Seefeld"));
    }

    private static TupleQueryResult executeQuery(String queryString) throws IOException {
        repo = new SailRepository(new MemoryStore());
        repo.initialize();
        connection = repo.getConnection();
        // adding with inputstream is read only
        InputStream inputStream = LocalFileSparqlEndpoint.class.getResourceAsStream("/model.ttl");
        connection.add(inputStream, SKIO.NAMESPACE, RDFFormat.TURTLE);
        String fullQuery =
                "PREFIX : <"+SKIO.NAMESPACE+">\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                        "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> \n" +
                        /*"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
                        "PREFIX dcterms: <http://purl.org/dc/terms/> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                        "PREFIX skio: <http://inf.unibz.it/2017/ski-resort-ontology/#> \n" +
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n" +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +*/
                        queryString;
        TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, fullQuery);
        TupleQueryResult result = tupleQuery.evaluate();
        //connection.close();
        //repo.shutDown();
        return result;
    }

    public static List<String[]> getClassInstances(String rdfClass) {
        String qry = "SELECT DISTINCT ?instance ?lat ?long WHERE {\n" +
                "?instance a "+rdfClass+" .\n" +
                " OPTIONAL {" +
                " ?instance geo:lat ?lat .\n" +
                " ?instance geo:long ?long .} \n" +
                "}";
        List<String[]> results = new ArrayList<>();
        try {
            TupleQueryResult qryResult = executeQuery(qry);
            while (qryResult.hasNext()) {
                BindingSet bindingSet = qryResult.next();
                Value lat = bindingSet.getValue("lat");
                Value lng = bindingSet.getValue("long");
                String coordinates = "";
                if (lat != null && lng != null)
                    coordinates =  "{lat: " + lat.stringValue() + ", lng: " + lng.stringValue() + "}";
                results.add(new String[]{
                        getStringVal(bindingSet, "instance"),
                        coordinates}
                );
            }
        } catch (Exception e) { // Malformed query etc
            e.printStackTrace();
        } finally {
            connection.close();
            repo.shutDown();
        }
        return results;
    }

    public static List<String[]> getPropertyInstances(String rdfProperty) {
        String qry = "SELECT DISTINCT ?instance ?hasValue ?lat ?long WHERE {\n" +
                "?instance " + rdfProperty + " ?hasValue\n" +
                " OPTIONAL {" +
                " ?hasValue geo:lat ?lat .\n" +
                " ?hasValue geo:long ?long .} \n" +
                "}";
        List<String[]> results = new ArrayList<>();
        try {
            TupleQueryResult qryResult = executeQuery(qry);
            while (qryResult.hasNext()) {
                BindingSet bindingSet = qryResult.next();
                Value lat = bindingSet.getValue("lat");
                Value lng = bindingSet.getValue("long");
                String coordinates = "";
                if (lat != null && lng != null)
                    coordinates =  "{lat: " + lat.stringValue() + ", lng: " + lng.stringValue() + "}";
                results.add(new String[]{
                        getStringVal(bindingSet, "instance"),
                        rdfProperty,
                        getStringVal(bindingSet, "hasValue"),
                        coordinates}
                        );
            }
        } catch (Exception e) { // Malformed query etc
            e.printStackTrace();
        } finally {
            connection.close();
            repo.shutDown();
        }
        return results;
    }

    public static List<String[]> getInstanceProperties(String rdfInstance) {
        String qry = "SELECT DISTINCT ?property ?hasValue ?lat ?long WHERE {\n" +
                rdfInstance + " ?property ?hasValue\n" +
                " OPTIONAL {" +
                " ?hasValue geo:lat ?lat .\n" +
                " ?hasValue geo:long ?long .} \n" +
                " FILTER (isIRI(?hasValue)\n" +
                " && (?property != rdf:type))\n" +
                "}";
        List<String[]> results = new ArrayList<>();
        try {
            TupleQueryResult qryResult = executeQuery(qry);
            while (qryResult.hasNext()) {
                BindingSet bindingSet = qryResult.next();
                Value lat = bindingSet.getValue("lat");
                Value lng = bindingSet.getValue("long");
                String coordinates = "";
                if (lat != null && lng != null)
                    coordinates =  "{lat: " + lat.stringValue() + ", lng: " + lng.stringValue() + "}";
                results.add(new String[]{
                        rdfInstance,
                        getStringVal(bindingSet, "property"),
                        getStringVal(bindingSet, "hasValue"),
                        coordinates}
                );
            }
        } catch (Exception e) { // Malformed query etc
            e.printStackTrace();
        } finally {
            connection.close();
            repo.shutDown();
        }
        return results;
    }

    public static List<String[]> getInstanceLiterals(String rdfInstance) {
        String qry = "SELECT DISTINCT ?property ?hasValue WHERE {\n" +
                rdfInstance + " ?property ?hasValue\n" +
                " FILTER isLiteral(?hasValue)\n" +
                "}";
        List<String[]> results = new ArrayList<>();
        try {
            TupleQueryResult qryResult = executeQuery(qry);
            while (qryResult.hasNext()) {
                BindingSet bindingSet = qryResult.next();
                results.add(new String[]{
                        rdfInstance, // + bindingSet.getValue("lat").stringValue(),
                        getStringVal(bindingSet, "property"),
                        getStringVal(bindingSet, "hasValue")}
                );
            }
        } catch (Exception e) { // Malformed query etc
            e.printStackTrace();
        } finally {
            connection.close();
            repo.shutDown();
        }
        return results;
    }

    public static List<String> getInstanceClasses(String rdfInstance) {
        String qry = "SELECT DISTINCT ?hasValue WHERE {\n" +
                rdfInstance + " rdf:type ?hasValue .\n" +
                "}";
        List<String> results = new ArrayList<>();
        try {
            TupleQueryResult qryResult = executeQuery(qry);
            while (qryResult.hasNext()) {
                BindingSet bindingSet = qryResult.next();
                results.add(getStringVal(bindingSet, "hasValue"));
            }
        } catch (Exception e) { // Malformed query etc
            e.printStackTrace();
        } finally {
            connection.close();
            repo.shutDown();
        }
        return results;
    }

    // returns string JSON string for google maps api
    public static String getInstanceCoordinates(String rdfInstance) {
        String qry = "SELECT ?lat ?long WHERE {\n" +
                rdfInstance + " geo:lat ?lat .\n" +
                rdfInstance + " geo:long ?long .\n" +
                "}";
        String result = "";
        try {
            TupleQueryResult qryResult = executeQuery(qry);
            if (qryResult.hasNext()) {
                BindingSet bindingSet = qryResult.next();
                String lat = bindingSet.getValue("lat").stringValue();
                String lng = bindingSet.getValue("long").stringValue();
                result = "{lat: " + lat + ", lng: " + lng + "}";
            }
        } catch (Exception e) { // Malformed query etc
            e.printStackTrace();
        } finally {
            connection.close();
            repo.shutDown();
        }
        return result;
    }

    private static String getStringVal(BindingSet bindingSet, String propertyName) {
        return bindingSet.getValue(propertyName).stringValue()
                .replace(SKIO.NAMESPACE, ":");
    }
}
