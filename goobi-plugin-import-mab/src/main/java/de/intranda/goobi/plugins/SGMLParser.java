package de.intranda.goobi.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
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
    private SubnodeConfiguration config;

    private String strConfigRulesetPath = "rulesetPath";
    private String strConfigOutputPath = "outputPath";
    private String strConfigImagePathFile = "imagePathFile";
    private Prefs prefs;
    private int iCurrentPageNo;
    private DocStruct physical;
    private DocStruct logical;
    private DocStruct currentVolume;

    private String strCurrentId;

    public SGMLParser(SubnodeConfiguration config) throws ConfigurationException, PreferencesException {

        this.config = config;
        strOutputPath = config.getString(strConfigOutputPath);
        strImagePath = config.getString(strConfigImagePathFile);
        prefs = new Prefs();
        prefs.loadPrefs(config.getString(strConfigRulesetPath));
    }

    public void addSGML(MetsMods mm, DocStruct currentVolume, String strId) throws IOException, UGHException {

        iCurrentPageNo = 1;
        this.mm = mm;
        this.dd = mm.getDigitalDocument();
        this.physical = dd.getPhysicalDocStruct();
        this.logical = dd.getLogicalDocStruct();
        this.currentVolume = currentVolume;

        //        String strId = mm.getGoobiID();
        this.strCurrentId = strId;

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

        Boolean boWithEbind = false;

        //For Diss, look like this:
        for (Element elt1 : elt.getElementsByTag("ebind")) {
            boWithEbind = true;

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
                    if (currentVolume != null) {
                        currentVolume.addChild(dsEintrag);
                    } else {
                        logical.addChild(dsEintrag);
                    }
                }

            }

        }

        //for Privatrecht, looks like this:
        if (!boWithEbind) {
            for (Element elt1 : elt.getElementsByTag("body")) {
                Elements elts = elt1.children();
                for (Element elt2 : elts) {

                    if (elt2.tagName().equalsIgnoreCase("div")) {
                        DocStruct dsEintrag = addDiv(elt2);
                        if (currentVolume != null) {
                            currentVolume.addChild(dsEintrag);
                        } else {
                            logical.addChild(dsEintrag);
                        }
                    }

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
                        if (currentVolume != null) {
                            currentVolume.addReferenceTo(page, "logical_physical");
                            dsEintrag.addReferenceTo(page, "logical_physical");
                        } else {
                            logical.addReferenceTo(page, "logical_physical");
                            dsEintrag.addReferenceTo(page, "logical_physical");
                        }
                    }

                    //                        logical.addReferenceTo(page, "logical_physical");
                    //                        dsEintrag.addReferenceTo(page, "logical_physical");
                }
            }
        }

        return dsEintrag;
    }

    private void addHeader(Element eltHeader) throws MetadataTypeNotAllowedException {

        DocStruct docStruct = logical;
        if (currentVolume != null) {
            docStruct = currentVolume;
        }

        for (Element elt : eltHeader.getAllElements()) {

            String strName = elt.tagName();
            if (strName.equalsIgnoreCase("titlestmt")) {

                for (Element eltTitle : elt.getElementsByTag("titleproper")) {
                    MetadataType typeTitle = prefs.getMetadataTypeByName("TitleDocMain");
                    Metadata mdTitle = new Metadata(typeTitle);

                    if (docStruct.getAllMetadataByType(typeTitle).size() != 0) {
                        typeTitle = prefs.getMetadataTypeByName("TitleDocSub1");
                        mdTitle = new Metadata(typeTitle);
                    }

                    mdTitle.setValue(eltTitle.text());
                    docStruct.addMetadata(mdTitle);
                }

//                for (Element eltTitle : elt.getElementsByTag("author")) {
//                    MetadataType typeTitle = prefs.getMetadataTypeByName("Author");
//                    Metadata mdTitle = new Metadata(typeTitle);
//
//                    //                        if (docStruct.getAllMetadataByType(typeTitle).size() == 0) {
//                    mdTitle.setValue(eltTitle.text());
//
//                    docStruct.addMetadata(mdTitle);
//                    //                        }
//
//                }
            }

            if (strName.equalsIgnoreCase("publicationstmt")) {

                for (Element eltTitle : elt.getElementsByTag("pubplace")) {
                    MetadataType typeTitle = prefs.getMetadataTypeByName("PlaceOfPublication");
                    Metadata mdTitle = new Metadata(typeTitle);
                    mdTitle.setValue(eltTitle.text());
                    docStruct.addMetadata(mdTitle);
                }

                for (Element eltTitle : elt.getElementsByTag("date")) {
                    MetadataType typeTitle = prefs.getMetadataTypeByName("PublicationYear");
                    Metadata mdTitle = new Metadata(typeTitle);
                    mdTitle.setValue(eltTitle.text());

                    docStruct.addMetadata(mdTitle);
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
        String strImageFolder = strOutputPath + this.strCurrentId + "/images/";
        new File(strImageFolder).mkdirs();

        //find the file: it has 8 digits.
        String seqNo = elt1.attr("seqno");
        String strFile = seqNo + ".tif";
        int digits = seqNo.length();
        for (int i = digits; i < 8; i++) {
            strFile = "0" + strFile;
        }

        String strFilePath = strImagePath + this.strCurrentId + "/" + strFile;

        File fileCopy = null;

        //copy original file:
        String strMasterPrefix = "master_";
        String strMediaSuffix = "_media";
        String strMasterPath = strImageFolder + strMasterPrefix + this.strCurrentId + strMediaSuffix + File.separator;
        //        String strNormalPath = strImageFolder +this.strCurrentId  + strMediaSuffix + File.separator;

        new File(strMasterPath).mkdirs();
        //        new File(strNormalPath).mkdirs();

        Path pathSource = Paths.get(strFilePath);
        Path pathDest = Paths.get(strMasterPath + strFile);

        //if no image, return null:
        File image = new File(pathSource.toString());
        if (!image.exists()) {
            return null;
        }

        Files.copy(pathSource, pathDest, StandardCopyOption.REPLACE_EXISTING);

        //        Path pathDest2 = Paths.get(strNormalPath + pathSource.getFileName());
        //        Files.copy(pathSource, pathDest2, StandardCopyOption.REPLACE_EXISTING);

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

    private Document getDoc(final String html) {
        final Document document = Jsoup.parse(html);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        return document;
    }

}
