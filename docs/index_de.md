---
title: Dokumentation zum einlesen von MAB2-Dateien
identifier: intranda_import_mab
description: Import Plugin für die Übersetzung von MAB2- und SGML-Daten in METS-MODS
published: false
---

## Einführung
Das Programm untersucht die hinterlegte MAB2-Datei, und übersetzt die Felder in Metadaten für ein in METS-MODS Datei. Falls vorhanden, wird auch eine SGML-Datei untersucht, um die Strukturdaten zu spezifizieren.

## Installation
Um das Plugin nutzen zu können, müssen folgende Dateien installiert werden:

```bash
goobi-plugin-import-mab.jar
goobi-plugin-import-mab.xml
tags.txt
```
Die Datei `goobi-plugin-import-mab.jar` enthält die Programmlogik und ist eine ausführbares Datei.

Die Datei `goobi-plugin-import-mab.xml` ist die Konfigurationsdatei.

## Überblick und Funktionsweise
Die Mappings mapMVW und mapChildren werden erzeugt. Dafür wird der JAR gestartet, wobei der Pfad zur Konfigurationsdatei als erster Parameter, und Pfad(e) zur MAB-Files die bearbeitet werden sollen als weitere Parameter. Damit werden die mapping files erzeugt und gespeichert. Das muss nur einmal geschehen, es sei denn, es kommen neue MAB files dazu.


* Das Programm wird als JAR geöffnet, mit Pfad zur config-Datei als einziger Parameter.
* Aus der Konfigurationsdatei werden die Pfade zur mab2-Datei usw. ausgelesen, und die MAB2-Datei wird durchgelesen..
* Für jedes Dataset in der Datei wird ein MetsMods Document erzeugt, mit passenden Metadaten. Die Übersetzung der einzelnen Felder passiert mittels der tags Datei.
* Wenn `"withSGML"` `true` ist, dann wird in der Ordner `"sgmlPath"` nach SGML-Datein gesucht, mit CatalogID als Name. Die MM Document bekommt davon dann die Struktur.
* Für jedes Page in der Document wird nach Images gesucht, in der `"imagePathFile"` Ordner, in Unterordner mit CatalogID als Name. Diese werden dann in den Image Ordner kopiert, und Referenzen in der Structmap gemacht.
* BEMERKUNG: Aktuell werden die Bilder NICHT mit der korrekten Permissions kopiert. Das bedeutet, vor dem Import in Goobi müssen alle erzeugten Ordner und Dateien dem Besitzer tomcat8 mittels `sudo chown -R tomcat8 *` gegeben werden!
* Danach kann mit dem Goobi Folder Import die Prozesse importiert werden. 

## Konfiguration
Die Konfiguration des Plugins erfolgt in der Datei `goobi-plugin-import-mab.xml` wie hier aufgezeigt:

{{CONFIG_CONTENT}}

Die folgende Tabelle enthält eine Zusammenstellung der Parameter und ihrer Beschreibungen:

Parameter               | Erläuterung
------------------------|------------------------------------
`project`               | soll im Moment nur den Eintrag "Project" haben; später können wir es Projektspezifisch machen.|
`rulesetPath`           | liefert den Pfad zur Ruleset für die MetsMods Dateien. |
`imagePathFile`         | ist der Pfad zu den Image Datein, die im Unterordner mit dem Name der CatalogId liegen. |
`outputPath`            | ist wo die fertigen MM Ordner kopiert werden, wobei die Unterordner nach der CatalogId benannt sind. |
`mabFile`               | spezifiziert der MAB2-Datei, die gelesen wird |
`tags`                  | spezifiziert der Übersetzungs.Datei, die MAB2-Codes in MM-Metadaten übersetzt.|
`withSGML`              | Wenn dieser Wert `true` ist, dann wird in dem Ordner `"sgmlPath"` nach SGML-Dateiengesucht, welche die  CatalogID als Namen haben. Diese werden benutzt, um die MM Struktur zu geben. |
`defaultPublicationType`| Dieses Element spezifiziert die MM Type der Dokumente, falls diese keine Kinder oder Eltern haben. Ein Dokument mit Kindern wird als MultiVolumeWork importiert und die Kinder werden als Volumes importiert. |
`singleDigCollection`   | Dieses Element spezifiziert die Metadata singleDigCollection für die MM Dateien. |
`mapMVW` | Das Element `"mapMVW"` spezifiziert den Pfad zur JSON Datei, in dem die MultiVolumeWork IDs zusammen mit einer Liste der IDs von allen Volumes die dazu gehören gespeichert sind. |
`mapChildren`           | Dieses Element spezifiziert den Pfad zur JSON Datei, in welcher die MultiVolumeWork IDs zusammen mit einer Liste der IDs von allen Volumes welche dazu gehören gespeichert sind.
`importFirst`           | Das Element `importFirst` spezifiziert wie viele Vorgänge angelegt werden sollen. Wenn das 0 ist, werden alle gemacht. 
`listIDs`               | Dieses Element spezifiziert den Pfad zu einer Textdatei, in dem eine Liste von IDs liegt. Wenn die Datei existiert, und nicht leer ist, werden NUR Vorgänge, die diese IDs haben erzeugt. Das wird benutzt, um hinterher geänderte oder verbesserte Vorgänge neu zu importieren.
`allMono`               | Dieses Element ist für den Sonderfall auf "true" zu setzen, wenn alle zu importierenden Dokumente als "Monograph" gespeichert werden sollen, _nicht_ als Volume, auch wenn sie Kinder sind.