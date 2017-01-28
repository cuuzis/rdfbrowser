package rdfbuilder;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static webscraper.ResortDownloader.REGIONS_OUT;

public class XpathRdfBuilder {

    private static final String outputLocation = "src/main/resources/model.ttl";

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

        // Extract namespaces from ontology:
        OntModel ontoModel = ModelFactory.createOntologyModel();
        InputStream ontoFile = FileManager.get().open("src/main/resources/skiresorts-xml.owl");
        ontoModel.read(ontoFile, null);
        Map<String, String> namespaces = ontoModel.getNsPrefixMap();
        for(Map.Entry<String, String> entry : namespaces.entrySet()) {
            //System.out.println(entry.getKey() + ": " + entry.getValue());
            builder.setNamespace(entry.getKey(), entry.getValue());
        }


        buildRegions(1);


        // Save model to file
        final Model model = builder.build();
        FileOutputStream out = new FileOutputStream(outputLocation);
        Rio.write(model, out, RDFFormat.TURTLE);
    }


    private static void buildRegions(int startFrom) throws IOException, SAXException, XPathExpressionException {
        int counter = startFrom;
        String pathname = REGIONS_OUT + counter + ".xml";
        while (new File(pathname).isFile()) {
            // Read XML values
            Document doc = docBuilder.parse(pathname);
            int level;
            String parent;
            String name;
            XPathExpression expr;
            Node node;
            expr = xPath.compile("/region/@level");
            node = (Node)expr.evaluate(doc, XPathConstants.NODE);
            level = Integer.parseInt(node.getNodeValue());

            expr = xPath.compile("/region/name");
            name = expr.evaluate(doc);

            expr = xPath.compile("/region/regions/region");
            NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                int containsId = Integer.parseInt(
                        nodeList.item(i).getAttributes().item(0).getTextContent());
            }

            expr = xPath.compile("/region/parents/region");
            NodeList parents = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                int containsId = Integer.parseInt(
                        nodeList.item(i).getAttributes().item(0).getTextContent());
                //System.out.println(nodeList.item(i).getTextContent());
            }


            // Store values in ttl:
            IRI geographicRegion;
            if (level == 1)
                geographicRegion = SKIO.CONTINENT;
            else if (level == 2)
                geographicRegion = SKIO.COUNTRY;
            else
                geographicRegion = SKIO.REGION;
            builder.defaultGraph()
                    .subject(SKIO.PREFIX + ":" + name.replaceAll(" ","_"))
                    .add(RDF.TYPE, geographicRegion)
                    .add(FOAF.NAME, name)
                    .add(DCTERMS.IDENTIFIER, counter);


            // Get next counter(identifier) and filename
            counter++;
            pathname = REGIONS_OUT + counter + ".xml";
        }
    }


    private static void buildResorts(int startFrom) {

    }
}
