package site.dragove.todo
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

case class AddTodo(title: String, completed: Boolean)
object AddTodo:
  given JsonValueCodec[AddTodo] = JsonCodecMaker.make
case class UpdateTodo(id: Long, completed: Boolean)
object UpdateTodo:
  given JsonValueCodec[UpdateTodo] = JsonCodecMaker.make


