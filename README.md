# A TODO-MVC with Server

## Tech Stacks

Server
- Scala 3 (Language)
- [Tapir with Netty and ox](https://tapir.softwaremill.com/en/latest/generate.html) (Server and Http endpoint, with virtual thread)
- [Sqala](https://wz7982.github.io/sqala-doc/) (SQL Builder, Json and Data Mapping)
- [Scribe](https://github.com/outr/scribe) (Logging)
- [Sqlite](https://github.com/xerial/sqlite-jdbc) (Database)

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
pnpm i
pnpm run dev
```

