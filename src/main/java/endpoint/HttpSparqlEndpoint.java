package endpoint;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

public class HttpSparqlEndpoint {
    public static void main(String[] args) {
        System.out.println(getDbpediaSkiArea("Alyeska Resort"));
    }

    public static String getDbpediaSkiArea(String name) {
        String result = "";
        String sparqlEndpoint = "https://dbpedia.org/sparql";
        Repository repo = new SPARQLRepository(sparqlEndpoint);
        repo.initialize();
        RepositoryConnection conn = repo.getConnection();
        String queryString = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT ?instance WHERE {\n" +
                " ?instance a <http://dbpedia.org/ontology/SkiArea> .\n" +
                " ?instance foaf:name \"" + name + "\"@en .\n" +
                "}";
        TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        TupleQueryResult qryResult = tupleQuery.evaluate();
            if (qryResult.hasNext()) {  // iterate over the result
                BindingSet bindingSet = qryResult.next();
                result = bindingSet.getBinding("instance").getValue().stringValue();
            }
        repo.shutDown();
        return result;
    }
}
