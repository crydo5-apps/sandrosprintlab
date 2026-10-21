# Sandros Print Labor — Android Kotlin-Prototyp

Native Android-Oberfläche mit Kotlin und Jetpack Compose für Dashboard und die zugehörigen Unterseiten im vereinbarten Cyber-Labor-Design.

## Enthalten

- Dashboard mit Projektstatus, Druckerprofil und SD-Kartenstatus
- Projektstart mit Android-Bildauswahl und STL/3MF-Dateiauswahl
- 3D-Modell-Editor als klar gekennzeichnete Demo-Ansicht
- Druckvorbereitung mit Materialauswahl und bearbeitbaren Demo-Einstellungen
- G-Code-/Exportseite mit absichtlich deaktiviertem echten Export, solange kein Slicer-G-Code liefert
- Projektbibliothek und Einstellungen
- Farbschema: Labor-Schwarz `#080B12`, Graphit `#151F2C`, Laser-Cyan `#38E8FF`, Electric Blue `#537CFF`, Plasma-Magenta `#D65CFF`
- Integrationsverträge für Foto-zu-3D-Rekonstruktion und Slicer in `IntegrationContracts.kt`

## Öffnen und starten

1. Öffne den Ordner `sandros-print-labor` in Android Studio (Jellyfish oder neuer empfohlen).
2. Verwende JDK 17 und installiere Android SDK Platform 35.
3. Synchronisiere Gradle und starte das `app`-Modul auf einem Emulator oder Android-Gerät.

Gradle-Konfiguration liegt in `settings.gradle.kts`, `build.gradle.kts` und `app/build.gradle.kts`.

## Noch nicht implementiert

- Eine tatsächliche Photogrammetrie-/KI-Rekonstruktion aus Fotos
- Interaktive STL-/3MF-3D-Ansicht und Mesh-Prüfung
- Echter Slicer und valide G-Code-Erzeugung für ein geprüftes CR-10S-Profil
- Export einer echten Slicer-Ausgabe über Android Storage Access Framework auf SD/OTG
- Persistente Projektdatenbank und Profile

Der Prototyp meldet diese Aufgaben nicht fälschlich als erfolgreich. Die Fotos und Modelle werden über Android-Dateiauswahl angefordert; es erfolgt kein Upload durch diesen Code. Der Speicherort-Dialog im Exportbereich ist ausdrücklich eine Demo und speichert keinen G-Code.

## Datenschutz und Drucksicherheit

Ein echter Rekonstruktionsdienst kann Fotos übertragen. Vor einer solchen Integration muss ein ausdrücklicher Zustimmungsdialog vorgesehen werden. G-Code ist nur nach echtem Slicing mit überprüftem Drucker- und Materialprofil als druckbereit zu kennzeichnen. Speichern einer Datei startet keinen Druck; der Druck wird am Drucker gestartet.
