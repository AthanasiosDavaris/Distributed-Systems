# Lab: Java TCP sockets (request/response) (Programmatismos Katanemimenon Systhmaton)

DIT UoP

## Student Details

- **Ονοματεπώνυμο:** ΑΘΑΝΑΣΙΟΣ ΝΤΑΒΑΡΗΣ
- **Α.Μ.:** 2022202200150

## Folder structure

- `src/gr/uop/ds/lab4/` Java sources
- `tests/` simple test cases (inputs -> expected outputs)

## Compile

From the lab folder:

```bash
mkdir -p out
javac -d out src/gr/uop/ds/lab4/*.java
```

## Run the server

```bash
java -cp out gr.uop.ds.lab4.TcpServer 5555
```

If you get `BindException: Address already in use`, choose another port (e.g. 6000).

## Test with 2 terminals using nc

Terminal A:

```bash
nc localhost 5555
OK READY
ADD 10 25
OK 35
HELP ADD
OK ADD <a> <b> - Adds two integers and returns the sum. (Ex: ADD 5 3)
```

Terminal B:

```bash
nc localhost 5555
OK READY
REVERSE hello
OK olleh
QUIT
OK bye
```

## Test with the included TcpClient

```bash
java -cp out gr.uop.ds.lab4.TcpClient localhost 5555 TIME
java -cp out gr.uop.ds.lab4.TcpClient localhost 5555 "ADD 5 10"
java -cp out gr.uop.ds.lab4.TcpClient localhost 5555 "REVERSE distributed"
```

Notes:

- The protocol is single-line request/response.
- Responses are always `OK ...` or `ERR ...`.
- Available commands: ECHO, TIME, UPPER, LOWER, REVERSE, ADD, LEN, HELP, QUIT.
