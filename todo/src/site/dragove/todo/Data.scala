package site.dragove.todo

case class AddTodo(title: String, completed: Boolean)
case class UpdateTodo(id: Long, completed: Boolean)


