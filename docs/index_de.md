---
title: MAB-Dateien einlesen
identifier: intranda_import_mab
description: Import Plugin für die Übersetzung von MAB2- und SGML-Daten in METS-MODS
published: true
---

## Einführung
Das Programm untersucht die hinterlegte MAB2-Datei und übersetzt die Felder in Metadaten für eine METS-Datei. Falls vorhanden, wird auch eine SGML-Datei untersucht, um die Strukturdaten zu spezifizieren.

## Installation
Um das Plugin nutzen zu können, müssen folgende Dateien installiert werden:

```bash
/opt/digiverso/goobi/plugin/import/goobi-plugin-import-mab.jar
/opt/digiverso/goobi/config/goobi-plugin-import-mab.xml
/opt/digiverso/goobi/config/tags.txt
```
Die Datei `goobi-plugin-import-mab.jar` enthält die Programmlogik und ist eine ausführbares Datei.

Die Datei `goobi-plugin-import-mab.xml` ist die Konfigurationsdatei.

## Überblick und Funktionsweise
Die Mappings mapMVW und mapChildren werden erzeugt. Dafür wird die jar-Datei gestartet, wobei der Pfad zur Konfigurationsdatei als erster Parameter und als weitere Parameter der/die Pfad(e) zu den MAB-Dateien übergeben werden, die bearbeitet werden sollen. Damit werden die Mapping-Dateien erzeugt und gespeichert. 

* Das Programm wird als JAR geöffnet, wobei der Pfad zur Konfigurationsdatei als einziger Parameter übergeben wird.
* Aus der Konfigurationsdatei werden die Pfade zur MAB2-Datei usw. ausgelesen, und die MAB2-Datei wird durchgelesen..
* Für jedes Dataset in der Datei wird ein METS-MODS-Dokument mit den passenden Metadaten erzeugt. Die Übersetzung der einzelnen Felder erfolgt mittels der Tags-Datei.
* Wenn `withSGML` true ist, dann wird im Ordner `sgmlPath` nach SGML-Dateien gesucht, die die CatalogID als Namen haben. Das METS-MODS-Dokument erhält dann daraus die Struktur.
* Für jede Seite im Dokument werden die passenden Bilder im Ordner `imagePathFile` gesucht, in den Unterordnern, die die CatalogID als Namen haben. Diese werden dann in den Image-Ordner kopiert, und Referenzen in der structMap erstellt.
* BEMERKUNG: Aktuell werden die Bilder NICHT mit den korrekten Berechtigungen kopiert. Das bedeutet, dass vor dem Import in  Goobi alle erzeugten Ordner und Dateien dem Benutzer `tomcat8` mittels `sudo chown -R tomcat8 *`  zugewiesen werden müssen!
* Danach können die Vorgägne mit dem Goobi Plugin für den Folder Import importiert werden.

## Konfiguration
Die Konfiguration des Plugins erfolgt in der Datei `goobi-plugin-import-mab.xml` wie hier aufgezeigt:

```xml
<config_plugin>		
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

    <!-- All as monograph -->		
    <allMono>false</allMono>		
    		
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

Die folgende Tabelle enthält eine Zusammenstellung der Parameter und ihrer Beschreibungen:

| Parameter               | Erläuterung |
|-------------------------|------------------------------------|
| `project`               | Dieser Parameter dient zur Festlegung des Projekt, für das diese Konfiguration gelten soll. |
| `rulesetPath`           | Dieser Parameter liefert den Pfad zur Ruleset-Datei für die METS-Dateien. |
| `imagePathFile`         | Hier wird der Pfad zu den Image-Dateien angegeben, die im Unterordner mit dem Namen der CatalogId liegen. |
| `outputPath`            | Dieser Parameter gibt an, wohin die fertigen METS-Ordner kopiert werden. Die Unterordner werden dabei nach der CatalogId benannt. |
| `mabFile`               | Hier wird die MAB2-Datei spezifiziert, die gelesen werden soll. |
| `tags`                  | Dieser Parameter spezifiziert die Übersetzungsdatei, die MAB2-Codes in METS-Metadaten übersetzt. |
| `withSGML`              | Wenn dieser Wert auf `true` gesetzt ist, wird im Ordner `sgmlPath` nach SGML-Dateien gesucht, deren Name der CatalogID entspricht. Diese Dateien werden genutzt, um die METS-Struktur zu erstellen. |
| `defaultPublicationType`| Dieses Element spezifiziert den METS-Typ der Dokumente für den Fall, dass diese keine Kinder oder Eltern haben. Ein Dokument mit Kindern wird als MultiVolumeWork importiert, während die Kinder als Volumes importiert werden. |
| `singleDigCollection`   | Dieser Parameter spezifiziert die Metadaten für die `singleDigCollection` der METS-Dateien. |
| `mapMVW`                | Das Element `mapMVW` gibt den Pfad zu einer JSON-Datei an, in der die MultiVolumeWork-IDs zusammen mit einer Liste der dazugehörigen Volume-IDs gespeichert sind. |
| `mapChildren`           | Dieses Element spezifiziert den Pfad zu einer JSON-Datei, in der die MultiVolumeWork-IDs zusammen mit einer Liste der dazugehörigen Volume-IDs gespeichert sind. |
| `importFirst`           | Dieser Parameter legt fest, wie viele Vorgänge zuerst angelegt werden sollen. Wenn der Wert `0` ist, werden alle Vorgänge erstellt. |
| `listIDs`               | Dieser Parameter gibt den Pfad zu einer Textdatei an, in der eine Liste von IDs enthalten ist. Wenn die Datei existiert und nicht leer ist, werden nur Vorgänge für diese IDs erzeugt. Dies wird genutzt, um nachträglich geänderte oder verbesserte Vorgänge neu zu importieren. |
| `allMono`               | Dieser Parameter ist auf "true" zu setzen, wenn alle zu importierenden Dokumente als `Monograph` gespeichert werden sollen, auch wenn sie Kinder enthalten, anstatt als Volume. |
