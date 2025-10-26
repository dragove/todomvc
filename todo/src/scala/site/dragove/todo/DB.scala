package site.dragove.todo

import javax.sql.DataSource
import sqala.static.metadata.autoInc
case class Todo(
    @autoInc
    id: Long,
    title: String,
    completed: Boolean
)
object DB:
  import org.h2.jdbcx.JdbcDataSource
  import sqala.jdbc.*
  import sqala.dynamic.dsl.sql
  import sqala.printer.H2Dialect
  given JdbcConnection[DataSource] with
    def init(url: String, username: String, password: String, driverClassName: String): JdbcDataSource =
      scribe.info("database start initialization")
      val ds = JdbcDataSource()
      ds.setURL(url)
      ds.setUser(username)
      ds.setPassword(password)
      scribe.info("database initialization completed")
      ds
  val db = JdbcContext(H2Dialect, true, "jdbc:h2:mem:todo;DB_CLOSE_DELAY=-1", "sa", "", "")
  given logger: Logger = Logger(it => scribe.info(it))
  db.execute(sql"""
    CREATE TABLE IF NOT EXISTS "todo" (
        "id" BIGINT PRIMARY KEY AUTO_INCREMENT,
        "title" TEXT,
        "completed" BOOLEAN
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
    db.execute(query:
      delete[Todo].where(_.id == id))

  def deleteCompleted() =
    db.execute(query:
      delete[Todo].where(_.completed))

  def list(completed: Option[Boolean]) =
    db.fetch(query:
      from(Todo).filterIf(completed.isDefined)(_.completed == completed))
