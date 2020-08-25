#Documentation zur MAB2-Dateien einlesen

## Beschreibung

Das Programm untersucht die hinterlegte MAB2-Datei, und übersetzt die Felder Metadaten für ein in METS-MODS Datei. Falls vorhanden, wird auch ein SGML-Datei untersucht, um die Strukturdaten zu spezifizieren.


## Installation und Konfiguration

Das Programm besteht aus drei Dateien:

```bash
goobi-plugin-import-mab.jar
goobi-plugin-import-mab.xml
tags.txt
```

Die Datei `"goobi-plugin-import-mab.jar"` enthält die Programmlogik und ist ein ausführbares Datei.

Die Datei ```goobi-plugin-import-mab.xml``` ist das Config-Datei.


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

Das Element `"mapMVW"`
spezifiziert der Pfad zur JSON datei, in dem die MultiVolumeWork IDs gespeichert sind, zusammen mit jeweils eine Liste der IDs von alle Volumes die dazu gehören.

Das Element `"mapChildren"`
spezifiziert der Pfad zur JSON datei, das der gleiche Mapping anders rum speichert. Also zu jeder Volume ID, der an einer MVW gehört, wird der ID des Parents geordnet. 

Zum testen: das Element `"importFirst"`
spezifiziert wie viele Vorgänge angelegt werden sollen. Wenn das 0 ist, werden alle gemacht. 

Das Element `"listIDs"`
spezifiziert der Pfad zu einer Textdatei, in dem eine Liste IDs liegt. Wenn der Datei existiert, und nicht leer ist, werden NUR Vorgänge, die diesen IDs haben erzeugt. Das wird benutzt, um hinterher geänderte oder verbesserte Vorgänge neu zu importieren.

Die restliche Einträge sind für die späteren Import benutzt.

## Arbeitsweise

Die Arbeitsweise sieht folgendermaßen aus:

### Vorbereitung:

Die Mappings mapMVW und mapChildren werden erzeugt. Dafür wird der JAR gestartet, mit Pfad zur config-Datei als erster Parameter, und Pfad(e) zur MAB-Files die bearbeitet werden sollen als weitere Parameter. Damit werden die mapping files erzeugt und gespeichert. Das muss nur einmal geschehen, ausser es kommen neue MAB files dazu.

### Import

* Das Programm wird als JAR geöffnet, mit Pfad zur config-Datei als einziger Parameter.
* Aus der config-Datei werden die Pfade zur mab2-Datei usw. ausgelesen, und der mab2-Datei wird durchlesen.
* Für jeder Dataset in der Datei wird ein MetsMods Document erzeugt, mit possenden Metadaten. Die Übersetzung der einzelnen Felder passiert mittels der tags Datei.
* Wenn `"withSGML"` `true` ist, dann wird in der Ornder `"sgmlPath"` nach SGMl-Datein gesucht, mit CatalogID als Name. Die MM Document ebkommt devon dann Struktur.
* Für jedes Page in der Document wird nach Images gesucht, in der `"imagePathFile"` Ornder, in Unterordner mit CatalogID als Name. Dieser werden dann nach die Image Ordner kopiert, und Referenzen in der Structmap gemacht.
* BEMERKUNG: Aktuell werden die Bilder NICHT mit der korrekten Permissions kopiert. Das bedeutet, vor dem Import in Goobi mussen alle erzeugten Ordner und Dateien den Besitzer tomcat8 gegeben werden, mittels `sudo chown -R tomcat8 *` !
* Danach kann mit der Goobi Folder Import die Prozesse importiert werden. 

###Note

Im Aktuellen Fall ist der MAB Datei zu gross für die Speicher am Kundenrechner. In diesem Fall habe ich die Datei in 2 geteilt, wobei im zweiten Teil nur Werke stehen, die nicht in die Mapping Dateien auftauchen, damit der Erzeugung von MVW und Volumes funktioniert. Dann kann das Programm 2 Mal aufgerufen werden, einmal für jeder MAB Datei. MVWs und Volumes werden nur korrekt zugeordnet, wenn beide Datensätze im gleichen MAB Datei liegen!

