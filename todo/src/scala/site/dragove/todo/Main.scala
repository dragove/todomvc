package site.dragove.todo

import ox.*
import sttp.model.headers.Origin
import sttp.tapir.server.interceptor.cors.{CORSConfig, CORSInterceptor}
import sttp.tapir.server.netty.sync.{NettySyncServer, NettySyncServerOptions}
import scribe.format.Formatter

object Main extends OxApp.Simple:
  def run(using Ox): Unit =
    // config scribe logging to log in single line
    scribe.Logger.root
      .clearHandlers()
      .withHandler(formatter = Formatter.compact)
      .replace()
    val port = 8080
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

    supervised {
      val binding = useInScope(
        NettySyncServer()
          .options(corsInterceptor)
          .port(port)
          .addEndpoints(Controller.all.toList)
          .start()
      )(_.stop())
      println(s"Server started at http://localhost:${binding.port}. ")
      never
    }
