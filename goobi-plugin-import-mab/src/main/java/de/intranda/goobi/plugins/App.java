package de.intranda.goobi.plugins;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

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

    public static void main(String[] args)
            throws ConfigurationException, ParserConfigurationException, SAXException, IOException, UGHException, JDOMException {

        //        String strConfig = "/home/joel/git/rechtsgeschichte/testdiss/diss-config.xml";
        //        String strConfig = "/home/joel/git/rechtsgeschichte/testprivr/privrecht-config.xml";
        //        String strConfig = "/home/joel/git/rechtsgeschichte/data/config.xml";
        String strConfig = "/home/joel/git/rechtsgeschichte/testdiss4/diss-config4.xml";
//        String strConfig = "";
        if (args.length > 0) {
            strConfig = args[0];
        }
        else {
            
        }
        
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
            MakeMetsMods maker = new MakeMetsMods(myconfig);
            maker.parse();
//        }
    }
}
