---
title: MAB2-Dateien einlesen
identifier: intranda_import_mab
description: Import Plugin für die Übersetzung von MAB2- und SGML-Daten in METS-MODS
published: false
---

## Einführung
Das Programm untersucht die hinterlegte MAB2-Datei und übersetzt die Felder in Metadaten für eine METS-MODS-Datei. Falls vorhanden, wird auch eine SGML-Datei untersucht, um die Strukturdaten zu spezifizieren.

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
Die Mappings mapMVW und mapChildren werden erzeugt. Dafür wird das JAR gestartet, wobei der Pfad zur Konfigurationsdatei als erster Parameter und der/die Pfad(e) zu den MAB-Dateien, die bearbeitet werden sollen, als weitere Parameter übergeben werden. Damit werden die Mapping-Dateien erzeugt und gespeichert. Das muss nur einmal geschehen, es sei denn, es kommen neue MAB-Dateien hinzu.

* Das Programm wird als JAR geöffnet, wobei der Pfad zur Konfigurationsdatei als einziger Parameter übergeben wird.
* Aus der Konfigurationsdatei werden die Pfade zur MAB2-Datei usw. ausgelesen, und die MAB2-Datei wird durchgelesen..
* Für jedes Dataset in der Datei wird ein METS-MODS-Dokument mit den passenden Metadaten erzeugt. Die Übersetzung der einzelnen Felder erfolgt mittels der Tags-Datei.
* Wenn `withSGML`   true ist, dann wird im Ordner `sgmlPath` nach SGML-Dateien gesucht, die die CatalogID als Namen haben. Das METS-MODS-Dokument erhält dann daraus die Struktur.
* Für jede Seite im Dokument werden die passenden Bilder im Ordner `imagePathFile` gesucht, in den Unterordnern, die die CatalogID als Namen haben. Diese werden dann in den Image-Ordner kopiert, und Referenzen in der structMap erstellt.
* BEMERKUNG: Aktuell werden die Bilder NICHT mit den korrekten Berechtigungen kopiert. Das bedeutet, dass vor dem Import in  Goobi alle erzeugten Ordner und Dateien dem Benutzer `tomcat8` mittels `sudo chown -R tomcat8 *`  zugewiesen werden müssen!
* Danach können die Prozesse mit dem Goobi Folder Import importiert werden.

## Konfiguration
Die Konfiguration des Plugins erfolgt in der Datei `goobi-plugin-import-mab.xml` wie hier aufgezeigt:

{{CONFIG_CONTENT}}

Die folgende Tabelle enthält eine Zusammenstellung der Parameter und ihrer Beschreibungen:

| Parameter               | Erläuterung |
|-------------------------|------------------------------------|
| `project`               | Im Moment soll dieses Feld nur den Eintrag "Project" enthalten; später kann es projektspezifisch angepasst werden. |
| `rulesetPath`           | Dieser Parameter liefert den Pfad zur Ruleset-Datei für die MetsMods-Dateien. |
| `imagePathFile`         | Hier wird der Pfad zu den Image-Dateien angegeben, die im Unterordner mit dem Namen der CatalogId liegen. |
| `outputPath`            | Dieser Parameter gibt an, wohin die fertigen MM-Ordner kopiert werden. Die Unterordner werden dabei nach der CatalogId benannt. |
| `mabFile`               | Hier wird die MAB2-Datei spezifiziert, die gelesen werden soll. |
| `tags`                  | Dieser Parameter spezifiziert die Übersetzungsdatei, die MAB2-Codes in MM-Metadaten übersetzt. |
| `withSGML`              | Wenn dieser Wert auf `true` gesetzt ist, wird im Ordner `"sgmlPath"` nach SGML-Dateien gesucht, deren Name der CatalogID entspricht. Diese Dateien werden genutzt, um die MM-Struktur zu erstellen. |
| `defaultPublicationType`| Dieses Element spezifiziert den MM-Typ der Dokumente für den Fall, dass diese keine Kinder oder Eltern haben. Ein Dokument mit Kindern wird als MultiVolumeWork importiert, während die Kinder als Volumes importiert werden. |
| `singleDigCollection`   | Dieser Parameter spezifiziert die Metadaten für die singleDigCollection der MM-Dateien. |
| `mapMVW`                | Das Element `"mapMVW"` gibt den Pfad zu einer JSON-Datei an, in der die MultiVolumeWork-IDs zusammen mit einer Liste der dazugehörigen Volume-IDs gespeichert sind. |
| `mapChildren`           | Dieses Element spezifiziert den Pfad zu einer JSON-Datei, in der die MultiVolumeWork-IDs zusammen mit einer Liste der dazugehörigen Volume-IDs gespeichert sind. |
| `importFirst`           | Dieser Parameter legt fest, wie viele Vorgänge zuerst angelegt werden sollen. Wenn der Wert 0 ist, werden alle Vorgänge erstellt. |
| `listIDs`               | Dieser Parameter gibt den Pfad zu einer Textdatei an, in der eine Liste von IDs enthalten ist. Wenn die Datei existiert und nicht leer ist, werden nur Vorgänge für diese IDs erzeugt. Dies wird genutzt, um nachträglich geänderte oder verbesserte Vorgänge neu zu importieren. |
| `allMono`               | Dieser Parameter ist auf "true" zu setzen, wenn alle zu importierenden Dokumente als "Monograph" gespeichert werden sollen, auch wenn sie Kinder enthalten, anstatt als Volume. |
