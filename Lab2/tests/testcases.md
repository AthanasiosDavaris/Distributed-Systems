# Test cases (inputs -> expected outputs)

Assume the server is running and the client has already received:

- `OK READY`

Then for each input line below, the server must reply with exactly one output line.

1) `TIME`
- OUT: `OK <timestamp>`

2) `FOO`
- OUT: `ERR 404 unknown-command`

3) `ECHO`
- OUT: `ERR 422 bad-args`

4) (empty line)
- OUT: `ERR 400 empty-request`

5) `QUIT`
- OUT: `OK bye` (and then the server closes the connection)
