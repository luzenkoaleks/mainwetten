# Mainwetten

Mainwetten ist eine produktiv bereitgestellte Spring-Boot-Webanwendung zur Organisation von Angelwetten und Angel-Challenges.

Benutzer können Wetten erstellen, andere Teilnehmer einladen, Fänge erfassen und automatisch Ranglisten berechnen lassen. Die Anwendung ist für Smartphones optimiert und ermöglicht es, einen realen Fang mehreren gleichzeitig laufenden Wetten zuzuordnen.

**Produktive Anwendung:** [www.mainwetten.de](https://www.mainwetten.de)

## Funktionen

### Benutzerkonten

* Registrierung mit Benutzername, E-Mail-Adresse und Passwort
* Bestätigung der E-Mail-Adresse über einen zeitlich begrenzten Link
* Login per Benutzername oder E-Mail-Adresse
* „Angemeldet bleiben“-Funktion
* Änderung des Passworts
* Zurücksetzen des Passworts per E-Mail
* Invalidierung bestehender Sitzungen nach einer Passwortänderung
* Vollständige Löschung des eigenen Kontos
* Erneute Registrierung mit einer E-Mail-Adresse nach vollständiger Kontolöschung

### Angelwetten

* Erstellung von Wetten mit Titel, Zeitraum, Fischkategorie und Bewertungsmodus
* Unterscheidung zwischen Süßwasser-, Salzwasser- und gemischten Wetten
* Einladung anderer Benutzer über ihren Benutzernamen
* Annahme und Ablehnung von Einladungen
* Übersicht über aktive, kommende und abgelaufene Wetten
* Zugriffsschutz für Wetten, Teilnehmer und Fänge

### Fänge

* Eintragung eines Fangs direkt innerhalb einer Wette
* Globaler Fang-eintragen-Workflow
* Zuordnung eines realen Fangs zu mehreren passenden Wetten
* Automatische Erfassung des Eintragungszeitpunkts
* Prüfung von Wettzeitraum, Teilnehmerstatus und Fischkategorie
* Nachtragefrist für Fänge bis zu 48 Stunden nach Wettende
* Gruppierte Fangübersicht nach Fischart und Teilnehmer
* Kennzeichnung des besten Fangs einer Fischart

### Ranglisten

* Automatische Punkteberechnung
* Rangfolge nach dem ausgewählten Bewertungsmodus
* Artenbonus bei eindeutig höchster Artenvielfalt
* Tiebreaker bei Punktegleichstand
* Mobile Darstellung der Rangliste

## Bewertungslogik

Mainwetten unterstützt zwei Bewertungsmodi.

### Alle Fänge zählen

Für jede Fischart werden vergeben:

* 1 Punkt für jeden Teilnehmer, der mindestens einen Fisch dieser Art gefangen hat
* 1 Punkt für den größten Fisch dieser Art
* 1 Punkt für die meisten Fänge dieser Art

Zusätzlich erhält genau ein Teilnehmer einen Artenbonuspunkt, wenn er mehr verschiedene Fischarten als alle anderen Teilnehmer gefangen hat. Bei einem Gleichstand wird kein Artenbonus vergeben.

### Bester Fang pro Fischart zählt

Für jede Fischart werden vergeben:

* 1 Punkt für jeden Teilnehmer, der mindestens einen Fisch dieser Art gefangen hat
* 1 Punkt für den größten Fisch dieser Art

Der Punkt für die meisten Fänge entfällt. Der Artenbonus wird nach denselben Regeln wie im Modus „Alle Fänge zählen“ vergeben.

### Tiebreaker

Bei Punktegleichstand wird die Gesamtlänge der jeweils größten Fänge in den Fischarten verglichen, die von allen gleichauf liegenden Teilnehmern gefangen wurden.

Fischarten, die nicht von allen betroffenen Teilnehmern gefangen wurden, fließen nicht in den Tiebreaker ein.

## Technologiestack

* Java 21
* Spring Boot 4
* Spring Web MVC
* Spring Security
* Spring Data JPA
* Thymeleaf
* PostgreSQL
* Flyway
* Maven Wrapper
* Bootstrap
* Docker Compose für die lokale PostgreSQL-Datenbank

## Architektur

Die Anwendung verwendet eine klassische serverseitige MVC-Struktur:

* Controller verarbeiten HTTP-Anfragen und Formularaktionen
* Services enthalten Fach- und Sicherheitslogik
* Repositories kapseln Datenbankzugriffe
* Thymeleaf rendert die HTML-Seiten
* Flyway verwaltet versionierte Datenbankmigrationen
* Spring Security übernimmt Authentifizierung, Autorisierung und CSRF-Schutz

## Lokales Setup

### Voraussetzungen

* Java 21
* Docker
* Docker Compose

Ein lokal installiertes Maven ist nicht erforderlich, da der Maven Wrapper im Repository enthalten ist.

### Datenbank starten

Im Projektverzeichnis:

```bash
docker compose up -d
```

### Anwendung starten

```bash
./mvnw spring-boot:run
```

Die Anwendung ist anschließend erreichbar unter:

```text
http://localhost:8080
```

### Anwendung nach einem Neustart starten

```bash
cd ~/dev/mainwetten
docker compose up -d
./mvnw spring-boot:run
```

Der Pfad `~/dev/mainwetten` muss gegebenenfalls an den lokalen Speicherort des Projekts angepasst werden.

### PostgreSQL stoppen

```bash
docker compose down
```

Die Daten bleiben dabei im Docker-Volume erhalten.

Zum vollständigen Entfernen der lokalen Datenbank einschließlich ihrer Daten:

```bash
docker compose down -v
```

## Tests

Alle automatisierten Tests ausführen:

```bash
./mvnw test
```

Frischen Build einschließlich Tests erstellen:

```bash
./mvnw clean test
```

Produktions-JAR erstellen:

```bash
./mvnw clean package
```

Das erzeugte JAR befindet sich anschließend im Verzeichnis:

```text
target/
```

## Konfiguration

Die Konfiguration erfolgt über Spring-Profile und Umgebungsvariablen.

Für die lokale Entwicklung sind für die Datenbank Standardwerte hinterlegt.

| Variable       | Beschreibung                      | Lokaler Standardwert                          |
| -------------- | --------------------------------- | --------------------------------------------- |
| `DB_URL`       | JDBC-URL der PostgreSQL-Datenbank | `jdbc:postgresql://localhost:5432/mainwetten` |
| `DB_USER`      | Datenbankbenutzer                 | `mainwetten`                                  |
| `DB_PASSWORD`  | Datenbankpasswort                 | `mainwetten`                                  |
| `SHOW_SQL`     | Ausgabe von SQL-Anweisungen       | `false`                                       |
| `APP_BASE_URL` | Basis-URL für Links in E-Mails    | `http://localhost:8080`                       |

Eine Beispielkonfiguration befindet sich in `.env.example`.

### Produktionsvariablen

Für den Produktivbetrieb werden unter anderem folgende Variablen benötigt:

```text
SPRING_PROFILES_ACTIVE
PORT

APP_BASE_URL

DB_URL
DB_USER
DB_PASSWORD

REMEMBER_ME_KEY

MAIL_FROM
MAIL_HOST
MAIL_PORT
MAIL_USERNAME
MAIL_PASSWORD
MAIL_SMTP_AUTH
MAIL_STARTTLS_ENABLED
MAIL_STARTTLS_REQUIRED

LEGAL_FULL_NAME
LEGAL_STREET
LEGAL_POSTAL_CODE
LEGAL_CITY
LEGAL_COUNTRY
LEGAL_EMAIL
LEGAL_SUPERVISORY_AUTHORITY_NAME
LEGAL_SUPERVISORY_AUTHORITY_ADDRESS
```

Passwörter, Schlüssel und andere Secrets dürfen niemals im Repository gespeichert oder in Commits aufgenommen werden.

## Datenmodell der Fänge

Fänge werden normalisiert gespeichert:

* `catch_record` beschreibt einen realen Fang
* `catch_assignment` ordnet diesen Fang einer oder mehreren Wetten zu

Dadurch kann ein Fang mehreren Wetten zugeordnet werden, ohne Fischart, Länge und Besitzer mehrfach zu speichern.

Beim Löschen eines Benutzerkontos werden die zugehörigen Fänge und Fangzuordnungen über referenzielle Datenbankregeln entfernt.

## Sicherheit und Validierung

Die Anwendung verwendet unter anderem:

* BCrypt für Passwort-Hashes
* CSRF-Schutz durch Spring Security
* serverseitige Formularvalidierung
* teilnahmebasierte Zugriffskontrollen
* zeitlich begrenzte Verifikations- und Passwort-Reset-Tokens
* Invalidierung bestehender Sitzungen nach Passwortänderungen
* Invalidierung persistenter Logins bei Logout und Kontolöschung
* case-insensitive Eindeutigkeit von Benutzernamen und E-Mail-Adressen
* Rate-Limiting für öffentliche Formulare
* Nutzungslimits für neu erstellte Wetten und eingetragene Fänge
* Content Security Policy
* HTTP Strict Transport Security
* Schutz vor Clickjacking und MIME-Sniffing
* restriktive Referrer- und Permissions-Policy
* reduzierte Fehlerdetails im Produktionsprofil
* externe Konfiguration produktiver Zugangsdaten und Secrets

## Nutzungslimits

Zum Schutz vor Missbrauch gelten derzeit unter anderem:

* maximal 10 neu erstellte Wetten innerhalb von 24 Stunden
* maximal 25 aktive oder kommende selbst erstellte Wetten
* maximal 100 eingetragene Fänge innerhalb von 24 Stunden

Die Prüfungen erfolgen serverseitig und werden durch Datenbankabfragen sowie Transaktionssperren abgesichert.

## Produktionsbetrieb

Die produktive Infrastruktur besteht aus:

* Railway für die Spring-Boot-Anwendung
* Railway PostgreSQL für die Datenbank
* Railway-Region Amsterdam für Anwendung und Datenbank
* tägliche und wöchentliche Datenbank-Volume-Backups
* ALL-INKL.COM für Domainverwaltung und E-Mail-Versand

Die kanonische Adresse lautet:

```text
https://www.mainwetten.de
```

Die Root-Domain:

```text
https://mainwetten.de
```

leitet dauerhaft per HTTP 301 auf die kanonische `www`-Adresse weiter.

## Datenschutz und rechtliche Hinweise

Die Anwendung enthält öffentlich erreichbare Seiten für:

* Impressum
* Datenschutzerklärung

Die für Impressum und Datenschutz erforderlichen Betreiberangaben werden über Umgebungsvariablen bereitgestellt und nicht fest im Quellcode gespeichert.

## Aktueller Status

Mainwetten ist produktiv bereitgestellt. Die Kernfunktionen wurden sowohl durch automatisierte Tests als auch durch manuelle Produktionstests überprüft.

Mögliche spätere Erweiterungen:

* Kumpanen-Liste für vereinfachte Einladungen
* Abstimmung über das Löschen gemeinsam genutzter Wetten
* zusätzliche Verwaltungs- und Komfortfunktionen
* weitere Fischarten und Kategorien
* zusätzliche externe Datenbanksicherungen
* weiterer UI- und Accessibility-Polish

## Lizenz

Dieses Projekt steht unter der Apache License 2.0.
