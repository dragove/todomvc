package site.dragove.todo

import sttp.tapir.*

import sttp.shared.Identity
import sttp.tapir.generic.auto.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.EndpointIO.Body

object Controller:
  import scala.collection.mutable.ArrayBuffer
  private val apiEndpoints: ArrayBuffer[ServerEndpoint[Any, Identity]] =
    ArrayBuffer()
  def all =
    Endpoints.init
    apiEndpoints.toList
  def controller(s: ServerEndpoint[Any, Identity]) =
    apiEndpoints.append(s)
  def base(url: String) = endpoint.in(url)
  def get(url: String) = endpoint.get.in(url)
  def post(url: String) = endpoint.post.in(url)
  def put(url: String) = endpoint.put.in(url)
  def delete(url: String) = endpoint.delete.in(url)

object Endpoints:
  import Controller.*
  import sttp.tapir.json.jsoniter.*
  val successJson = "{\"success\": true}"

  def init =
    controller:
      get("status")
        .out(stringBody)
        .handleSuccess(_ => "Hello World")

    controller:
      get("all")
        .in(query[Option[String]]("filter"))
        .out(jsonBody[List[Todo]])
        .handleSuccess:
          case Some(x) =>
            x match
              case "all"       => TodoRepo.list(None)
              case "active"    => TodoRepo.list(Some(false))
              case "completed" => TodoRepo.list(Some(true))
          case None => TodoRepo.list(None)

    controller:
      post("add")
        .in(jsonBody[AddTodo])
        .out(stringJsonBody)
        .handleSuccess: t =>
          TodoRepo.insert(Todo(0L, t.title, t.completed))
          successJson

    controller:
      put("update")
        .in(jsonBody[UpdateTodo])
        .out(stringJsonBody)
        .handleSuccess: t =>
          TodoRepo.updateCompleted(t.id, t.completed)
          successJson

    controller:
      put("toggleAll")
        .in(query[Boolean]("completed"))
        .out(stringJsonBody)
        .handleSuccess: t =>
          TodoRepo.updateAllCompleted(t)
          successJson

    controller:
      delete("delete")
        .in(query[Long]("id"))
        .out(stringJsonBody)
        .handleSuccess: t =>
          TodoRepo.deleteById(t)
          successJson

    controller:
      delete("clear")
        .out(stringJsonBody)
        .handleSuccess: _ =>
          TodoRepo.deleteCompleted()
          successJson
