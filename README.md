# A TODO-MVC with Server

## Tech Stacks

Server
- Scala 3 (Language)
- Tapir with Netty and ox (Server and Http endpoint, with virtual thread)
- Sqala (SQL Builder)
- Scribe (Logging)
- jsoniter (JSON serializer/deserializer)
- Ducktape (Data Type Transformation)
- Sqlite (Database)

Client is implemented with Solidjs


## Run it locally

on the root directory run
```bash
./mill todo.run
# if you are on windows
./mill.bat todo.run
```

change directory to web and run
```bash
pnpm run dev
```


