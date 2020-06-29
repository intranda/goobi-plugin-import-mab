#Documentation zur MAB2-Dateien einlesen

## Beschreibung

Das Programm untersucht die hinterlegte MAB2-Datei, und übersetzt die Felder Metadaten für ein in METS-MODS Datei. Falls vorhanden, wird auch ein SGML-Datei untersucht, um die Strukturdaten zu spezifizieren.


## Installation und Konfiguration

Das Programm besteht aus drie Dateien:

```bash
plugin_intranda_opac_mab.jar
plugin_intranda_opac_mab.xml
tags.txt
```

Die Datei `"plugin_intranda_opac_mab.jar"` enthält die Programmlogik und ist ein ausführbares Datei.

Die Datei ```plugin_intranda_opac_mab.xml``` ist das Config-Datei.


Die Datei dient zur Konfiguration des Plugins und muss wie folgt aufgebaut sein:

```xml
<config>

    <!-- Ruleset for the MM files: -->
    <rulesetPath>/opt/digiverso/goobi/rulesets/ruleset.xml</rulesetPath>

    <!-- Path to images: -->
    <imagePathFile>/path/to/images/</imagePathFile>
    
    <!-- Folder where the files are to be copied -->
    <outputPath>/path/to/import/folder/</outputPath>

    <!-- Mab file to read: -->
    <mabFile>/path/to/mab-file.txt</mabFile>
    
    <!-- Translation file between mab2-tags and MM metadata: -->
    <tags>/path/to/tags.txt</tags>
    
    <!-- Use SGML files? -->
    <withSGML>true</withSGML>

    <!-- Path to SGML files, if withSGML: -->
    <sgmlPath>/path/to/SGML-Folder/</sgmlPath>


    <!-- default publication type. If missing or empty, no record will be created -->
    <defaultPublicationType>Monograph</defaultPublicationType>

    <!-- Collection name -->
    <singleDigCollection>Privatrecht</singleDigCollection>

</config>
```

Eine Kopie liegt in dieser Repro, im Ordner "resources".


Das Element `"rulesetPath"`
liefert der Pfad zur Ruleset für die MetsMods Dateien.

Das Element `"imagePathFile"`
ist der Pfad zur Image Datein, die darunter liegen in Unterordner mit Name der CatalogId. 

Das Element `"outputPath"`
ist wo die fertigen MM Ordner kopiert werden, in Unterordner benannt nach der CatalogId.

Das Element `"mabFile"`
spezifiziert der mab2-Datei, die gelesen wird

Das Element `"tags"`
spezifiziert der Übersetzungs.Datei, welches mab2-Codes in MM Metadaten übersetzt.

Wenn `"withSGML"` `true` ist, dann wird in der Ornder `"sgmlPath"` nach SGMl-Datein, mit CatalogID als Name. Dieser werden benutzt, um das MM Struktur zu geben.

Das Element `"defaultPublicationType"`
spezifiziert die MM Type der Dokument

Das Element `"singleDigCollection"`
spezifiziert die Metadatum singleDigCollection für die MM Dateien.




## Arbeitsweise

Die Arbeitsweise sieht folgendermaßen aus:

* Das Programm wird als JAR geöffnet, mit Pfad zur config-Datei als einziger Parameter.
* Aus der config-Datei werden die Pfade zur mab2-Datei usw. ausgelesen, und der mad2-Datei wird durchlesen.
* 
