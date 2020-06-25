package de.intranda.goobi.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;
import org.jdom2.JDOMException;
import org.xml.sax.SAXException;

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

public class MakeMetsMods {

    //these are the xml config fields:
    private String strRulesetPath = "rulesetPath";
    private String strOutputPath = "outputPath";
    private String strImagePathFile = "imagePathFile";

    private String strCurrentPath;
    private SGMLParser sgmlParser;

    //
    private Prefs prefs;
    private HashMap<String, String> mapTags;
    private XMLConfiguration config;
    private ArrayList<MetsMods> lstMM;

    private Boolean boWithSGML;
    //keep track of id numbers:
    ArrayList<String> lstIds;

    //and page numbers:
    int iCurrentPageNo = 1;

    //and missing files:
    ArrayList<String> lstMissingFiles;

    //and all top level metadata:
    ArrayList<String> lstTopLevelMetadata;

    public static void main(String[] args) throws ConfigurationException, ParserConfigurationException, SAXException, IOException, UGHException, JDOMException {

        String strConfig = "/home/joel/git/rechtsgeschichte/testprivr/privrecht-config.xml";
//        String strConfig = "/home/joel/git/rechtsgeschichte/testdiss/diss-config.xml";
        
        if (args.length > 0) {
            strConfig = args[0];
        }
        
        //test diss:
//        MakeMetsMods maker = new MakeMetsMods("resources/plugin_intranda_opac_mab.xml");
//        maker.saveMMFile("resources/mab2-complete.txt", "/home/joel/git/rechtsgeschichte/test");
        
        MakeMetsMods maker = new MakeMetsMods(strConfig);

        maker.saveMMFile(maker.config.getString("mabFile"), maker.config.getString("outputPath"));
                
//        maker.saveMMFile("/home/joel/git/rechtsgeschichte/testdiss/privatr-test.txt", "/home/joel/git/rechtsgeschichte/testdiss");
    }

    /**
     * Constructor; takes path to the config file.
     * 
     * @param strConfigFile
     * @throws PreferencesException
     * @throws ConfigurationException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public MakeMetsMods(String strConfigFile)
            throws PreferencesException, ConfigurationException, ParserConfigurationException, SAXException, IOException {

        config = new XMLConfiguration(strConfigFile);
        lstMM = new ArrayList<MetsMods>();
        this.prefs = new Prefs();
        prefs.loadPrefs(config.getString(strRulesetPath));
        lstIds = new ArrayList<String>();
        lstMissingFiles = new ArrayList<String>();
        lstTopLevelMetadata = new ArrayList<String>();

        boWithSGML = config.getBoolean("withSGML");
        if (boWithSGML) {
            sgmlParser = new SGMLParser(strConfigFile);
        }

        readTagsList(config.getString("tags"));

    }

    public void saveMMFile(String mabFile, String strFolderForMM) throws UGHException, IOException, JDOMException {

        parse(mabFile);
    }

    public void parse(String mabFile) throws IOException, UGHException, JDOMException {

        String text = ParsingUtils.readFileToString(new File(mabFile));

        if ((text != null) && (text.length() != 0)) {

            MetsMods mm = makeMM(config.getString("defaultPublicationType"));
            DocStruct logical = mm.getDigitalDocument().getLogicalDocStruct();

            BufferedReader reader = new BufferedReader(new StringReader(text));
            String str = "";

            while ((str = reader.readLine()) != null) {
                str = str.trim();

                //finished one ?
                if (str.length() == 0) {

                    if (boWithSGML) {
                        sgmlParser.addSGML(mm);
                    }
                    
                    saveMM(mm, strCurrentPath);

                    //start next
                    mm = makeMM(config.getString("defaultPublicationType"));
                    logical = mm.getDigitalDocument().getLogicalDocStruct();
                }

                if (str.length() < 4) {
                    continue;
                }

                String tag = str.substring(0, 4);

                try {

                    if (str.length() > 5) {
                        // Data field
                        int iValue = str.indexOf(":");
                        String content = str.substring(iValue + 1, str.length());

                        if (!boWithSGML && tag.equals("1040")) {
                            addImageFiles(content, mm);
                            continue;
                        }

                        Metadata md = getMetadata(tag);
                        if (md != null) {
                            md.setValue(content);
                            logical.addMetadata(md);

                            //set GoobiId:
                            if (md.getType().getName().equals("CatalogIDDigital")) {
                                mm.setGoobiID(content);
                                String strFolder = config.getString(strOutputPath);
                                if (!strFolder.endsWith("/")) {
                                    strFolder = strFolder + "/";
                                }
                                strCurrentPath = strFolder + content + "/";
                                new File(strCurrentPath).mkdir();
                            }
                        }

                    }
                } catch (Exception e) {
                    // TODO: handle exception
                   System.out.println(e.getMessage());
                }
            }

            

        }
    }

    private Metadata getMetadata(String tag) throws MetadataTypeNotAllowedException {

        for (String id : mapTags.keySet()) {

            if (id.contentEquals(tag)) {

                String strElt = mapTags.get(id);
                MetadataType type = prefs.getMetadataTypeByName(strElt);
                return new Metadata(type);
            }
        }

        //not found?
        return null;
    }

    /**
     * Find the specified image file in the list of files and paths. If it does not exist, save it in a text file ""MISSING_Urkunden.txt" etc
     * 
     * @param strFilename
     * @param mm
     * @param physical
     * @throws UGHException
     * @throws IOException
     */
    private void addImageFiles(String strValue, MetsMods mm) throws UGHException, IOException {

        String strTitel = "Dateinamen Bilder Dissprojekt: Titelblatt: ";
        if (!strValue.startsWith(strTitel)) {
            return;
        }

        String strRem = strValue.replace(strTitel, "");

        ArrayList<String> lstImages = new ArrayList<String>();

        if (!strRem.contains(";")) {
            lstImages.add(strRem);
        } else {
            String[] lstStrings = strRem.split(" ; ");
            for (int i = 0; i < lstStrings.length; i++) {
                String strImage = lstStrings[i].replace("Widmung01: ", "");
                strImage = strImage.replace("Widmung02: ", "");
                lstImages.add(strImage.trim());
            }
        }

        for (String strImage : lstImages) {
            DigitalDocument dd = mm.getDigitalDocument();
            DocStruct physical = dd.getPhysicalDocStruct();
            DocStruct logical = dd.getLogicalDocStruct();

            String strFilename = config.getString(strImagePathFile) + strImage + ".jpg";

            DocStruct page = null;

            //this returns null if the page already exists; in that case, add the file to the existing page
            page = getAndSavePage(strFilename, mm, page);
            if (page != null) {
                physical.addChild(page);
                logical.addReferenceTo(page, "logical_physical");
            }
        }
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
    private DocStruct getAndSavePage(String strDatei, MetsMods mm, DocStruct page) throws UGHException, IOException {

        File file = new File(strDatei);
        if (!file.exists()) {
            return null;
        }

        //otherwise:        
        //create subfolder for images, as necessary:
        String strImageFolder = strCurrentPath + "/images/";
        new File(strImageFolder).mkdirs();

        //copy original file:
        String strMasterPrefix = "master_";
        String strMediaSuffix = "_media";
        String strMasterPath = strImageFolder + strMasterPrefix + mm.getGoobiID() + strMediaSuffix + File.separator;
        String strNormalPath = strImageFolder + mm.getGoobiID() + strMediaSuffix + File.separator;

        new File(strMasterPath).mkdirs();
        new File(strNormalPath).mkdirs();

        Path pathSource = Paths.get(strDatei);
        Path pathDest = Paths.get(strNormalPath + strDatei.toLowerCase());

        //first aufruf: make the master file
        if (page == null) {
            pathDest = Paths.get(strMasterPath + pathSource.getFileName());
        }

        Files.copy(pathSource, pathDest, StandardCopyOption.REPLACE_EXISTING);
        Files.copy(pathSource, Paths.get(strNormalPath + pathSource.getFileName()), StandardCopyOption.REPLACE_EXISTING);

        File fileCopy = new File(pathDest.toString());

        //first time for this image?
        if (page == null) {
            DocStructType pageType = prefs.getDocStrctTypeByName("page");
            DocStruct dsPage = mm.getDigitalDocument().createDocStruct(pageType);

            //physical page number : just increment for this folio
            MetadataType typePhysPage = prefs.getMetadataTypeByName("physPageNumber");
            Metadata mdPhysPage = new Metadata(typePhysPage);
            mdPhysPage.setValue(String.valueOf(this.iCurrentPageNo));
            dsPage.addMetadata(mdPhysPage);

            //logical page number : take the file name
            MetadataType typeLogPage = prefs.getMetadataTypeByName("logicalPageNumber");
            Metadata mdLogPage = new Metadata(typeLogPage);

            String strPage = FilenameUtils.removeExtension(strDatei);

            //remove the ID number and leading 0s from the beginning:
            if (mm.getGoobiID() != null && mm.getGoobiID().length() > 3) {
                String strId = mm.getGoobiID();

                strPage = strPage.replace(strId, "");
                strPage = strPage.replaceFirst("^0+(?!$)", "");
            }

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
        } else {

            //TODO: why is the whole path written out here, but not above??
            ContentFile cf = new ContentFile();
            if (SystemUtils.IS_OS_WINDOWS) {
                cf.setLocation("file:" + fileCopy.getCanonicalPath());
            } else {
                cf.setLocation("file:/" + fileCopy.getCanonicalPath());
            }
            page.addContentFile(cf);
            page.setImageName(fileCopy.getName());
            return null;
        }

    }

    /**
     * Using ExamineXML, we have created a file listing image files together with their paths. This method reads that file, taking it into a local
     * hashmap to be used in the xml conversion.
     * 
     * @param strFileList
     * @throws IOException
     */
    private void readTagsList(String strFileList) throws IOException {

        File toRead = new File(strFileList);
        FileInputStream fis = new FileInputStream(toRead);

        try {
            Scanner sc = new Scanner(fis);

            mapTags = new HashMap<String, String>();

            //read data from file line by line:
            String currentLine;
            while (sc.hasNextLine()) {
                currentLine = sc.nextLine();
                
                if (currentLine.length()<3) {
                    continue;
                }
                
                //now tokenize the currentLine:
                StringTokenizer st = new StringTokenizer(currentLine, " ", false);
                //put tokens ot currentLine in map
                mapTags.put(st.nextToken(), st.nextToken());
            }
        } finally {
            fis.close();
        }
    }

    //    public void asfasf() {
    //
    //        MetsMods mmAkte = makeMM("Dissertation");
    //
    //        DigitalDocument dd = mmAkte.getDigitalDocument();
    //        DocStruct logical = dd.getLogicalDocStruct();
    //        logical.addMetadataGroup(mdAkten);
    //        logical.addMetadataGroup(mdBestand);
    //        logical.addMetadataGroup(mdSystem);
    //        logical.addMetadataGroup(mdKlass);
    //
    //        //make ID
    //        makeId(mmAkte, nodeAkte);
    //
    //        //add the metadata and the groups:
    //        List<Element> lstChildren = nodeAkte.getChildren();
    //
    //        for (int k = 0; k < lstChildren.size(); k++) {
    //
    //            Element node = (Element) lstChildren.get(k);
    //
    //            //for leaves:
    //            if (node.getChildren().size() == 0) {
    //                String strName = getTopLevelName(node.getName());
    //                MetadataType type = prefs.getMetadataTypeByName(strName);
    //                Metadata md = new Metadata(type);
    //                md.setValue(node.getValue());
    //                logical.addMetadata(md);
    //                continue;
    //            }
    //
    //            //for branches:
    //            if (node.getName().contentEquals("folio")) {
    //
    //                addFolio(node, mmAkte, dd);
    //
    //            } else if (node.getName().contentEquals("ortsreg")) {
    //
    //                MetadataGroup mdOrtsreg = maker.getMetadata(node);
    //                logical.addMetadataGroup(mdOrtsreg);
    //
    //            } else if (node.getName().contentEquals("vorsignatur")) {
    //
    //                MetadataGroup mdPersreg = maker.getMetadata(node);
    //                logical.addMetadataGroup(mdPersreg);
    //
    //            } else if (node.getName().contentEquals("persreg")) {
    //
    //                MetadataGroup mdPersreg = maker.getMetadata(node);
    //                logical.addMetadataGroup(mdPersreg);
    //
    //            } else if (node.getName().contentEquals("sachreg")) {
    //
    //                MetadataGroup mdPersreg = maker.getMetadata(node);
    //                logical.addMetadataGroup(mdPersreg);
    //
    //            } else {
    //                throw new UGHException("Missed a metadata");
    //            }
    //        }
    //
    //        saveMM(mmAkte, strFolderForMM);
    //    }

    /**
     * Make a Mets/Mods object, and add it to the physical DocStruct as a BoundBook.
     * 
     * @param strType
     * @return
     * @throws UGHException
     */
    private MetsMods makeMM(String strType) throws UGHException {

        MetsMods newMM = new MetsMods(prefs);
        DigitalDocument newDD = new DigitalDocument();

        newMM.setDigitalDocument(newDD);
        DocStruct logical = newDD.createDocStruct(prefs.getDocStrctTypeByName(strType));
        newDD.setLogicalDocStruct(logical);

        DocStruct physical = newDD.createDocStruct(prefs.getDocStrctTypeByName("BoundBook"));
        newDD.setPhysicalDocStruct(physical);

        //needed for some reason in Goobi:
        MetadataType MDTypeForPath = prefs.getMetadataTypeByName("pathimagefiles");

        // check for valid filepath
        try {
            List<? extends Metadata> filepath = physical.getAllMetadataByType(MDTypeForPath);
            if (filepath == null || filepath.isEmpty()) {
                Metadata mdForPath = new Metadata(MDTypeForPath);
                if (SystemUtils.IS_OS_WINDOWS) {
                    mdForPath.setValue("file:/");
                } else {
                    mdForPath.setValue("file://");
                }
                physical.addMetadata(mdForPath);
            }
        } catch (Exception e) {
            throw new UGHException(e);
        }

        return newMM;
    }

    private void saveMM(MetsMods mmNew, String strFolderForMM) throws UGHException {

        lstMM.add(mmNew);
        String strFolder = strFolderForMM;

        if (!strFolder.endsWith("/")) {
            strFolder = strFolder + "/";
        }

        new File(strFolder).mkdirs();

        String strFilename = strFolder + "meta.xml";
        mmNew.write(strFilename);

        //reset page numbers:
        this.iCurrentPageNo = 1;
    }
}