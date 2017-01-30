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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LocalFileSparqlEndpoint {

    private static Repository repo;
    private static RepositoryConnection connection;

    public static void main(String[] args) throws IOException {
        System.out.println("Instances");
        //getClassInstances("").forEach(s -> System.out.println(s));
        for (String str : getClassInstances("")) {
            System.out.println(str);
        }
        System.out.println("Properties");
        //getClassProperties("").forEach(s -> System.out.println(s));
        for (String str : getClassProperties("")) {
            System.out.println(str);
        }
    }

    private static TupleQueryResult executeQuery(String queryString) throws IOException {
        repo = new SailRepository(new MemoryStore());
        repo.initialize();
        connection = repo.getConnection();
        // adding with inputstream is read only
        //InputStream inputstream = new FileInputStream(MODEL_LOCATION);
        InputStream inputStream = LocalFileSparqlEndpoint.class.getResourceAsStream("/model.ttl");
        connection.add(inputStream, "<http://inf.unibz.it/2017/ski-resort-ontology#>", RDFFormat.TURTLE);
        //connection.add(new File(MODEL_LOCATION), null, RDFFormat.TURTLE);
        String fullQuery =
                "PREFIX : <http://inf.unibz.it/2017/ski-resort-ontology#>\n" +
                        "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> \n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                        "PREFIX dcterms: <http://purl.org/dc/terms/> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                        "PREFIX skio: <http://inf.unibz.it/2017/ski-resort-ontology/#> \n" +
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n" +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                        queryString;
        TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, fullQuery);
        TupleQueryResult result = tupleQuery.evaluate();
        //connection.close();
        //repo.shutDown();
        return result;
    }

    public static List<String> getClassInstances(String rdfClass) throws IOException {
        String qry = "SELECT ?className WHERE {\n" +
                "?className a :Continent.\n" +
                "}";
        TupleQueryResult qryResult = executeQuery(qry);
        List<String> results = new ArrayList<String>();
        while (qryResult.hasNext()) {
            BindingSet bindingSet = qryResult.next();
            results.add(String.valueOf(bindingSet.getValue("className")));
        }
        connection.close();
        repo.shutDown();
        return results;
    }

    public static List<String> getClassProperties(String rdfProperty) throws IOException {
        String qry = "SELECT DISTINCT ?propertyName WHERE {\n" +
                "?className a :Continent.\n" +
                "?className ?propertyName ?hasValue\n" +
                "}";
        TupleQueryResult qryResult = executeQuery(qry);
        List<String> results = new ArrayList<String>();
        while (qryResult.hasNext()) {
            BindingSet bindingSet = qryResult.next();
            results.add(String.valueOf(bindingSet.getValue("propertyName")));
        }
        connection.close();
        repo.shutDown();
        return results;
    }
}
