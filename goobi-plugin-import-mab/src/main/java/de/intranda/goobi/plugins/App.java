package de.intranda.goobi.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.jdom2.JDOMException;
import org.xml.sax.SAXException;

import ugh.exceptions.UGHException;

public class App {

    public App() {
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) throws ConfigurationException, ParserConfigurationException, SAXException, IOException, UGHException,
            JDOMException, TransformerException {

//        String strDocCheck = "/home/joel/git/rechtsgeschichte/privrecht/priv-all.txt";
//        String strMultInheritance = "/home/joel/git/rechtsgeschichte/privrecht/multiple-inheritance.txt";
//
//        collectMultipleInheritance(strDocCheck, strMultInheritance);

                //        String strDocType = "MehrbdWerk";
                //
                //        outputDocTypes(strDocCheck, strDocType);
                //        if (true) {
                //            return;
                //        }
        
                //        String strConfig = "/home/joel/git/rechtsgeschichte/testdiss/diss-config.xml";
                //        String strConfig = "/home/joel/git/rechtsgeschichte/testprivr/privrecht-config.xml";
                //        String strConfig = "/home/joel/git/rechtsgeschichte/data/config.xml";
                //        String strConfig = "/home/joel/git/rechtsgeschichte/privrecht/test/zusatz-config.xml";
                String strConfig = "/home/joel/git/rechtsgeschichte/privrecht/test/test-config.xml";
        
//                String strConfig = "/home/joel/git/rechtsgeschichte/diss/diss-config.xml";
        
                //        String strConfig = "";
                if (args.length > 0) {
                    strConfig = args[0];
                }
        
//                        args = new String[2];
//                        args[0] = strConfig;
//                        args[1] = "/home/joel/git/rechtsgeschichte/diss/diss-all.txt";
        
                XMLConfiguration xmlConfig = new XMLConfiguration(strConfig); //ConfigPlugins.getPluginConfig("whatever");
                xmlConfig.setExpressionEngine(new XPathExpressionEngine());
                xmlConfig.setReloadingStrategy(new FileChangedReloadingStrategy());
        
                SubnodeConfiguration myconfig = null;
                myconfig = xmlConfig.configurationAt("/config[./project = 'Project']");
        
                //        Boolean boWithSGML = myconfig.getBoolean("withSGML", false);
        
                //        if (boWithSGML) {
                //            MakeMetsModsOld maker = new MakeMetsModsOld(myconfig);
                //            maker.parse();
                //        } else {
        
                if (args.length == 2 && args[1].contentEquals("verbose")) {
                    MakeMetsMods maker = new MakeMetsMods(myconfig);
                    maker.boVerbose = true;
                    maker.parse();
        
                } else if (args.length > 1) {
        
                    MakeVolumeMap maker = new MakeVolumeMap(myconfig);
                    for (int i = 2; i < args.length; i++) {
        
                        maker.lstFilesVol.add(args[i]);
                    }
        
                    maker.parse();
                } else {
                    MakeMetsMods maker = new MakeMetsMods(myconfig);
        
                    if (args.length == 0) {
                        maker.boVerbose = false;
                    }
                    maker.parse();
                }
    }

    private static void outputDocTypes(String strFile, String strDocType) throws IOException {

        String strResult = "";
        String strIdCurrent = "";
        String str1 = "";

        String text = ParsingUtils.readFileToString(new File(strFile));

        BufferedReader reader = new BufferedReader(new StringReader(text));
        String str = "";

        while ((str = reader.readLine()) != null) {

            str = str.trim();

            if (str.length() < 4) {
                continue;
            }

            String tag = str.substring(0, 4);

            //get current id
            if (tag.equals("0000")) {
                int iValue = str.indexOf(":");
                strIdCurrent = str.substring(iValue + 1, str.length()).trim();
            }

            if (tag.equals("1577")) {
                int iValue = str.indexOf(":");
                str1 = str.substring(iValue + 1, str.length()).trim();
                if (str1.contentEquals(strDocType)) {
                    strResult += strIdCurrent + System.lineSeparator();
                }
            }
        }

        File fileOut = new File("/home/joel/git/rechtsgeschichte/privrecht/test/out.txt");
        try (PrintWriter out = new PrintWriter(fileOut)) {
            out.println(strResult);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void collectMultipleInheritance(String strFileIn, String strFileout) throws IOException {

        String strResult = "";
        String strIdCurrent = "";

        ArrayList<String> lstParentIds = new ArrayList<String>();
        ArrayList<String> lstChildIds = new ArrayList<String>();

        String text = ParsingUtils.readFileToString(new File(strFileIn));

        BufferedReader reader = new BufferedReader(new StringReader(text));
        String str = "";

        String strParent1 = null;
        String strParent2 = null;

        //First collect all the multiple parents:
        while ((str = reader.readLine()) != null) {

            str = str.trim();
            if (str.length() < 4) {
                continue;
            }

            String tag = str.substring(0, 4);

            //get current id
            if (tag.equals("0000")) {
                int iValue = str.indexOf(":");
                strIdCurrent = str.substring(iValue + 1, str.length()).trim();

                strParent1 = null;
                strParent2 = null;
            }

            if (str.length() < 8) {
                continue;
            }
            String strLongTag = str.substring(0, 8);

            if (strLongTag.equals("0004.001")) {
                int iValue = str.indexOf(":");
                strParent1 = str.substring(iValue + 1, str.length()).trim();
            }
            if (strLongTag.equals("0004.002")) {
                int iValue = str.indexOf(":");
                strParent2 = str.substring(iValue + 1, str.length()).trim();

                if (!lstParentIds.contains(strParent1)) {
                    lstParentIds.add(strParent1);
                }
                if (!lstParentIds.contains(strParent2)) {
                    lstParentIds.add(strParent2);
                }
            }
        }

        reader = new BufferedReader(new StringReader(text));
        Boolean boAdded = false;
        //Now collect all the children of multiple parents:
        while ((str = reader.readLine()) != null) {

            str = str.trim();
            if (str.length() < 4) {
                continue;
            }

            String tag = str.substring(0, 4);

            //get current id
            if (tag.equals("0000")) {
                int iValue = str.indexOf(":");
                strIdCurrent = str.substring(iValue + 1, str.length()).trim();
                boAdded = false;
            }

            if (str.length() < 8) {
                continue;
            }
            String strLongTag = str.substring(0, 8);

            if (strLongTag.equals("0004.001") || strLongTag.equals("0004.002")) {
                int iValue = str.indexOf(":");
                strParent1 = str.substring(iValue + 1, str.length()).trim();

                if (lstParentIds.contains(strParent1) && !lstChildIds.contains(strIdCurrent)) {
                    lstChildIds.add(strIdCurrent);
                }
            }
            if (strLongTag.equals("0004.002")) {
                int iValue = str.indexOf(":");
                strParent2 = str.substring(iValue + 1, str.length()).trim();

                if (!boAdded && lstParentIds.contains(strParent2) && !lstChildIds.contains(strIdCurrent)) {
                    lstChildIds.add(strIdCurrent);
                }
            }
        }

        //now Write: first children
        Collections.sort(lstChildIds);
        Collections.sort(lstParentIds);

        for (int i = 0; i < lstChildIds.size(); i++) {
            strResult = addDataset(lstChildIds.get(i), strResult, text);
        }
        //then parents 
        for (int i = 0; i < lstParentIds.size(); i++) {
            strResult = addDataset(lstParentIds.get(i), strResult, text);
        }

        File fileOut = new File(strFileout);
        try (PrintWriter out = new PrintWriter(fileOut)) {
            out.println(strResult);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static String addDataset(String strId, String strResult, String text) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(text));
        String strIdCurrent;
        String str;
        Boolean boAdd = false;
        while ((str = reader.readLine()) != null) {

            str = str.trim();
            if (str.length() < 4) {
                if (boAdd) {
                    strResult = strResult.concat(str);
                    strResult = strResult.concat(System.lineSeparator());
                    boAdd = false;
                    break;
                }
                continue;
            }

            String tag = str.substring(0, 4);

            //get current id
            if (tag.equals("0000")) {
                int iValue = str.indexOf(":");
                strIdCurrent = str.substring(iValue + 1, str.length()).trim();
                if (strIdCurrent.equals(strId)) {
                    boAdd = true;
                    strResult = strResult.concat(str);
                    strResult = strResult.concat(System.lineSeparator());
                }
                continue;
            }

            if (boAdd) {
                strResult = strResult.concat(str);
                strResult = strResult.concat(System.lineSeparator());
            }
        }
        return strResult;
    }

}
