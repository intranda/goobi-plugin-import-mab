package de.intranda.goobi.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.UGHException;
import ugh.fileformats.mets.MetsMods;

public class SGMLParser {

    private String strOutputPath;
    private String strImagePath;

    private MetsMods mm;
    private DigitalDocument dd;
    private XMLConfiguration config;

    private String strConfigRulesetPath = "rulesetPath";
    private String strConfigOutputPath = "outputPath";
    private String strConfigImagePathFile = "imagePathFile";
    private Prefs prefs;
    private int iCurrentPageNo;
    private DocStruct physical;
    private DocStruct logical;

    //    ///Test
    //    public static void main(String[] args) throws IOException, UGHException, ConfigurationException {
    //        File sgml = new File("/home/joel/git/rechtsgeschichte/20190211 Datenlieferung/Testdaten Privatrecht 2019-01/sgml/37876.sgm");
    //
    //        SGMLParser parser = new SGMLParser("/home/joel/git/rechtsgeschichte/testdiss/test.xml");
    //        parser.parse(sgml);
    //    }

    public SGMLParser(String strConfigFile) throws ConfigurationException, PreferencesException {
        config = new XMLConfiguration(strConfigFile);
        strOutputPath = config.getString(strConfigOutputPath);
        strImagePath = config.getString(strConfigImagePathFile);
        prefs = new Prefs();
        prefs.loadPrefs(config.getString(strConfigRulesetPath));
    }

    public void addSGML(MetsMods mm) throws IOException, UGHException {

        iCurrentPageNo = 1;
        this.mm = mm;
        this.dd = mm.getDigitalDocument();
        this.physical = dd.getPhysicalDocStruct();
        this.logical = dd.getLogicalDocStruct();
        String strId = mm.getGoobiID();
        File sgml = new File(config.getString("sgmlPath") + strId + ".sgm");

        if (sgml.exists()) {
            parse(sgml);
        }
    }

    private void parse(File sgml) throws IOException, UGHException {

        String text = ParsingUtils.readFileToString(sgml);

        Document doc = getDoc(text);

        //        //for testing
        //        final File f = new File("/home/joel/git/rechtsgeschichte/test/html.txt");
        //        FileUtils.writeStringToFile(f, doc.outerHtml(), "UTF-8");
        //        //

        for (Element elt : doc.getElementsByTag("html")) {

            parse(elt);
            break;
        }
    }

    public void parse(Element elt) throws UGHException, IOException {

        for (Element elt1 : elt.getElementsByTag("ebind")) {
            for (Element eltHeader : elt1.getElementsByTag("ebindheader")) {
                for (Element eltDesc : eltHeader.getElementsByTag("filedesc")) {

                    addHeader(eltDesc);
                    break;
                }
            }

            Elements elts = elt1.children();
            for (Element elt2 : elts) {

                if (elt2.tagName().equalsIgnoreCase("div")) {
                    DocStruct dsEintrag = addDiv(elt2);
                    logical.addChild(dsEintrag);
                }

            }

        }
    }

    private DocStruct addDiv(Element elt2) throws UGHException, IOException {

        DocStruct dsEintrag = dd.createDocStruct(prefs.getDocStrctTypeByName(getDocStructName(elt2)));
        //metadata:
        if (dsEintrag.getType().getName().equalsIgnoreCase("Chapter") || dsEintrag.getType().getName().equalsIgnoreCase("OtherDocStrct")) {
            MetadataType type = prefs.getMetadataTypeByName("TitleDocMain");
            Metadata md = new Metadata(type);
            md.setValue(elt2.text());
            dsEintrag.addMetadata(md);
        }

        Elements children = elt2.children();
        for (Element eltPage : children) {

            if (eltPage.tagName().equalsIgnoreCase("div")) {
                dsEintrag.addChild(addDiv(eltPage));
            }

            if (eltPage.tagName().equalsIgnoreCase("page")) {

                for (Element eltImg : eltPage.getElementsByTag("img")) {
                    DocStruct page = getAndSavePage(eltImg);
                    if (page != null) {
                        physical.addChild(page);
                        //                        logical.addReferenceTo(page, "logical_physical");
                        dsEintrag.addReferenceTo(page, "logical_physical");
                    }
                }
            }
        }

        return dsEintrag;
    }

    private void addHeader(Element eltHeader) throws MetadataTypeNotAllowedException {

        for (Element elt : eltHeader.getAllElements()) {

            String strName = elt.tagName();
            if (strName.equalsIgnoreCase("titlestmt")) {

                for (Element eltTitle : elt.getElementsByTag("titleproper")) {
                    MetadataType typeTitle = prefs.getMetadataTypeByName("TitleDocMain");
                    Metadata mdTitle = new Metadata(typeTitle);

                    if (logical.getAllMetadataByType(typeTitle).size() == 0) {
                        mdTitle.setValue(eltTitle.text());
                        logical.addMetadata(mdTitle);
                    }
                }

                for (Element eltTitle : elt.getElementsByTag("author")) {
                    MetadataType typeTitle = prefs.getMetadataTypeByName("Author");
                    Metadata mdTitle = new Metadata(typeTitle);

                    if (logical.getAllMetadataByType(typeTitle).size() == 0) {
                        mdTitle.setValue(eltTitle.text());
                        logical.addMetadata(mdTitle);
                    }
                }
            }

            if (strName.equalsIgnoreCase("publicationstmt")) {

                for (Element eltTitle : elt.getElementsByTag("pubplace")) {
                    MetadataType typeTitle = prefs.getMetadataTypeByName("PlaceOfPublication");
                    Metadata mdTitle = new Metadata(typeTitle);

                    if (logical.getAllMetadataByType(typeTitle).size() == 0) {
                        mdTitle.setValue(eltTitle.text());
                        logical.addMetadata(mdTitle);
                    }
                }

                for (Element eltTitle : elt.getElementsByTag("date")) {
                    MetadataType typeTitle = prefs.getMetadataTypeByName("PublicationYear");
                    Metadata mdTitle = new Metadata(typeTitle);

                    if (logical.getAllMetadataByType(typeTitle).size() == 0) {
                        mdTitle.setValue(eltTitle.text());
                        logical.addMetadata(mdTitle);
                    }
                }
            }

        }
    }

    private String getDocStructName(Element elt1) {

        String strName = elt1.text();
        if (strName.equalsIgnoreCase("[Gesamttitelblatt]") || strName.equalsIgnoreCase("[Titelblatt]")) {

            return "TitlePage";
        }
        if (strName.equalsIgnoreCase("Vorwort")) {

            return "Preface";
        }
        if (strName.equalsIgnoreCase("Inhaltsverzeichnis")) {

            return "Index";
        }

        //otherwise:
        for (Element child : elt1.children()) {

            if (child.tagName().equalsIgnoreCase("div")) {
                return "PartOfWork";
            }
        }
        return "Chapter";
    }

    /**
     * Find the specified image file in the hashmap. If it is there, copy the file to a (new, if necessary) subfolder of the main folder, named after
     * the ID of the MetsMods file. In this folder, .tif files are saved in a subfolder "master_MMName_media", .jpgs in "MMName", so that import to
     * Goobi works. Return a new DocStruct with the filename and the location of the file.
     * 
     * If page is not null, then a page for this image already exists, and the image (derivative) should be added to it, and null returned.
     * 
     * @param strDatei
     * @param mm
     * @return
     * @throws UGHException
     * @throws IOException
     */
    private DocStruct getAndSavePage(Element elt1) throws UGHException, IOException {

        //create subfolder for images, as necessary:
        String strImageFolder = strOutputPath + mm.getGoobiID() + "/images/";
        new File(strImageFolder).mkdirs();

        //find the file: it has 8 digits.
        String seqNo = elt1.attr("seqno");
        String strFile = seqNo + ".tif";
        int digits = seqNo.length();
        for (int i = digits; i < 8; i++) {
            strFile = "0" + strFile;
        }

        String strFilePath = strImagePath + mm.getGoobiID() + "/" + strFile;

        File fileCopy = null;

        //copy original file:
        String strMasterPrefix = "master_";
        String strMediaSuffix = "_media";
        String strMasterPath = strImageFolder + strMasterPrefix + mm.getGoobiID() + strMediaSuffix + File.separator;
        String strNormalPath = strImageFolder + mm.getGoobiID() + strMediaSuffix + File.separator;

        new File(strMasterPath).mkdirs();
        new File(strNormalPath).mkdirs();

        Path pathSource = Paths.get(strFilePath);
        Path pathDest = Paths.get(strMasterPath + strFile);

        //if no image, return null:
        File image = new File(pathSource.toString());
        if (!image.exists()) {
            return null;
        }
        
        
        Files.copy(pathSource, pathDest, StandardCopyOption.REPLACE_EXISTING);
        fileCopy = new File(pathDest.toString());

        DocStructType pageType = prefs.getDocStrctTypeByName("page");
        DocStruct dsPage = mm.getDigitalDocument().createDocStruct(pageType);

        //physical page number : just increment for this folio
        MetadataType typePhysPage = prefs.getMetadataTypeByName("physPageNumber");
        Metadata mdPhysPage = new Metadata(typePhysPage);
        mdPhysPage.setValue(String.valueOf(this.iCurrentPageNo));
        dsPage.addMetadata(mdPhysPage);

        //logical page number : take the name from the img
        MetadataType typeLogPage = prefs.getMetadataTypeByName("logicalPageNumber");
        Metadata mdLogPage = new Metadata(typeLogPage);

        String strPage = elt1.attr("nativeno");

        mdLogPage.setValue(strPage);
        dsPage.addMetadata(mdLogPage);

        iCurrentPageNo++;

        ContentFile cf = new ContentFile();
        if (SystemUtils.IS_OS_WINDOWS) {
            cf.setLocation("file:" + fileCopy.getCanonicalPath());
        } else {
            cf.setLocation("file:/" + fileCopy.getCanonicalPath());
        }
        dsPage.addContentFile(cf);
        dsPage.setImageName(fileCopy.getName());

        return dsPage;

    }

    //        String strXHTML = htmlToXhtml(text);
    //        String strXML = convertSGMLtoXML(strXHTML);
    //
    //        InputStream stream = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
    //        SAXBuilder builder = new SAXBuilder();
    //
    //        Document doc = (Document) builder.build(stream);
    //
    //        Element rootNode = doc.getRootElement();
    //
    //        for (Element elt : rootNode.getChildren()) {
    //
    //            System.out.println(elt.getName());
    //        }
    //
    //    }

    private Document getDoc(final String html) {
        final Document document = Jsoup.parse(html);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        return document;
    }

    //    private String htmlToXhtml(final String html) {
    //        final org.jsoup.nodes.Document document = Jsoup.parse(html);
    //        document.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
    //        return document.html();
    //    }

    //    private String convertSGMLtoXML(String text) {
    //
    //        String strXML = text.replace("<!DOCTYPE ebind PUBLIC \"-//UC Berkeley//DTD ebind.dtd (Electronic Binding (Ebind))//EN\">", "");
    ////        String strXML = text.replace("<!DOCTYPE EBIND PUBLIC \"-//UC Berkeley//DTD ebind.dtd (Electronic Binding (Ebind))//EN\">", "<sgml>");
    //        strXML = strXML.trim();
    //
    ////        strXML = "<?xml version=\"1.0\"?>" + System.lineSeparator() + strXML;
    ////
    ////        strXML = strXML.replace("ß", "&szlig");
    ////        strXML = strXML.replace("&", "&amp;");
    ////        strXML = strXML.replace("></PAGE>", "/></PAGE>");
    //
    ////        strXML = strXML + System.lineSeparator() + "</sgml>";
    //
    //        return strXML;
    //    }

}
