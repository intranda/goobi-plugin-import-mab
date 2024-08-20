---
title: Documentation for Importing MAB2 Files
identifier: intranda_import_mab
description: Import plugin for translating MAB2 and SGML data into METS-MODS
published: false
---

## Introduction
The program examines the provided MAB2 file and translates the fields into metadata for a METS-MODS file. If available, an SGML file is also examined to specify the structural data.

## Installation
To use the plugin, the following files must be installed:

```bash
goobi-plugin-import-mab.jar
goobi-plugin-import-mab.xml
tags.txt 
```

The goobi-plugin-import-mab.jar file contains the program logic and is an executable file.

The goobi-plugin-import-mab.xml file is the configuration file.

Overview and Functionality
The mappings mapMVW and mapChildren are generated. To do this, the JAR file is started with the path to the configuration file as the first parameter, and the path(s) to the MAB files to be processed as additional parameters. This generates and saves the mapping files. This only needs to be done once unless new MAB files are added.

* The program is opened as a JAR with the path to the config file as the only parameter.
The paths to the mab2 file, etc., are read from the configuration file, and the MAB2 file is processed.
* For each dataset in the file, a MetsMods document is generated with appropriate metadata. The translation of individual fields occurs using the tags file.
* If "withSGML" is set to true, the program searches the "sgmlPath" folder for SGML files named after the CatalogID. The MetsMods document then receives the structure from these files.
* For each page in the document, the program searches for images in the "imagePathFile" folder in subfolders named after the CatalogID. These are then copied to the image folder, and references are created in the StructMap.
* NOTE: Currently, the images are NOT copied with the correct permissions. This means that before importing into Goobi, all generated folders and files must be assigned to the tomcat8 user using sudo chown -R tomcat8 *.

* Afterward, the processes can be imported using the Goobi Folder Import.

## Configuration
The configuration of the plugin is done in the goobi-plugin-import-mab.xml file as shown below:

{{CONFIG_CONTENT}}

The following table contains a summary of the parameters and their descriptions:

| Parameter               | Description |
|-------------------------|------------------------|
| `project`               | Should currently only have the entry "Project"; later, it can be made project-specific.                                                                            |
| `rulesetPath`           | Provides the path to the ruleset for the MetsMods files.                                                                                                           |
| `imagePathFile`         | Specifies the path to the image files, which are located in a subfolder named after the CatalogID.                                                                 |
| `outputPath`            | Specifies where the finished MetsMods folders are copied, with subfolders named after the CatalogID.                                                               |
| `mabFile`               | Specifies the MAB2 file to be read.                                                                                                                                |
| `tags`                  | Specifies the translation file that translates MAB2 codes into MetsMods metadata.                                                                                  |
| `withSGML`              | If set to `true`, the program searches the `"sgmlPath"` folder for SGML files named after the CatalogID. These files are used to give structure to the MetsMods document. |
| `defaultPublicationType`| Specifies the type of the document in MetsMods if it has no children or parents. A document with children is imported as a MultiVolumeWork, and the children are imported as Volumes. |
| `singleDigCollection`   | Specifies the singleDigCollection metadata for the MetsMods files.                                                                                                 |
| `mapMVW`                | Specifies the path to the JSON file where the MultiVolumeWork IDs along with a list of all associated volume IDs are stored.                                        |
| `mapChildren`           | Specifies the path to the JSON file where the MultiVolumeWork IDs along with a list of all associated volume IDs are stored.                                        |
| `importFirst`           | Specifies how many processes should be created. If set to 0, all are created.                                                                                      |
| `listIDs`               | Specifies the path to a text file containing a list of IDs. If the file exists and is not empty, ONLY processes with these IDs will be created.                    |
| `allMono`               | Set this to "true" for the special case where all documents to be imported should be stored as "Monograph" and not as Volume, even if they are children.            |
