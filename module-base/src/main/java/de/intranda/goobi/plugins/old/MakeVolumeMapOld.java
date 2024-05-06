//package de.intranda.goobi.plugins;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.StringReader;
//import java.util.ArrayList;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.OutputKeys;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerConfigurationException;
//import javax.xml.transform.TransformerException;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.TransformerFactoryConfigurationError;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;
//
//import org.apache.commons.configuration.ConfigurationException;
//import org.jdom2.JDOMException;
//import org.w3c.dom.Attr;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;
//
//import ugh.exceptions.UGHException;
//
//public class MakeVolumeMap {
//
//    //    private ArrayList<String> lstFilesMulti;
//    private ArrayList<String> lstFilesVol;
//    private ArrayList<String> lstIdsSaved;
//
//    private String strMap = "/home/joel/git/rechtsgeschichte/map.xml";
//
//    private Document document;
//
//    private Element root;
//    private NodeList docNodes;
//    private int docNodeLength;
//
//    public static void main(String[] args) throws ConfigurationException, ParserConfigurationException, SAXException, IOException, UGHException,
//            JDOMException, TransformerException {
//
//        MakeVolumeMap maker = new MakeVolumeMap();
//        //        maker.lstFilesMulti = new ArrayList<String>();
//        //        maker.lstFilesMulti.add("/home/joel/git/rechtsgeschichte/final_data/mw_nicht_uw");
//        //        maker.lstFilesMulti.add("/home/joel/git/rechtsgeschichte/final_data/stueck_nicht_uw");
//        //        maker.lstFilesMulti.add("/home/joel/git/rechtsgeschichte/final_data/bd_nicht_uw");
//
//        maker.lstFilesVol.add("/home/joel/git/rechtsgeschichte/final_data/mw_nicht_uw");
//        maker.lstFilesVol.add("/home/joel/git/rechtsgeschichte/final_data/stueck_nicht_uw");
//        maker.lstFilesVol.add("/home/joel/git/rechtsgeschichte/final_data/bd_nicht_uw");
//        maker.lstFilesVol.add("/home/joel/git/rechtsgeschichte/final_data/mono");
//        maker.lstFilesVol.add("/home/joel/git/rechtsgeschichte/final_data/uw_bd");
//        maker.lstFilesVol.add("/home/joel/git/rechtsgeschichte/final_data/uw_rest");
//        maker.lstFilesVol.add("/home/joel/git/rechtsgeschichte/final_data/uw_stueck");
//        maker.lstFilesVol.add("/home/joel/git/rechtsgeschichte/final_data/uw_mw");
//
//        maker.parse();
//
//    }
//
//    public MakeVolumeMap() {
//
//        lstIdsSaved = new ArrayList<String>();
//        lstFilesVol = new ArrayList<String>();
//    }
//
//    public void parse() throws IOException, JDOMException, ParserConfigurationException, TransformerException {
//
//        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
//
//        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
//
//        this.document = documentBuilder.newDocument();
//
//        // root element
//        this.root = document.createElement("map");
//        document.appendChild(root);
//
//        //        //add a new top level doc:
//        //        Element doc = addTopLevelDoc("10", "sig10");
//        //
//        //        //add a child:
//        //        addChild(doc, "100");
//        //       addChild(doc, "200");
//
//        //get parents
//        fillMap(root, false);
//        saveXML();
//
//        //get children
//        fillMap(root, false);
//        saveXML();
//
//        //get grandchildren
//        fillMap(root, false);
//        saveXML();
//
//        System.out.println("Done creating XML File");
//    }
//
//    private void saveXML()
//            throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException, FileNotFoundException {
//        //transform the DOM Object to an XML File
//        Transformer tr = TransformerFactory.newInstance().newTransformer();
//        tr.setOutputProperty(OutputKeys.INDENT, "yes");
//        tr.setOutputProperty(OutputKeys.METHOD, "xml");
//        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//        tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
//        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
//        DOMSource domSource = new DOMSource(document);
//
//        // send DOM to file
//        tr.transform(domSource, new StreamResult(new FileOutputStream(strMap)));
//    }
//
//    private void fillMap(Element root, Boolean boTopLevel) throws IOException {
//
//        String strIdCurrent = "";
//        Boolean boSaved = false;
//        Boolean boTop = true;
//
//        for (String strFile : lstFilesVol) {
//
//            System.out.println("Parsing " + strFile);
//            String text = ParsingUtils.readFileToString(new File(strFile));
//
//            if ((text != null) && (text.length() != 0)) {
//
//                BufferedReader reader = new BufferedReader(new StringReader(text));
//                String str = "";
//
//                while ((str = reader.readLine()) != null) {
//
//                    str = str.trim();
//
//                    if (str.length() < 4) {
//                        continue;
//                    }
//
//                    String tag = str.substring(0, 4);
//
//                    try {
//                        //get current id
//                        if (tag.equals("0000")) {
//                            boSaved = false;
//                            boTop = true;
//                            int iValue = str.indexOf(":");
//                            strIdCurrent = str.substring(iValue + 1, str.length()).trim();
//
//                            if (lstIdsSaved.contains(strIdCurrent)) {
//                                boSaved = true;
//                            }
//                        }
//
//                        if (!boSaved && tag.equals("0001")) {
//                            boTop = false;
//                            int iValue = str.indexOf(":");
//                            String idVerweisSSW = str.substring(iValue + 1, str.length()).trim();
//
//                            Element eltParent = findParent(idVerweisSSW);
//                            if (eltParent != null) {
//                                addChild(eltParent, strIdCurrent);
//                                lstIdsSaved.add(strIdCurrent);
//                                boSaved = true;
//                            }
//                        }
//
//                        if (!boSaved && tag.equals("0004")) {
//                            boTop = false;
//                            int iValue = str.indexOf(":");
//                            String idVerweisSSW = str.substring(iValue + 1, str.length()).trim();
//
//                            Element eltParent = findParent(idVerweisSSW);
//                            if (eltParent != null) {
//                                addChild(eltParent, strIdCurrent);
//                                lstIdsSaved.add(strIdCurrent);
//                                boSaved = true;
//                            }
//                        }
//
//                        if (!boSaved && boTop && tag.equals("9999")) {
//                            addTopLevelDoc(strIdCurrent);
//                            lstIdsSaved.add(strIdCurrent);
//                            boSaved = true;
//                            this.docNodes = root.getChildNodes();
//                            this.docNodeLength = docNodes.getLength();
//                        }
//
//                        //                        if (tag.equals("0014")) {
//                        //                            // Data field
//                        //                            int iValue = str.indexOf(":");
//                        //                            String content = str.substring(iValue + 1, str.length()).trim();
//                        //
//                        //                            if (!boTopLevel) {
//                        //
//                        //                                int iBracket = content.indexOf("[");
//                        //                                if (iBracket != -1 || content.startsWith("in") || content.startsWith("an")) {
//                        //
//                        //                                    //find the corresp. top level doc:
//                        //                                    Element eltParent = findParent(strIdCurrent);
//                        //                                    if (eltParent != null) {
//                        //                                        addChild(eltParent, strIdCurrent);
//                        //                                    }
//                        //                                }
//                        //                            } else if (boTopLevel && (!content.startsWith("in") && !content.startsWith("an"))) {
//                        //
//                        //                                String strSig = content;
//                        //                                int iBracket = strSig.indexOf("[");
//                        //                                if (iBracket != -1) {
//                        //                                    continue;
//                        //                                }
//                        //                                
//                        //
//                        //                                int iValue2 = strSig.indexOf(":");
//                        //                                if (iValue2 != -1) {
//                        //                                    strSig = strSig.substring(iValue + 1, strSig.length()).trim();
//                        //                                }
//                        //
//                        //                                addTopLevelDoc(strIdCurrent, strSig);
//                        //
//                        //                            }
//                        //
//                        //                        }
//                    } catch (Exception e) {
//                        // TODO: handle exception
//                        System.out.println(e.getMessage());
//                    }
//                }
//
//            }
//        }
//    }
//
//    private Element findParent(String strId) {
//
//        //        int iValue = content.lastIndexOf(":");
//        //        String ref = content.substring(iValue + 1, content.length()).trim();
//
//        if (strId.startsWith("000")) {
//            strId = strId.replaceFirst("000", "");
//        }
//
//        if (!lstIdsSaved.contains(strId) || docNodes == null) {
//            return null;
//        }
//
//        for (int i = 0; i < docNodeLength; i++) {
//            Node node = docNodes.item(i);
//            String strContent = node.getTextContent();
//            if (strId.equals(strContent)) {
//                return (Element) node;
//            }
//
//            if (node.hasChildNodes()) {
//                NodeList childNodes = node.getChildNodes();
//                for (int j = 0; j < childNodes.getLength(); j++) {
//                    Node nodeChild = childNodes.item(j);
//                    if (nodeChild.getNodeType() != Node.ELEMENT_NODE) {
//                        continue;
//                    }
//                    String strContent1 = nodeChild.getTextContent();
//                    if (strId.equals(strContent1)) {
//                        return (Element) nodeChild;
//                    }
//                }
//            }
//        }
//
//        //otherwise
//        return null;
//    }
//
//    private Element addTopLevelDoc(String strId) {
//        Element doc = document.createElement("doc");
//        doc.appendChild(document.createTextNode(strId));
//        root.appendChild(doc);
//
//        //        Attr attrId = document.createAttribute("id");
//        //        attrId.setValue(strId);
//        //        doc.setAttributeNode(attrId);
//
//        return doc;
//    }
//
//    private void addChild(Element doc, String strId) {
//        Element child = document.createElement("child");
//        child.appendChild(document.createTextNode(strId));
//        doc.appendChild(child);
//
//        //        Attr attrIdChild = document.createAttribute("id");
//        //        attrIdChild.setValue(strId);
//        //        child.setAttributeNode(attrIdChild);
//        //
//        //        doc.appendChild(child);
//    }
//
//}
