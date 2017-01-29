package rdfbuilder;

// Taken from
// package org.apache.marmotta.commons.vocabulary;
// and slightly modified

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;


public class GEO {
    public static final String NAMESPACE = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    public static final String PREFIX = "geo";
    public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
    public static final IRI POINT;
    public static final IRI SPATIALTHING;
    public static final IRI ALT;
    public static final IRI LAT;
    public static final IRI LAT_LONG;
    public static final IRI LOCATION;
    public static final IRI LONG;

    static {
        SimpleValueFactory factory = SimpleValueFactory.getInstance();
        POINT = factory.createIRI(NAMESPACE, "Point");
        SPATIALTHING = factory.createIRI(NAMESPACE, "SpatialThing");
        ALT = factory.createIRI(NAMESPACE, "alt");
        LAT = factory.createIRI(NAMESPACE, "lat");
        LAT_LONG = factory.createIRI(NAMESPACE, "lat_long");
        LOCATION = factory.createIRI(NAMESPACE, "location");
        LONG = factory.createIRI(NAMESPACE, "long");
    }
}

