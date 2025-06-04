package site.dragove.todo

import com.github.plokhotnyuk.jsoniter_scala.macros.*
case class AddTodo(title: String, completed: Boolean) derives ConfiguredJsonValueCodec
case class UpdateTodo(id: Long, completed: Boolean) derives ConfiguredJsonValueCodec
