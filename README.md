# Smart E-Health Consulting System

Eine Desktop-Anwendung in Java (Swing), mit der Patienten Arzttermine passend zu ihren Beschwerden buchen. Sie findet Fachärzte im gewünschten Umkreis, bestätigt Termine per E-Mail und erinnert rechtzeitig daran.

Entstanden als Gruppenprojekt mit vier Personen im Modul „Object-Oriented Programming in Java – Advanced Course“ im Wintersemester 2021/22.

<p align="center">
  <img src="docs/make-appointment.png" width="720" alt="Formular zur Terminbuchung: Beschwerde Erkältung, Datum, Uhrzeit, Suchradius 5 km und eine Liste passender Hausärzte">
</p>

## Funktionen

- **Konten:** Registrierung und Login. Passwörter werden gehasht in der Datenbank gespeichert.
- **Admin-Bereich:** Alle Profile ansehen, bearbeiten und löschen.
- **Termin buchen:** Passend zur Beschwerde werden Fachärzte im gewählten Suchradius aufgelistet. Die Entfernung zwischen Patient und Praxis berechnet die Anwendung aus den Adressen, die Koordinaten kommen von OpenStreetMap (Nominatim).
- **Bestätigung per E-Mail** mit Arzt, Adresse, Datum und Uhrzeit.
- **Erinnerungen** per E-Mail, wahlweise eine Woche, drei Tage, eine Stunde oder zehn Minuten vor dem Termin.
- **Termine verschieben oder absagen**, jeweils mit E-Mail an den Patienten.
- **Gesundheitsdaten** wie Medikamente, Allergien und Vorerkrankungen pflegen und als PDF oder TXT exportieren.

## Screenshots

<table>
  <tr>
    <td width="50%" align="center"><img src="docs/login.png" alt="Login mit E-Mail oder Benutzername und Passwort"><br><sub>Login</sub></td>
    <td width="50%" align="center"><img src="docs/dashboard.png" alt="Dashboard mit Profil, Terminbuchung, Terminverwaltung und Gesundheitsdaten"><br><sub>Dashboard</sub></td>
  </tr>
  <tr>
    <td width="50%" align="center"><img src="docs/cancel-appointment.png" alt="Liste gebuchter Termine mit Buttons zum Absagen und Verschieben"><br><sub>Termine absagen oder verschieben</sub></td>
    <td width="50%" align="center"><img src="docs/health-information.png" alt="Gesundheitsdaten mit Medikamenten, Allergien und Vorerkrankungen sowie Export als PDF oder TXT"><br><sub>Gesundheitsdaten mit Export</sub></td>
  </tr>
  <tr>
    <td colspan="2" align="center"><img src="docs/admin-users.png" width="85%" alt="Admin-Ansicht mit Tabelle aller Nutzer und Formular zum Bearbeiten"><br><sub>Admin-Bereich: Nutzer ansehen, bearbeiten und löschen</sub></td>
  </tr>
</table>

Die Screenshots zeigen Beispielnutzer, die Ärzte und Beschwerden stammen aus [`database.sql`](database.sql).

## Technik

Java 17, Swing (IntelliJ GUI Designer), MySQL über JDBC, JavaMail, `java.util.Timer` für die Erinnerungen, iText für den PDF-Export, JCalendar für die Datumsauswahl, OpenStreetMap Nominatim für die Umkreissuche

<p align="center">
  <img src="ClassDiagram.PNG" width="720" alt="Klassendiagramm des E-Health-Systems">
</p>

Die Aufgabenstellung steht in [eHealth.pdf](eHealth.pdf), die Projektdokumentation mit Anforderungen, Use Cases, Aktivitäts- und Sequenzdiagrammen in [javaProject.pdf](javaProject.pdf). Beide sind auf Englisch.

## Starten

Das Projekt ist ein IntelliJ-IDEA-Projekt. Die Oberflächen sind mit dem GUI Designer von IntelliJ gebaut, deshalb lässt es sich dort am einfachsten starten.

1. Den Ordner in IntelliJ IDEA öffnen und ein JDK 17 einstellen.
2. Die JARs aus `lib/` und `lib/Quartz/` als Bibliothek hinzufügen.
3. Eine MySQL-Datenbank `ehealth` anlegen und [`database.sql`](database.sql) importieren. Die Datei enthält die Tabellen und Beispieldaten für Ärzte und Beschwerden.
4. Die eigenen Zugangsdaten in `src/main/java/DatabaseConnector.java` und den Mail-Zugang in `src/Email/Mail.java` eintragen.
5. `src/main/java/Main.java` starten.

Für die Umkreissuche braucht die Anwendung eine Internetverbindung, weil sie die Adressen bei OpenStreetMap nachschlägt.
