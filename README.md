# HamsterRPC

Das zuvor entwickelte IT-System für das westhessische
Hamsterverwahrungsunternehmen ist bei den Mitarbeitern sehr gut angekommen. 
Durch dieses wichtige Hilfsmittel konnten weitere Kunden gewonnen werden und das
Unternehmen ist gewachsen. Inzwischen reicht ein Mitarbeiter und Arbeitsplatz
nicht mehr aus, um die ganzen Anfragen der Kunden zu bearbeiten. Es besteht daher
Bedarf an einer Version über die mehrere Mitarbeiter mit ihren PCs gleichzeitig 
auf das IT-System zugreifen können. Das existierende System soll aber nicht 
abgelöst, sondern aus Kostengründen nur mit Netzwerkfunktionalität 
erweitert werden. 

Die IT-Verantwortliche möchte, dass das Frontend praktisch gleich bleiben kann
und zusätzlich die Möglichkeit besteht mit moderneren Programmiersprachen neue
Frontends zu entwickeln. Auf einer Fortbildung hat sie gelernt, dass RPCs der
letzte Schrei sind und man das unbedingt machen muss, um ein erfolgreiches und
hippes Unternehmen zu sein.

Praktischerweise haben die Entwickler der Open-Source-Bibliothek *Hamsterlib*
ein passendes RPC-Protokoll für ihre Bibliothek spezifiziert. Die 
Implementierung des Protokolls wird allerdings kommerziell vertrieben, damit die
Entwickler ihre Miete bezahlen können. Dafür ist das 
Hamsterverwahrungsunternehmen allerdings zu geizig. Es von Ihnen neu 
implementieren zu lassen ist günstiger, zumal die IT-Verantwortliche beim Surfen
sogar einen fertigen Java-Client gefunden hat. Es wird also nur noch der Server
benötigt!

Der Server soll als Default nur lokal auf *localhost* also der IP-Adresse 
**127.0.0.1** laufen. Dieses Vorgehen ist generell immer sinnvoll. Es 
verhindert, dass Dienste aus Versehen über das Netzwerk oder gar das Internet 
zugegriffen werden können. Mit dem optionalen Kommandoparameter `-h IP-ADRESSE`
soll dann die IP-Adresse festgelegt werden auf der der 
Server seinen Dienst anbietet (optional im Sinne von: wenn nicht angegeben, wird 
die Default-Adresse gewählt). Halten Sie sich für die Syntax wieder an die 
rtfm()-Funktion. 

Ihre Aufgabe ist nun: Bauen Sie einen Server, der die Hamsterlib-Funktionen über 
das Hamster-RPC-Protokoll bereitstellt. Das heißt, ein Client schickt
eine Anfrage, um eine Funktion aufzurufen, Ihr Server empfängt und dekodiert 
diese, ruft dann die entsprechende Hamsterlib-Funktion auf, packt das Ergebnis
in eine Antwort und schickt diese an den Client.

Der Server soll als minimale Anforderung als einfacher sequenzieller Server 
aufgebaut sein. Beachten Sie, dass der Client für jeden RPC-Aufruf eine neue 
Verbindung aufbauen kann und Ihr Server natürlich in der Lage sein muss mehrere 
Anfragen nacheinander abarbeiten zu können.
