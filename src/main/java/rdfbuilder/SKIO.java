package rdfbuilder;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class SKIO {
    public static final String NAMESPACE = "http://inf.unibz.it/2017/ski-resort-ontology#";
    public static final String PREFIX = "skio";
    public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
    public static final IRI REGION;
    public static final IRI COUNTRY;
    public static final IRI CONTINENT;

    static {
        SimpleValueFactory factory = SimpleValueFactory.getInstance();
        REGION = factory.createIRI(NAMESPACE, "Region");
        CONTINENT = factory.createIRI(NAMESPACE, "Continent");
        COUNTRY = factory.createIRI(NAMESPACE, "Country");
    }
}
