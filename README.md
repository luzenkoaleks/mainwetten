# Mainwetten

Mainwetten ist eine Spring-Boot-Webanwendung zur Organisation von Angelwetten und Angel-Challenges. Nutzer können Wetten erstellen, andere Teilnehmer einladen, Fänge eintragen und die Rangliste automatisch berechnen lassen.

Das Projekt ist als Lern- und Portfolio-Projekt entstanden und legt Wert auf eine mobile-first Benutzeroberfläche, serverseitige Validierung und ein nachvollziehbares Datenmodell.

## Funktionen

* Registrierung und Login mit Spring Security
* Erstellung von Angelwetten mit Zeitraum, Bewertungsmodus und Fischkategorie
* Unterscheidung zwischen Süßwasser-, Salzwasser- und gemischten Wetten
* Einladungen zu Wetten mit Annehmen-/Ablehnen-Funktion
* Fang-Eintragung direkt innerhalb einer Wette
* Globaler Fang-eintragen-Workflow: ein Fang kann mehreren passenden Wetten gleichzeitig zugeordnet werden
* Automatische Prüfung von Zeitraum, Teilnahmeberechtigung und Fischkategorie
* Nachtragefrist für Fänge bis zu 48 Stunden nach Wettende
* Automatische Ranglistenberechnung
* Gruppierte Fangübersicht nach Fischart und Teilnehmer
* Mobile-first Layout für Dashboard, Wett-Detailseite und Fangformulare

## Bewertungslogik

Mainwetten unterstützt aktuell zwei Bewertungsmodi:

### Alle Fänge zählen

* 1 Punkt pro gefangener Fischart
* 1 Punkt für den größten Fisch je Fischart
* 1 Punkt für die meisten Fänge je Fischart
* 1 Artenbonuspunkt, wenn genau ein Teilnehmer die meisten verschiedenen Fischarten gefangen hat

### Bester Fang pro Fischart zählt

* 1 Punkt pro gefangener Fischart
* 1 Punkt für den größten Fisch je Fischart
* kein Punkt für die meisten Fänge je Fischart
* 1 Artenbonuspunkt, wenn genau ein Teilnehmer die meisten verschiedenen Fischarten gefangen hat

Bei Punktegleichstand wird ein Tiebreaker anhand der größten Fänge der gemeinsam gefangenen Fischarten berechnet.

## Technologiestack

* Java 21
* Spring Boot
* Spring Web MVC
* Spring Security
* Spring Data JPA
* Thymeleaf
* PostgreSQL
* Flyway
* Maven
* Bootstrap
* Docker Compose für die lokale Datenbank

## Lokales Setup

### Voraussetzungen

* Java 21
* Docker und Docker Compose
* Maven Wrapper ist im Projekt enthalten

### Datenbank starten

```bash
docker compose up -d
```

### Anwendung starten

```bash
./mvnw spring-boot:run
```

Die Anwendung ist danach unter folgender Adresse erreichbar:

```text
http://localhost:8080
```

## Tests ausführen

```bash
./mvnw test
```

Für einen frischen Build inklusive Tests:

```bash
./mvnw clean test
```

## Konfiguration

Die Datenbankkonfiguration wird über Umgebungsvariablen unterstützt. Für lokale Entwicklung sind Standardwerte hinterlegt.

| Variable      | Beschreibung                      | Standardwert                                  |
| ------------- | --------------------------------- | --------------------------------------------- |
| `DB_URL`      | JDBC-URL zur PostgreSQL-Datenbank | `jdbc:postgresql://localhost:5432/mainwetten` |
| `DB_USER`     | Datenbankbenutzer                 | `mainwetten`                                  |
| `DB_PASSWORD` | Datenbankpasswort                 | `mainwetten`                                  |
| `SHOW_SQL`    | SQL-Logging aktivieren            | `false`                                       |

Eine Beispielkonfiguration befindet sich in `.env.example`.

## Datenmodell

Fänge werden normalisiert gespeichert:

* `catch_record` beschreibt einen realen Fang
* `catch_assignment` ordnet diesen Fang einer oder mehreren Wetten zu

Dadurch kann ein Fang mehreren Wetten zugeordnet werden, ohne die eigentlichen Fangdaten mehrfach speichern zu müssen.

## Sicherheit und Validierung

Das Projekt verwendet:

* BCrypt für Passwort-Hashes
* CSRF-Schutz durch Spring Security
* serverseitige Formularvalidierung
* Zugriffskontrollen auf Basis akzeptierter Wett-Teilnahmen
* externe Konfiguration für Datenbankzugangsdaten
* reduzierte Fehlerdetails in produktionsnaher Konfiguration

Echte Zugangsdaten oder produktive Secrets sollten nicht im Repository gespeichert werden.

## Aktueller Status

Das Projekt befindet sich noch in aktiver Entwicklung. Der aktuelle Stand ist als funktionsfähiges Portfolio-/Lernprojekt gedacht.

Geplante oder mögliche Erweiterungen:

* Login per Benutzername oder E-Mail-Adresse
* E-Mail-Verifikation
* Passwort-zurücksetzen-Funktion
* Kumpanen-Liste für einfachere Einladungen
* Löschfunktion für Wetten mit Abstimmung bei mehreren Teilnehmern
* weiterer Design-Polish
* Deployment-Vorbereitung mit Datenschutz-/Impressumsseiten

## Lizenz

Noch keine Lizenz festgelegt.
