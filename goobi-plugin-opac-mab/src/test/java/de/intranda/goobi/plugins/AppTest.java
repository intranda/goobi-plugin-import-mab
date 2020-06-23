//package de.intranda.goobi.plugins;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.List;
//
//import javax.xml.parsers.ParserConfigurationException;
//
//import org.apache.commons.configuration.ConfigurationException;
//import org.jdom2.Document;
//import org.jdom2.JDOMException;
//import org.jdom2.output.Format;
//import org.jdom2.output.XMLOutputter;
//import org.junit.Assert;
//import org.xml.sax.SAXException;
//
//import de.intranda.goobi.plugins.parser.DataManager;
//import de.intranda.goobi.plugins.parser.ParserMab2MPI;
//import de.intranda.goobi.plugins.parser.ParsingUtils;
//import de.intranda.goobi.plugins.parser.model.Result;
//import junit.framework.Test;
//import junit.framework.TestCase;
//import junit.framework.TestSuite;
//import ugh.exceptions.PreferencesException;
//import ugh.exceptions.UGHException;
//
///**
// * Unit test for simple App.
// */
//public class AppTest 
//    extends TestCase
//{
//    /**
//     * Create the test case
//     *
//     * @param testName name of the test case
//     */
//    public AppTest( String testName )
//    {
//        super( testName );
//    }
//
//    /**
//     * @return the suite of tests being tested
//     */
//    public static Test suite()
//    {
//        return new TestSuite( AppTest.class );
//    }
//
//    public void testApp() throws ConfigurationException, UGHException, IOException, ParserConfigurationException, SAXException 
//    {
//       MakeMetsMods maker = new MakeMetsMods("resources/plugin_intranda_opac_mab.xml");
//    
//       maker.saveMMFile("resources/mab2.txt", "resources/test");
//    }
//    
//    
//    
//    /**
//     * Rigourous Test :-)
//     * @throws IOException 
//     * @throws JDOMException 
//     * @throws ConfigurationException 
//     */
//    public void testOld() throws JDOMException, IOException, ConfigurationException
//    {
////        String strMab = new String(Files.readAllBytes(Paths.get("resources/mab.txt")), StandardCharsets.UTF_8);
////        
////        List<Result> results = ParserMab2MPI.parseResult(strMab, false);
////        
////        Document doc =  results.get(0).getDoc();
////        
////        assertTrue( true );   
//        
//        DataManager.getInstance().loadTagsMab2("resources/tags_mab2.properties");
//        File mabFile = new File("resources/mab.txt");
//        String strXmlfile = "resources/mab.xml";
//
//        Assert.assertTrue(mabFile.isFile());
//        String mab2 = ParsingUtils.readFileToString(mabFile);
//        List<Result> results = ParserMab2MPI.parseResult(mab2, false);
//
//        Result result = results.get(0);
//        //        ParserMarcXml.addAdditionalMetatada(result.getDoc(), cat, ParserMarc21.MARC_NAMEPSACE);
//        Document doc = result.getDoc();
//
//        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
//        //output xml to console for debugging
//        //xmlOutputter.output(doc, System.out);
//        xmlOutputter.output(doc, new FileOutputStream(strXmlfile));
//        
//    }
//}
