package webscraper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


// Requires java to trust the SSL certificate used in skimap.org
// if "unable to find valid certification path to requested target" error is thrown:
// 1. Download the site certificate.cer using your browser (if valid!)
// 2. Import it using java keytool (default password is changeit):
// keytool -import -trustcacerts -alias myTrustedCert -file certificate.cer -keystore "JAVA_HOME/jre/lib/security/cacerts"

public class ResortDownloader {

    private static final String REGIONS = "https://skimap.org/Regions/view/";
    private static final String SKI_AREAS = "https://skimap.org/SkiAreas/view/";
    private static final String SKI_MAPS = "https://skimap.org/SkiMaps/view/";
    private static final String REGIONS_OUT = "src/main/resources/xml-regions/";
    private static final String SKI_AREAS_OUT = "src/main/resources/xml-skiareas/";
    private static final String SKI_MAPS_OUT = "src/main/resources/xml-maps/";

    public static void main(String[] args) {
        scrapeData(REGIONS, REGIONS_OUT, 1);
        scrapeData(SKI_AREAS, SKI_AREAS_OUT, 1);
        scrapeData(SKI_MAPS, SKI_MAPS_OUT, 22); // for some reason ski areas start from 22.xml
    }

    /**
     * Scrapes documents from {startFrom}.xml until empty document is returned (the same as 0_empty.xml)
     * @param source URL
     * @param destination Directory
     * @param startFrom first document
     */
    private static void scrapeData(String source, String destination, int startFrom) {
        URL src;
        File dest;
        File emptyResults;
        int counter = -1;
        try {
            src = new URL(source +"0.xml");
            emptyResults = new File(destination + "0_empty.xml");
            FileUtils.copyURLToFile(src, emptyResults);

            counter = startFrom;
            do {
                src = new URL(source + counter + ".xml");
                dest = new File(destination + counter + ".xml");
                FileUtils.copyURLToFile(src, dest);
                counter++;
            } while (!FileUtils.contentEquals(emptyResults, dest));
            dest.delete(); //Last file is empty

        } catch (MalformedURLException e) {
            System.out.println(counter + " Malformed URL");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(counter + " IO error");
            e.printStackTrace();
        }
        System.out.println("Processed " + (counter - startFrom - 1) + " of " + source);
    }
}
