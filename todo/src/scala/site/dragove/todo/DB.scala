package site.dragove.todo

import sqala.metadata.autoInc
import javax.sql.DataSource
case class Todo(
    @autoInc
    id: Long,
    title: String,
    completed: Boolean
)
object DB:
  import org.sqlite.{SQLiteConfig, SQLiteDataSource}
  import sqala.jdbc.*
  import sqala.printer.SqliteDialect
  import sqala.dynamic.dsl.sql
  given JdbcConnection[DataSource] with
    def init(url: String, username: String, password: String, driverClassName: String): DataSource = 
      val config = SQLiteConfig()
      val ds = SQLiteDataSource(config)
      ds.setUrl("jdbc:sqlite:todo.db")
      ds
  val db =
    JdbcContext(SqliteDialect, "jdbc:sqlite:todo.db", "", "", "jdbc:sqlite:todo.db")
  given logger: Logger = Logger(it => scribe.debug(it))
  db.execute(sql"""
    CREATE TABLE IF NOT EXISTS `todo` (
        id INTEGER PRIMARY KEY,
        title TEXT,
        completed INTEGER
    )""")

object TodoRepo:
  import DB.db
  import sqala.jdbc.Logger
  import sqala.static.dsl.*

  given logger: Logger = Logger(it => scribe.debug(it))
  def insert(todo: Todo) =
    db.insert(todo)

  def save(todo: Todo) =
    db.save(todo)

  def updateCompleted(id: Long, completed: Boolean) =
    db.execute:
      update[Todo]
        .set(_.completed := completed)
        .where(_.id == id)

  def updateAllCompleted(completed: Boolean) =
    db.execute:
      update[Todo]
        .set(_.completed := completed)

  def deleteById(id: Long) =
    db.execute:
      delete[Todo].where(_.id == id)

  def deleteCompleted() =
    db.execute:
      delete[Todo].where(_.completed)

  def list(completed: Option[Boolean]) =
    db.fetch:
      query:
        from[Todo].filterIf(completed.isDefined)(_.completed == completed)

