package site.dragove.todo

import sqala.metadata.autoInc
case class Todo(
    @autoInc
    id: Long,
    title: String,
    completed: Boolean
)
object DB:
  import org.sqlite.SQLiteDataSource
  import org.sqlite.SQLiteConfig
  import sqala.jdbc.*
  import sqala.printer.SqliteDialect
  given logger: Logger = Logger(it => scribe.debug(it))
  private val config = SQLiteConfig()
  private val ds = SQLiteDataSource(config)
  ds.setUrl("jdbc:sqlite:todo.db")
  private val con = ds.getConnection()
  con
    .createStatement()
    .executeUpdate("""
    CREATE TABLE IF NOT EXISTS `todo` (
        id INTEGER PRIMARY KEY,
        title TEXT,
        completed INTEGER
    );
    """)
  val db = JdbcContext(ds, SqliteDialect)

object TodoRepo:
  import DB.db
  import sqala.static.dsl.*
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
      from[Todo].filterIf(completed.isDefined)(_.completed == completed)
