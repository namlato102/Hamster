# HamsterLib

Wer länger unterwegs ist, kann seine Goldhamster bei einem westhessischen
Hamsterverwahrungsunternehmen abgeben. Die Verwaltung der Gasthamster soll
nun durch ein neues IT-System unterstützt werden. Die Kunden können ihre
Hamster abgeben und ihnen dabei optional einen Vorrat an Leckerli mitgeben. Für
die Aufnahme eines Hamsters wird einmalig ein Grundbetrag von 17€ fällig.
Die Hamster haben nichts besseres zu tun als ständig in ihrem Laufrad zu
rennen, wodurch dieses mit einer Drehzahl von 25 Umdrehungen pro Minute
rotiert. Diese Umdrehungen werden vom System erfasst und pro 1000 Umdrehungen
erhöht sich der am Ende zu zahlende Betrag um 5€. Die Kunden können anrufen und
sich nach dem Wohlergehen ihrer Lieblinge erkundigen. Für jeden Anruf
dieser Art wird 1€ berechnet. Ausserdem können die Kunden ihrem Hamster
Leckerli geben lassen. Ist dabei der anfängliche Vorrat an Leckerli erschöpft,
so stellt das Hamsterasyl natürlich gerne weitere Leckerli zur Verfügung -- 
allerdings zum Stückpreis von 2€.

Zufällig entdeckt die IT-Abteilung des Hamsterverwahrungsunternehmens
die Open-Source-Bibliothek *Hamsterlib*, die exakt die benötigten Funktionen implementiert,
und die somit verwendet werden soll. Den Quellcode zu dieser Bibliothek
finden Sie in diesem Git-Repository. Eigenartigerweise steht der Quellcode auch noch in
mehreren Programmiersprachen zur Verfügung. 

Machen Sie sich anhand der Dokumentation und dem Quellcode in \texttt{libsrc}
mit den API-Funktionen der Bibliothek vertraut. Insgesamt bietet die Bibliothek
sieben Funktionen.

Erstellen Sie daraus ein Programm **hamster**, das mit einem Kommandoparameter
und (je nach Kommando) evtl. weiteren Parametern aufgerufen wird, und das
folgende Funktionen unterstützt:

- `hamster list`

	Gibt eine Liste mit dem gesamten Hamsterbestand  (Namen, Preise und Leckerli-Vorrat) tabellarisch aus.

- `hamster list Meier`

	Gibt eine Liste mit den Hamstern des Besitzers *Meier* aus.

- `hamster add Schmidt Pausbackenbube`

	Fügt einen neuen Datensatz für den Hamster *Pausbackenbube* des Besitzers *Schmidt* mit dem
	Standard-Anfangspreis 17€ (= Vollpension zzgl. Laufradbenutzung) hinzu.

- `hamster add Schmidt Pausbackenbube 55`

	Wie oben, jedoch erhält  *Pausbackenbube* einen Vorrat von 55 Leckerli mit auf den Weg.

- `hamster feed Bilbo Baggins 3`

	Verfüttere 3 Leckerli an den Hamster *Baggins* des Besitzers *Bilbo*.
	Falls der Leckerli-Vorrat von *Baggins* erschöpft ist, erhöht sich der Preis
	um 2€ je Leckerli.

- `hamster state Dirk Dickbacke`

	Zustandsabfrage des Hamsters *Dickbacke* des Besitzers *Dirk*. Geliefert
	wird die Anzahl der Laufradumdrehungen, die Größe des Leckerlivorrats
	und der aktuelle Preis, der sich durch die Abfrage um jeweils 1€ erhöht.

- `hamster bill Bigspender`
	Gibt den vom Besitzer *Bigspender* zu zahlenden Gesamtbetrag (Summe über alle seine Hamster)
	aus und löscht alle Datensätze von *Bigspender*.

**Hinweise**
- Falsche oder fehlende Benutzereingaben müssen abgefangen und mit einer Fehlermeldung zurückgewiesen werden. Ihr Programm sollte in solchen Fällen eine kurze Bedienungsanleitung ("RTFM-Text") ausgeben.
- Falsche Parameter (z.B. zu lange Besitzer- oder Hamsternamen) werden von der Bibliothek zurückgewiesen. Ihr Programm sollte in solchen Fällen eine qualifizierte Fehlermeldung ausgeben.
- Jede **Kombination** aus Hamster- und Besitzernamen darf nur ein Mal vorkommen. Versuche, dieselbe Namenskombination ein weiteres Mal einzutragen	werden von der Bibliothek erkannt und müssen mit einer qualifizierten Fehlermeldung zurückgewiesen werden.
- Im Verzeichnis *scripts* finden Sie ein Shell-Script *testhamster.sh*, mit dem Sie Ihr Programm gründlich testen können. Es ruft Ihr Programm mit verschiedenen Optionen auf und vergleicht die erhaltene Ausgabe mit der Ausgabe der Referenz-Implementierung.
