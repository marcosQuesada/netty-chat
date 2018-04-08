Netty Chat
======================

 Just an IRC chat using raw sockets

 ## Application flow
  From top to down:
  - Chat Service takes responsability on PUB/SUB features, all of them are presented as commands
  - CommandExecutor handles invocations on all registered commands, takes care too about execution thread pool
  - Router handles client lifecycles and forward requests to CommandExecutor
  - Client connections are represented as Sessions (User profiles), sessions are created once a user has valid auth
  credentials
  - Socket connections are handled using Netty pipes:
    - String based messages are decoded to Commands
    - Auth middleware intercepts all requests, handles authentication and forward all requests to client handler
    on valid credentials, if not, just reject connections.
    - ClientHandler acts as pipe termination

 ## Threading Model
  - Requests handled by Router::receiveMessage are moved by netty handlerGroup, so that, command execution is
  delegatedto its own thread pool ("serverExecutor"), isolating netty requests from command executions.

 ## Exposed Commands:
  - login: if user does not exist create profile else login.
    -  /login name password
  - join: try to join chat room (max 10 active clients per chat room).
 If chat room does not exist - create it first. If client’s limit exceeded - send an error, otherwise join chat room and send last N messages of activity. Server should support many chat rooms.
    -  /join chat_room
  -  leave: disconnect client.
        - /leave
  - users — show users in the channel.
    -  /users
 - publish: text message terminated with CR - sends message to current channel.


 ## Give it a try:
  Start server using makefile (make run), this will compile the project, creates fat jar and runs, so that, just open
  some telnet clients:

Client A

```
 telnet localhost 9999

 /login foopass

 /join roomA

 /publish hello

```

Client B

```
 telnet localhost 9999

 /login bar pass

 /join roomA

 /publish hi there?

```

 ## Stress test
  As Apache benchmark is not designed to work with websockets, and platforms like Tsung are pretty big, i decided to
  create a basic bot client using golang (just an easy script). Inside test folder you can find a binnary (bench)
  that enables load testing, being able to select desired number of socket clients, each client behaves as a bot,
  connecting to the server, joinning to an specific room (taking care of the maximum users by room too), and then
  starts to publish messages in regular intervals (tickers)

 To check it, first start server:

```
 make run

```
 And then, to start bot platform, execute from root:

```
 ./src/test/java/com/marcosquesada/netty/chat/stress/bench --total 1000

```
 In this example, we are requesting to create 1000 bot clients, all of them connects to server, joins room and start talking,
 sending messages at 200ms rate. On my current machine (macbook air, not so big), pushing ulimts up to 15000, i been
 able to run it up to the limit (15K) without errors... or it seems so :)



