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
﻿<config_plugin>
<config>
        <!-- which projects to use for (can be more than one, otherwise use *) -->
        <project>Project</project>
        
    <!-- Ruleset for the MM files: -->
    <rulesetPath>/opt/digiverso/goobi/rulesets/ruleset-mpi.xml</rulesetPath>

    <!-- Path to images: -->
    <imagePathFile>/opt/digiverso/import/dissertationen/</imagePathFile>
    
    <!-- Folder where the files are to be copied -->
    <outputPath>/opt/digiverso/import/diss-mm/</outputPath>

    <!-- Mab file to read: -->
    <mabFile>/opt/digiverso/import/dissertationen/data/diss.txt</mabFile>
    
    <!-- Ruleset for the MM files: -->
    <tags>/opt/digiverso/import/dissertationen/data/tags-full.txt</tags>
    
    <!-- Use SGML files? -->
    <withSGML>false</withSGML>

    <!-- Path to SGML files, if withSGML: -->
    <sgmlPath></sgmlPath>

    <!-- default publication type if it cannot be detected. If missing or empty, no record will be created -->
    <defaultPublicationType>Monograph</defaultPublicationType>

    <!-- Collection name -->
    <singleDigCollection>Dissertationen</singleDigCollection>   

        <!-- Mapping for MultiVolumeWork to child Volumes: -->
        <mapMVW>/opt/digiverso/import/dissertationen/data/map.txt</mapMVW>
    
        <!-- Mapping for child Volumes to parent MultiVolumeWork: -->
        <mapChildren>/opt/digiverso/import/dissertationen/data/reverseMap.txt</mapChildren>

    <!-- For testing: stop the import after this many folders have been created. If 0, then import all.-->
    <importFirst>10</importFirst>

        <!-- List of IDs to import. If empty, import all files -->
        <listIDs>/opt/digiverso/import/dissertationen/data/missing-image-ids.txt</listIDs>

    <!-- For the import -->
    <basedir>/opt/digiverso/import/diss-mm/</basedir>
    <prefixInDestination>master_</prefixInDestination>
    <suffixInDestination>_media</suffixInDestination>

    <title>
        <doctype doctypename="Monograph" processtitle="CatalogIDDigital" />
        <doctype doctypename="Volume" processtitle="CatalogIDDigital" />
        <doctype doctypename="MultiVolumeWork" processtitle="CatalogIDDigital" />
    </title>

    <moveFiles>true</moveFiles>

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
spezifiziert die MM Type der Dokument, falls es keine Kinder oder Eltern hat. Ein Dokument mit Kindern wird als MultiVolumeWork importiert, die Kinder werden als Volumes importiert.

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
