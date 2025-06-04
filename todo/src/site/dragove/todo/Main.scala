package site.dragove.todo

import ox.*
import sttp.tapir.server.netty.sync.NettySyncServer
import sttp.tapir.server.netty.sync.NettySyncServerOptions
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.interceptor.cors.CORSConfig
import sttp.model.headers.Origin
import sttp.model.Method

object Main extends OxApp.Simple:
  def run(using Ox): Unit =
    val port = sys.env.get("HTTP_PORT").flatMap(_.toIntOption).getOrElse(8080)
    val corsInterceptor = NettySyncServerOptions.customiseInterceptors
      .corsInterceptor(
        CORSInterceptor.customOrThrow(
          CORSConfig.default
            .allowOrigin(Origin.Host("http", "127.0.0.1", Some(3000)))
            .allowOrigin(Origin.Host("http", "localhost", Some(3000)))
            .allowAllMethods
        )
      )
      .options
    val binding = useInScope(
      NettySyncServer()
        .options(corsInterceptor)
        .port(port)
        .addEndpoints(Controller.all.toList)
        .start()
    )(_.stop())
    println(s"Server started at http://localhost:${binding.port}. ")
    never
