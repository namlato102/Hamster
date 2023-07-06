# Projekt “Social Media Authentifizierung für die Hamsterverwaltung aka. HamsterAuth”, 20 Punkte

Beflügelt vom Erfolg des IoH hat der CEO des allseits bekannten
westhessischen Anbieters von Kleintier-Management Lösungen beschlossen,
die Hamsterverwaltung so umzustellen, dass Kunden nun durch eine
Webanwendung selbst den Status ihrer lieben Kleinen abfragen können.
Hierzu wurde für den bestehenden REST-Service von einer befreundeten
Agentur auch eine Webanwendung ~~gebastelt~~ entwickelt.

Um die am besten geeignete Plattform zu sondieren, wurde auch gleich
eine international renomierte Agentur engagiert, die die Verbreitung von
verschiedenen Social Media Accounts in der relevanten Zielgruppe
untersuchen sollte.

In umfangreichen Marketingstudien dieser Agentur hat sich klar
herausgestellt, dass unter allen Teilnehmenden eine überraschend
deutliche Mehrheit einen HDZ-Account besitzt. Etwaige Bedenken, dass
sich im Freundeskreis des Werkstudenten, der mit der Durchführung der
Studie betraut worden ist, einfach besonders viele Komilitonen befanden
wurden zur Kenntnis genommen aber ansonsten als unwesentlich übergangen.

Für die Integration des HDZ-Accounts wurde der Standard Open ID Connect
ausgewählt, da der CTO beim Golfen vernommen hat, dass man damit sehr
flexibel Accounts von verschiedensten IDPs integrieren können soll.
Außerdem hat die besagte Agentur anhand eines YouTube-Videos auch schon
eine Unterstützung von Open ID Connect in die entstandene Single Page
Application eingebaut.

Leider wurde ein Großteil des Budgets bereits auf besagte
Marketingstudien verwendet und wieso auch teures Geld für externe
Berater verwenden, wenn man über interne Ressourcen verfügt, die so
etwas schnell erledigen können? Schließlich existiert die Funktionalität
ja schon, Sie müssen sie nur noch geeignet integrieren. Meint zumindest
der CTO. Tatsächlich bezog sich die Agentur mit “Open ID Connect
eingebaut” nur auf das Frontend. Für das Backend hat die Agentur
eigentlich auch nur ein Skelett bekommen, mit dem gearbeitet wurde. Es
ist also nicht viel mehr als ein Scherbenhaufen, den Sie jetzt aufkehren
sollen.

## Zusammenfassung der Aufgabe

-   Sie haben dieses mal bereits einen RESTful Webservice gegeben, der
    allerdings derzeit unverschlüsselt auf HTTP Anfragen entgegen nimmt
    und die Gegenseite sich nicht authentifizieren muss.

-   Ihre Aufgabe besteht nun darin, den Webservice abzusichern. Diese
    Absicherung soll zum Einen aus der Verwendung einer verschlüsselten
    Verbindung, zum Anderen aus der Authentifikation und Autorisierung
    von Anfragen bestehen.

-   Für die Authentifizierung und Autorisierung der Anfragen soll das
    aus der Vorlesung bekannte Protokoll OAuth 2.0 verwendet werden.

-   Die Endpunkte im Backend bestehen derzeit noch aus Skeletten. Sie
    müssen Sie geeignet implementieren, indem Sie die Funktionalität auf
    Grundlage der bekannten Hamsterlib implementieren.

-   Die Endpunkte wurden von der Agentur auch so angepasst, dass nun der
    Owner-Name nirgendwo mehr per Requestparameter mit übergeben wird.
    Stattdessen sollen Sie nun den Namen des Users eintragen, der den
    Request veranlasst hat.

-   Es gibt einen zentralen Authorization Server für diese Aufgabe, der
    auf der Open-Source Lösung Keycloak aufbaut. Dieser ist an das LDAP
    der Hochschule angebunden. Sie können sich bei diesem Server daher
    mit Ihrem HDS-Account anmelden.

## Token

Die Webanwendung wird HTTP Requests an Ihren Webserver schicken, die mit
einem Authorization-Header versehen sind, in denen in einem Bearer ein
signiertes JWT-Token enthalten ist. Schauen Sie sich diese Token bspw.
mittels <https://jwt.io> an. Wie lange sind die Token gültig? Welche
Informationen sind darin enthalten? Mit welchem Algorithmus wurde der
Token signiert? Welcher Flowtyp von OAuth bzw. Open ID Connect wird
verwendet?

## Webanwendung

Für diese Aufgabe steht eine fertige Webanwendung auf Basis von Angular
bereit. Sie finden in der Vorlage auch den Quellcode dieser Anwendung,
Sie können aber auch das Kompilat der Anwendung verwenden, was im
Webserver der Vorlage bereits als statischer Inhalt integriert ist.

Sollten Sie Anpassungen an der Webanwendung vornehmen wollen, benötigen
Sie für die Entwicklung von Angular-Anwendungen zwingend ein lokal
installiertes Node.js. Für die Bearbeitung des Übungsblatts ist dies
jedoch nicht notwendig.

## Authentifizierung

Ihre erste Aufgabe sollte es sein, abzusichern, dass ein Client den
Webserver nur mit passendem JWT erreichen kann. Außerdem benötigen Sie
die Informationen über den angemeldeten User, um die für die Ausführung
des Requests erforderlichen Informationen für die Hamsterlib zusammen zu
bekommen. Passen Sie jedoch auf, dass der Client trotzdem auch ohne
Authentifizierung auf statischen Content zugreifen darf, da er sonst das
Frontend nicht laden kann.

## Tests

Für diese Aufgabe existiert (noch) kein Framework, was Ihre Lösung
automatisiert testet. Daher ist auch die CI-Funktionalität begrenzt und
prüft nur, ob Ihre Lösung kompiliert und ob die Endpunkte ohne
entsprechenden Header eine Fehlermeldung zurückliefern.

## Hinweise C

Auch in diesem Übungsblatt geht es wieder um die Programmierung eines
RESTful Webservers, weswegen kein Template in der Programmiersprache C
zur Verfügung steht. Weichen Sie bitte auch dieses mal auf C# oder Java
aus.

## Hinweise Java

Verwenden Sie für die Prüfung der Token die Bibliothek Spring Security.
Diese ist in der Vorlage bereits eingefügt.

## Hinweise C#

Verwenden Sie für die Prüfung der Token das Paket
`Microsoft.AspNetCore.JwtBearer`. Dieses ist in der Vorlage bereits
eingefügt.