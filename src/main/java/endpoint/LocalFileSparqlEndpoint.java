package endpoint;

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
        for (String str : getClassInstances(":Continent")) {
            System.out.println(str);
        }
        System.out.println("---- Property Info -----------------------------");
        for (String[] str : getPropertyInstances(":containsGeographicRegion")) {
            System.out.println("Triple:");
            System.out.println(str[0]);
            System.out.println(str[1]);
            System.out.println(str[2]);
        }
        System.out.println("---- Instance Info -----------------------------");
        for (String[] str : getInstanceProperties(":Americas")) {
            System.out.println("Triple:");
            System.out.println(str[0]);
            System.out.println(str[1]);
            System.out.println(str[2]);
        }
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
                        /*"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> \n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
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

    public static List<String> getClassInstances(String rdfClass) {
        String qry = "SELECT DISTINCT ?className WHERE {\n" +
                "?className a "+rdfClass+".\n" +
                "}";
        List<String> results = new ArrayList<>();
        try {
            TupleQueryResult qryResult = executeQuery(qry);
            while (qryResult.hasNext()) {
                BindingSet bindingSet = qryResult.next();
                results.add(getStringVal(bindingSet,"className"));
            }
        } catch (Exception e) { // Malformed query etc
            e.printStackTrace();
        } finally {
            connection.close();
            repo.shutDown();
        }
        return results;
    }

    public static List<String[]> getPropertyInstances(String rdfProperty) throws IOException {
        String qry = "SELECT DISTINCT ?instance ?hasValue WHERE {\n" +
                "?instance " + rdfProperty + " ?hasValue\n" +
                "}";
        List<String[]> results = new ArrayList<>();
        try {
            TupleQueryResult qryResult = executeQuery(qry);
            while (qryResult.hasNext()) {
                BindingSet bindingSet = qryResult.next();
                results.add(new String[]{
                        getStringVal(bindingSet, "instance"),
                        rdfProperty,
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

    public static List<String[]> getInstanceProperties(String rdfInstance) throws IOException {
        String qry = "SELECT DISTINCT ?property ?hasValue WHERE {\n" +
                rdfInstance + " ?property ?hasValue\n" +
                "}";
        List<String[]> results = new ArrayList<>();
        try {
            TupleQueryResult qryResult = executeQuery(qry);
            while (qryResult.hasNext()) {
                BindingSet bindingSet = qryResult.next();
                results.add(new String[]{
                        rdfInstance,
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

    private static String getStringVal(BindingSet bindingSet, String propertyName) {
        return bindingSet.getValue(propertyName).stringValue()
                .replace(SKIO.NAMESPACE, ":");
    }
}
