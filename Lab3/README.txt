Programmatismos Katanemimenon Sytsthmaton Lab Demo (Multi-client server + basic synchronization)

1) Compile
   javac -d out $(find src -name "*.java")

2) Run server
   java -cp out gr.uop.ds.lab6.ChatServer 5555
   (optional) pool size:
   java -cp out gr.uop.ds.lab6.ChatServer 5555 16

3) Quick manual test with 3 terminals (netcat)
   Terminal A:  nc localhost 5555
     OK READY
     NICK alice
     OK NICK alice
     SAY hello
     OK SENT

   Terminal C:  nc localhost 5555
     OK READY
     NICK charlie
     OK NICK charlie
     (when alice says hello)
     MSG alice hello

   Terminal B: connect + NICK bob, then close the terminal (Ctrl-D)
   Server should NOT crash; WHO should not list bob afterward.

4) Load test
   java -cp out gr.uop.ds.lab6.LoadTest localhost 5555
   (optional) override: host port clients msgsPerClient
   java -cp out gr.uop.ds.lab6.LoadTest localhost 5555 10 20
