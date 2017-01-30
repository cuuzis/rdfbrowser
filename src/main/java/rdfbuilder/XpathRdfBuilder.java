package rdfbuilder;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static webscraper.ResortDownloader.REGIONS_OUT;
import static webscraper.ResortDownloader.SKI_AREAS_OUT;

public class XpathRdfBuilder {

    public static final String MODEL_LOCATION = "src/main/resources/model.ttl";
    private static final String ONTOLOGY_LOCATION = "src/main/resources/skiresorts-xml.owl";

    private static XPathFactory xPathFactory;
    private static XPath xPath;
    private static DocumentBuilderFactory docFactory;
    private static DocumentBuilder docBuilder;
    private static ModelBuilder builder;


    public static void main(String[] args) throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
        xPathFactory = XPathFactory.newInstance();
        xPath = xPathFactory.newXPath();
        docFactory = DocumentBuilderFactory.newInstance();
        docBuilder = docFactory.newDocumentBuilder();
        builder = new ModelBuilder();

        // Extract namespaces from ontology
        OntModel ontoModel = ModelFactory.createOntologyModel();
        InputStream ontoFile = FileManager.get().open(ONTOLOGY_LOCATION);
        ontoModel.read(ontoFile, null);
        Map<String, String> namespaces = ontoModel.getNsPrefixMap();
        for(Map.Entry<String, String> entry : namespaces.entrySet()) {
            builder.setNamespace(entry.getKey(), entry.getValue());
        }


        buildRegions(1);
        buildResorts(1);


        // Save model to file
        final Model model = builder.build();
        FileOutputStream out = new FileOutputStream(MODEL_LOCATION);
        Rio.write(model, out, RDFFormat.TURTLE);
    }


    private static void buildRegions(int startFrom) throws IOException, SAXException, XPathExpressionException {
        int counter = startFrom;
        String pathname = REGIONS_OUT + counter + ".xml";

        // while file exists
        while (new File(pathname).isFile()) {
            // Get XML values using XPath
            Document doc = docBuilder.parse(pathname);
            XPathExpression expr;
            Node node;
            NodeList nodeList;
            int level;
            String name;
            String subject;

            expr = xPath.compile("/region/@level");
            node = (Node)expr.evaluate(doc, XPathConstants.NODE);
            level = Integer.parseInt(node.getNodeValue());

            expr = xPath.compile("/region/name");
            name = expr.evaluate(doc);
            subject = getSubjectStr(name);

            // Store values in ttl:
            IRI geographicRegion;
            if (level == 1)
                geographicRegion = SKIO.CONTINENT;
            else if (level == 2)
                geographicRegion = SKIO.COUNTRY;
            else
                geographicRegion = SKIO.REGION;
            builder.defaultGraph()
                    .subject(subject)
                    .add(RDF.TYPE, geographicRegion)
                    .add(DCTERMS.IDENTIFIER, counter)
                    .add(FOAF.NAME, name);

            // parent regions
            expr = xPath.compile("/region/parents/region");
            nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                String parentGeoRegion = nodeList.item(i).getTextContent();
                builder.defaultGraph()
                        .subject(subject)
                        .add(SKIO.ISREGIONINREGION, getSubjectStr(parentGeoRegion));
            }

            // child regions
            expr = xPath.compile("/region/regions/region");
            nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                String childGeoRegion = nodeList.item(i).getTextContent();
                builder.defaultGraph()
                        .subject(subject)
                        .add(SKIO.CONTAINSGEOREGION, getSubjectStr(childGeoRegion));
            }

            // child ski resorts
            expr = xPath.compile("/region/skiAreas/skiArea");
            nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                String childSkiResort = nodeList.item(i).getTextContent();
                builder.defaultGraph()
                        .subject(subject)
                        .add(SKIO.CONTAINSSKIRESORT, getSubjectStr(childSkiResort));
            }

            // Get next counter(identifier) and filename
            counter++;
            pathname = REGIONS_OUT + counter + ".xml";
        }
        System.out.println("Processed " + (counter - startFrom) + " regions");
    }


    private static void buildResorts(int startFrom) throws IOException, SAXException, XPathExpressionException {
        int counter = startFrom;
        int brokenFiles = 0;
        String pathname = SKI_AREAS_OUT + counter + ".xml";

        // while file exists
        while (new File(pathname).isFile()) {
            // Check if file is not broken
            BufferedReader br = Files.newBufferedReader(Paths.get(pathname));
            if (br.readLine().equals("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>")) {
                // Get XML values using XPath
                Document doc = docBuilder.parse(pathname);
                XPathExpression expr;
                NodeList nodeList;
                Node node;
                String name;
                String subject;
                float latitude;
                float longitude;

                expr = xPath.compile("/skiArea/name");
                name = expr.evaluate(doc);
                subject = getSubjectStr(name);

                // Store mandatory values in ttl
                builder.defaultGraph()
                        .subject(subject)
                        .add(RDF.TYPE, SKIO.SKIRESORT)
                        .add(DCTERMS.IDENTIFIER, counter)
                        .add(FOAF.NAME, name);

                // parent regions
                expr = xPath.compile("/skiArea/regions/region");
                nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    String parentGeoRegion = nodeList.item(i).getTextContent();
                    builder.defaultGraph()
                            .subject(subject)
                            .add(SKIO.ISRESORTINREGION, getSubjectStr(parentGeoRegion));
                }

                // add lat long if they exist
                expr = xPath.compile("/skiArea/georeferencing/@lat");
                node = (Node)expr.evaluate(doc, XPathConstants.NODE);
                boolean hasLat = node != null;
                if (hasLat) {
                    latitude = Float.parseFloat(node.getNodeValue());

                    expr = xPath.compile("/skiArea/georeferencing/@lng");
                    node = (Node) expr.evaluate(doc, XPathConstants.NODE);
                    longitude = Float.parseFloat(node.getNodeValue());

                    builder.defaultGraph()
                            .subject(subject)
                            .add(GEO.LAT, latitude)
                            .add(GEO.LONG, longitude);
                }
            }
            else {
                brokenFiles++;
            }
            // Get next identifier and filename
            counter++;
            pathname = SKI_AREAS_OUT + counter + ".xml";
        }
        System.out.println("Processed " + (counter - startFrom) + " resorts. Of them invalid files: " + brokenFiles);
    }

    private static String getSubjectStr(String name) throws UnsupportedEncodingException {
        //return SKIO.PREFIX + ":" + name.replaceAll("( |\\'|\\\\)","_");
        return SKIO.PREFIX + ":" + URLEncoder.encode(name, "UTF-8");
    }
}
