#Documentation zur MAB2-Dateien einlesen

## Beschreibung

Das Programm untersucht die hinterlegte MAB2-Datei, und übersetzt die Felder Metadaten für ein in METS-MODS Datei. Falls vorhanden, wird auch ein SGML-Datei untersucht, um die Strukturdaten zu spezifizieren.


## Installation und Konfiguration

Das Programm besteht aus drei Dateien:

```bash
plugin_intranda_opac_mab.jar
plugin_intranda_opac_mab.xml
tags.txt
```

Die Datei `"plugin_intranda_opac_mab.jar"` enthält die Programmlogik und ist ein ausführbares Datei.

Die Datei ```plugin_intranda_opac_mab.xml``` ist das Config-Datei.


Die Datei dient zur Konfiguration des Plugins und muss wie folgt aufgebaut sein:

```xml
<config_plugin>
<config>

     <!-- which projects to use for (can be more than one, otherwise use *) -->
    <project>Project</project>
    
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

     <!-- Mapping for MultiVolumeWork to child Volumes: -->
    <mapMVW>/path/to/map.txt</mapMVW>
    
     <!-- Mapping for child Volumes to parent MultiVolumeWork: -->
    <mapChildren>/path/to/mapRev.txt</mapChildren>

    <!-- default publication type. If missing or empty, no record will be created -->
    <defaultPublicationType>Monograph</defaultPublicationType>

    <!-- Collection name -->
    <singleDigCollection>Privatrecht</singleDigCollection>

</config>
</config_plugin>
```

Eine Kopie liegt in dieser Repro, im Ordner "resources".

Das Element `"project"`
soll im Moment nur den Eintrag "Project" haben; später können wir es Projektspezyfisch mahchen.

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

Wenn `"withSGML"` `true` ist, dann wird in der Ornder `"sgmlPath"` nach SGMl-Datein gesucht, mit CatalogID als Name. Dieser werden benutzt, um das MM Struktur zu geben.

Das Element `"defaultPublicationType"`
spezifiziert die MM Type der Dokument

Das Element `"singleDigCollection"`
spezifiziert die Metadatum singleDigCollection für die MM Dateien.




## Arbeitsweise

Die Arbeitsweise sieht folgendermaßen aus:

* Das Programm wird als JAR geöffnet, mit Pfad zur config-Datei als einziger Parameter.
* Aus der config-Datei werden die Pfade zur mab2-Datei usw. ausgelesen, und der mad2-Datei wird durchlesen.
* Für jeder Dataset in der Datei wird ein MetsMods Document erzeugt, mit possenden Metadaten. Die Übersetzung der einzelnen Felder passiert mittels der tags Datei.
* Wenn `"withSGML"` `true` ist, dann wird in der Ornder `"sgmlPath"` nach SGMl-Datein gesucht, mit CatalogID als Name. Die MM Document ebkommt devon dann Struktur.
* Für jedes Page in der Document wird nach Images gesucht, in der `"imagePathFile"` Ornder, in Unterordner mit CatalogID als Name. Dieser werden dann nach die Image Ordner kopiert, und Referenzen in der Structmap gemacht.
* Danach kann mit der Goobi Folder Import die Prozesse importiert werden. 
