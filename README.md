# VanillaBoost Lab 0.1 - experimenteller Quellcode fuer Fabric 26.2

## Status zuerst

Dies ist KEINE fertige installierbare Mod-JAR. Das ZIP enthaelt ein neu geschriebenes,
kleines Entwicklungsprojekt, keinen Sodium-Fork und keinen eigenen Renderer.
Es wurde hier weder mit Java kompiliert noch in Minecraft gestartet: JDK, Gradle,
Minecraft-Abhaengigkeiten und Grafik-Testumgebung fehlen. Es gibt KEINE gemessenen
FPS-Werte und KEINE bestaetigte Sodium-/VulkanMod-Kompatibilitaet.

## Was tatsaechlich implementiert ist

Ein begrenzter, threadlokaler Identity-Paarcache fuer Shapes.faceShapeOccludes.
Er merkt sich das originale boolesche Ergebnis wiederholter Flaechenform-Vergleiche.
Hash-Kollisionen ersetzen Eintraege, liefern aber keine fremden Ergebnisse.
Je Thread maximal 1024 Paar-Slots, keine gemeinsame schreibbare Cache-Tabelle.
Keine veraenderten Texturen, Sichtweiten, Partikelmengen, Beleuchtungseinstellungen
oder Render-Backends. Keine Fabric-API-Abhaengigkeit.

WICHTIG: Dieser einzelne Eingriff kann in vielen Szenen gar nichts bringen oder
langsamer sein. Vanilla und Sodium besitzen bereits eigene Caches; Sodium kann
diesen Codepfad umgehen. Dies ist kein Ersatz fuer Sodium und kein breites
Optimierungspaket. Nicht als grossen FPS-Booster veroeffentlichen.

## Sicherheit und Grenzen

Der Cache ist standardmaessig AUS und muss nach dem Kompilieren bewusst aktiviert
werden. Seine Korrektheit setzt voraus, dass zwischengespeicherte Shapes waehrend
ihrer Nutzung unveraenderlich bleiben. Fremde Mods koennen diese Annahme brechen.
Auch das unveraenderte Aussehen ist deshalb ein Testziel und keine Garantie.
Client-only bedeutet nicht, dass die gemeinsame Shapes-Klasse im integrierten
Server einer Einzelspieler-Instanz unberuehrt bleibt. Keine Installation auf
Dedicated Servern. Zuerst in einem separaten Profil und einer Weltkopie testen.
Begrenzte starke Referenzen bleiben je Thread bis zur Verdraengung/Thread-Beendigung.
Keine automatischen Welt-/Resource-Reload-Invalidierungshooks implementiert.
Mixin-Ziele sind absichtlich erforderlich: Eine unerwartete Methodenaenderung soll
sichtbar fehlschlagen statt eine scheinbar funktionierende Optimierung vorzutäuschen.

## Bauen

Voraussetzungen: JDK 25, Gradle 9.5.1 und Internet fuer die Abhaengigkeiten.
Die Versionsbasis ist Fabric Loom 1.17-SNAPSHOT / Loader 0.19.3 / Minecraft 26.2.
Das SNAPSHOT ist nicht reproduzierbar gepinnt und kann sich spaeter aendern.
Kein Gradle-Wrapper-Binaerprogramm ist beigefuegt.

Im entpackten Projekt ausfuehren:

```sh
gradle cacheSelfTest build
```

Erst bei erfolgreichem Build die JAR ohne `-sources` aus `build/libs` in den
`mods`-Ordner eines separaten Fabric-26.2-Profils kopieren.

## Aktivieren und pruefen

Zuerst Validierungsmodus als zusaetzliche JVM-Argumente des Launchers verwenden:

```text
-Dvanillaboost.cache=true -Dvanillaboost.validate=true
```

Dabei wird Vanilla NICHT uebersprungen. Wiederholte Ergebnisse werden verglichen;
ein gefundener Widerspruch deaktiviert den Cache fuer die laufende Sitzung.
Das ist eine Stichprobe anhand beobachteter Paare, kein Beweis fuer Fehlerfreiheit.
Keine Fehlermeldung beweist weder aktive Mixins noch Kompatibilitaet.

Fuer den eigentlichen Leistungstest nach einem Neustart nur:

```text
-Dvanillaboost.cache=true
```

Ohne diese Argumente ist das Experiment aus. Zum vollstaendigen Entfernen die
JAR aus dem mods-Ordner nehmen. Keine Welt- oder Konfigurationsdateien werden erstellt.

## Kompatibilitaet als Testmatrix, nicht als Behauptung

Separat testen: Fabric allein; Fabric plus passende Sodium-Version; Minecrafts
OpenGL-Backend; Minecrafts Vulkan-Backend, soweit Hardware und Mods es unterstuetzen.
VulkanMod ist ein gesonderter Renderer-Mod, nicht derselbe Begriff wie der
Vulkan-Backend-Schalter in Minecraft 26.2. Dessen exakte Version muss separat getestet
werden. Renderer-Mods nicht ohne deren eigene Freigabe miteinander kombinieren.
Das Projekt importiert weder Sodium noch VulkanMod und verwendet keine direkten
Grafik-API-Aufrufe. Daraus folgt lediglich eine geringe direkte Kopplung.

## Messprotokoll

- Dasselbe Profil, dieselbe Weltkopie, Aufloesung, Sichtweite, Wetter und Route.
- VSync aus; FPS-Limit fuer den Vergleich gleich und ausreichend hoch.
- Erst aufwaermen, dann mehrere gleich lange Durchlaeufe mit/ohne Mod vergleichen.
- Durchschnitt und langsame Frames (1%-Lows/Frametimes) getrennt bewerten.
- Zusaetzlich Screenshots, Chunk-Neuladen, Resource-Reloads, Dimensionwechsel,
  Treppen, Slabs, Glas, Wasser und Einzelspieler-Kollisionen pruefen.
- Bei schlechteren Frametimes, Geometriefehlern oder Konflikten entfernen.

## Warum nicht Rust statt Vulkan?

Rust ist eine Programmiersprache; OpenGL und Vulkan sind Grafik-APIs. Ein in Rust
implementierter GPU-Renderer braucht normalerweise weiterhin eine Grafik-API.
Ein Software-Renderer ohne diese APIs waere moeglich, ist fuer dieses FPS-Ziel
aber kein sinnvoller Standardweg. Dieses Projekt bleibt deshalb backend-neutral.

## Recherchebasis

https://fabricmc.net/2026/06/15/262.html
https://github.com/FabricMC/fabric-example-mod/blob/26.2/build.gradle
https://aldak.netlify.app/javadoc/26.2.x/net/minecraft/world/phys/shapes/shapes

Diese Quellen belegen Toolchain/API-Konventionen, keine Laufzeit-Kompatibilitaet
oder Leistung dieses Projekts. Die Implementierung wurde fuer diese Anfrage neu
verfasst; zugrunde liegende Algorithmen wie Memoization sind allgemeine Techniken.
