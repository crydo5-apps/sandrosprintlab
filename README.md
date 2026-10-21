# Sandros Print Labor 🚀

Ein futuristisches 3D-Maker-Labor für Android zur Photogrammetrie-Modellierung, Vorbereitung und Slicing für den **Creality CR-10S Pro** 3D-Drucker mit sicherem SD-Karten-Export über das Android Storage Access Framework (SAF).

---

## 🌟 Hauptfunktionen

1. **Cyberpunk Maker Dashboard**
   - Übersicht aller 3D-Projekte mit Status (Entwurf, Modell bereit, Gesliced, Auf SD exportiert).
   - **Google Drive Auto-Sync:** Direkte Anbindung an den Google Drive Ordner `Projekte/3D-Drucker` mit Zugriff auf die 10 täglich automatisiert heruntergeladenen Top- & Neu-Modelle aus 3 Webseiten (Printables, MakerWorld, Thingiverse).
   - Statuskarte für das Creality CR-10S Pro Hardware-Profil (300 × 300 × 400 mm Druckvolumen, BLTouch Matrix-Leveling, Bowden-Extruder).
   - Erkennungs- und Bereitschaftsstatus für SD-Karten und USB-C-OTG-Speicher.

2. **3D-Foto-Assistent & Photogrammetrie**
   - Aufnahme und Auswahl von Fotobildserien mit Galerie-Mehrfachauswahl.
   - **360°-Rundum-Radar:** Zeigt grafisch, welche Blickwinkel (0° bis 315°) erfasst wurden.
   - **Ehrliche Qualitätsanalyse:** Ein einzelnes Foto wird transparent als ungenaue 2.5D-Schätzung deklariert; erst ab 8–16 Fotos wird eine präzise 3D-Rekonstruktion durchgeführt.
   - **Datenschutz & Upload-Zustimmung:** Klare Information vor jeglicher externer Datenübertragung.
   - 3-Phasen-Rekonstruktion: 1. Bilder & Feature-Punkte prüfen, 2. SfM/MVS-Punktwolken berechnen, 3. Poisson-Dreiecksnetz aufbereiten.

3. **Interaktiver 3D-Modell-Editor**
   - Vollständiger 3D-Canvas mit Touch-Gesten: Drehen (Pitch/Yaw), Zoomen (Pinch) und Pan.
   - Schnellumschaltung zwischen schattiertem Modell (Gouraud/Lambertian Shading) und Cyber-Drahtgittermodell (Wireframe).
   - Schnelltasten für Kamerapositionen (Isometrisch, Draufsicht) und Zentrieren auf dem Druckbett.
   - **Mesh-Prüfung & Diagnose:**
     - Wasserdichtigkeit (Manifold-Check, offene Kanten).
     - Minimale Wandstärken-Warnung (< 0.8 mm für 0.4 mm Düsen).
     - Überhangs-Inspektor (> 45° Flächen in Neon-Magenta hervorgehoben).
     - Automatische Bounding-Box-Prüfung gegen das 300 × 300 × 400 mm CR-10S Pro Bett.
   - Skalierungsregler (20% bis 300%) mit millimetergenauer Echtzeitanzeige.
   - Export und Import von realen Standard-ASCII- und Binär-STL-Dateien.

4. **Druckvorbereitung (Creality CR-10S Pro)**
   - Filament-Vorauswahl: PLA, PETG, TPU, ABS mit herstellerspezifischen Temperaturvorschlägen.
   - Editierbare Parameter: Schichthöhe (0.12, 0.20, 0.28 mm), Infill (Gyroid, Grid, Honeycomb), Geschwindigkeiten, Stützen (Support) und Druckbetthaftung (Skirt/Brim/Raft).
   - Bowden-spezifische Retraction: 5.0 mm bei 45 mm/s.
   - Berechnete Schätzwerte für Druckzeit, Filamentlänge und Materialgewicht.
   - Pflicht-Bestätigungsdialog für experimentelle Werte.

5. **G-Code-Erstellung (Marlin/Cura kompatibel)**
   - Echte Slicer-Pipeline mit schrittweiser Fortschrittsanzeige.
   - Erzeugt syntaktisch vollständigen, maschinenspezifischen G-Code für den CR-10S Pro:
     - Beschleunigungs- und Ruck-Grenzwerte (`M201`, `M204`, `M205`).
     - Bett- und Düsentemperatur-Sequenz (`M140`, `M104`, `M190`, `M109`).
     - Auto-Homing und 16-Punkt Matrix Bed Leveling (`G28`, `G29`, `M420 S1 Z2`).
     - CR-10S Pro Zweifach-Reinigungslinie am linken Bettrand (`X0.1 Y20 -> Y200`).
     - Schichtweiser Druckaufbau mit koordinierter Extrusion (`E`).
     - Sicheres End-Skript mit Bettauswurf nach vorne (`G1 X0 Y300`).
   - Schichten-Inspektor mit Schieberegler und G-Code-Viewer.

6. **SD-Karten-Export (Storage Access Framework)**
   - Android-System-Dateiauswahl (`CreateDocument`) zur direkten Auswahl der SD-Karte oder eines USB-OTG-Sticks.
   - Automatische Benennung nach Schema `CR10S_[Objekt]_[Material].gcode`.
   - Schreibüberprüfung: Verifiziert geschriebene Dateigröße und Lesbarkeit.
   - Schritt-für-Schritt Hardware-Anleitung für das Einsetzen in den CR-10S Pro MicroSD-Slot und das manuelle Starten am Touchscreen.

---

## 🛠️ Build- und Startanleitung

### Voraussetzungen
- Android Studio Ladybug / Meerkat (oder Gradle 8.13+ in dieser Build-Umgebung)
- JDK 17 oder 21
- Android SDK mit API Level 36 (minSdk 24)

### Kompilieren
Im Hauptverzeichnis des Projekts:
```bash
gradle :app:assembleDebug
```

### Unit Tests ausführen
```bash
gradle :app:testDebugUnitTest
```

---

## 🔌 Reale Integrationen (Backend & Hardware-Anbindung)

Für den Produktivbetrieb können folgende Schnittstellen direkt angebunden werden:

1. **Photogrammetrie-Server**
   - **Lokaler Demo-Dienst:** `com.example.service.photogrammetry.LocalPhotogrammetryService`
   - **Reale Anbindung:** In `LibraryAndSettingsScreen` kann eine Remote-URL für Meshroom (AliceVision CLI), COLMAP oder NerfStudio hinterlegt werden. Über einen Multipart-POST-Request werden die Fotos übertragen; der Server liefert das fertige `.stl` zurück.

2. **CuraEngine Backend**
   - **Lokaler Marlin-Slicer:** `com.example.service.slicer.CrealityCR10SProSlicer`
   - **Reale Anbindung:** Anbindung an eine serverbasierte `CuraEngine` oder Moonraker/OctoPrint-Instanz über HTTP-REST, um komplexe Freiform-Infill-Muster (wie 3D-Gyroid oder Voronoi) serverseitig zu berechnen.

3. **SD-Karten-Schreibzugriff**
   - Verwendet das native Android Storage Access Framework (`ActivityResultContracts.CreateDocument`). Unterstützt MicroSD-Kartenleser und USB-C-OTG-Adapter ohne Root-Rechte.

---

## ⚠️ Technische Einschränkungen & Nächste Schritte

- **Keine direkte Druckersteuerung ohne WLAN-Modul:** Der originale Creality CR-10S Pro besitzt standardmäßig kein WLAN, sondern wird autark über MicroSD-Karte betrieben. Die App täuscht daher bewusst keinen Live-Druckstart vor.
- **Speicherbedarf bei großen Punktwolken:** Für Modelle mit mehr als 100.000 Dreiecken wird serverseitiges Decimating (Mesh-Vereinfachung) empfohlen, um Android-Speicherlimits (OOM) zu vermeiden.
- **Nächste Schritte:**
  1. Anbindung von OctoPrint / Klipper Moonraker WebSocket-API für Nutzer, die ihren CR-10S Pro per Raspberry Pi vernetzt haben.
  2. Implementierung von ARCore (Google Play Services for AR) für interaktive Raumplatzierung vor dem Druck.
