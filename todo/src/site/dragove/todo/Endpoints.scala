package site.dragove.todo

import sttp.tapir.*

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import io.github.arainko.ducktape.*
import sttp.shared.Identity
import sttp.tapir.generic.auto.*
import sttp.tapir.json.jsoniter.*
import sttp.tapir.server.ServerEndpoint

object Endpoints:

  given JsonValueCodec[AddTodo] = JsonCodecMaker.make
  given JsonValueCodec[UpdateTodo] = JsonCodecMaker.make
  given JsonValueCodec[List[Todo]] = JsonCodecMaker.make
  val statusEndpoint =
    endpoint.get
      .in("status")
      .out(stringBody)
      .handleSuccess(_ => "Server is Running\n")
  val allEndpoint =
    endpoint.get
      .in("all")
      .in(query[Option[String]]("filter"))
      .out(jsonBody[List[Todo]])
      .handleSuccess(filter =>
        filter match
          case Some(x) =>
            x match
              case "all"       => TodoRepo.list(None)
              case "active"    => TodoRepo.list(Some(false))
              case "completed" => TodoRepo.list(Some(true))
          case None => TodoRepo.list(None)
      )

  val addEndpoint = endpoint.post
    .in("add")
    .in(jsonBody[AddTodo])
    .out(stringBody)
    .handleSuccess(t => {
      TodoRepo.insert(t.into[Todo].transform(Field.const(_.id, 0L)))
      "{\"success\": true}"
    })

  val updateEndpoint =
    endpoint.put
      .in("update")
      .in(jsonBody[UpdateTodo])
      .out(stringBody)
      .handleSuccess(t => {
        TodoRepo.updateCompleted(t.id, t.completed)
        "{\"success\": true}"
      })

  val toggleAllEndpoint =
    endpoint.put
      .in("toggleAll")
      .in(query[Boolean]("completed"))
      .out(stringBody)
      .handleSuccess(t => {
        TodoRepo.updateAllCompleted(t)
        "{\"success\": true}"
      })

  val deleteEndpoint =
    endpoint.delete
      .in("delete")
      .in(query[Long]("id"))
      .out(stringBody)
      .handleSuccess(t => {
        TodoRepo.deleteById(t)
        "{\"success\": true}"
      })

  val deleteCompleted =
    endpoint.delete
      .in("clear")
      .out(stringBody)
      .handleSuccess(_ => {
        TodoRepo.deleteCompleted()
        "{\"success\": true}"
      })

  val apiEndpoints: List[ServerEndpoint[Any, Identity]] =
    List(
      statusEndpoint,
      allEndpoint,
      addEndpoint,
      updateEndpoint,
      toggleAllEndpoint,
      deleteEndpoint,
      deleteCompleted
    )

  val all: List[ServerEndpoint[Any, Identity]] = apiEndpoints
