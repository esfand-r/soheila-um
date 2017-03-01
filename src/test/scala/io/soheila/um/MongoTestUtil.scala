package io.soheila.um

import de.flapdoodle.embed.mongo.config.{ MongodConfigBuilder, Net, RuntimeConfigBuilder }
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{ Command, MongodProcess, MongodStarter }
import de.flapdoodle.embed.process.runtime.Network
import org.specs2.execute.{ AsResult, Result }
import org.specs2.mutable.Around
import org.specs2.specification.core.Fragments
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test._
import play.api.{ Environment, Logger }
import play.modules.reactivemongo.{ ReactiveMongoApi, ReactiveMongoModule }
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.commands.DropDatabase
import reactivemongo.api.commands.bson.BSONDropDatabaseImplicits._
import reactivemongo.api.commands.bson.CommonImplicits._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Runs a fake application with a MongoDB database.
 */
class WithMongo(
  applicationBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder,
  config: Map[String, Any] = Map(
    "mongodb.uri" -> s"mongodb://localhost:12345/test"
  )
)
    extends WithServer(
      applicationBuilder
        .configure(config)
        .bindings(new ReactiveMongoModule)
        .overrides(new FakeModule)
        .build()
    )

/**
 * Executes before and after methods in the context of the around method.
 */
trait BeforeAfterWithinAround extends Around {
  def before: Any
  def after: Any
  abstract override def around[T: AsResult](t: => T): Result = super.around {
    try { before; t } finally { after }
  }
}

/**
 * A custom specification which starts a MongoDB instance before all the tests, and stops it after all of them.
 *
 * Note: This is handled like a global setup/teardown procedure. So you must clean the database after each test,
 * to get an isolated test case.
 */
trait MongoSpecification extends PlaySpecification with FutureAwaits with DefaultAwaitTimeout {
  sequential
  override def map(fs: => Fragments) = step(start()) ^ fs ^ step(stop())

  /**
   * Defines the port on which the embedded Mongo instance should listen.
   *
   * @return The port on which the embedded Mongo instance should listen.
   */
  def embedConnectionPort(): Int = {
    sys.env.get("EMBEDDED_MONGO_PORT") match {
      case Some(str) => str.toInt
      case _ => 12345
    }
  }

  /**
   * Defines the Mongo version to start.
   *
   * @return The Mongo version to start.
   */
  def embedMongoDBVersion(): Version.Main = { Version.Main.PRODUCTION }

  /**
   * The MongoDB executable.
   */
  lazy val mongodExecutable = MongodStarter
    .getInstance(new RuntimeConfigBuilder()
      .defaultsWithLogger(Command.MongoD, Logger(this.getClass).logger)
      .build())
    .prepare(new MongodConfigBuilder()
      .version(embedMongoDBVersion())
      .net(new Net(embedConnectionPort(), Network.localhostIsIPv6))
      .build)

  /**
   * The mongod process.
   */
  var process: Option[MongodProcess] = None

  /**
   * Starts the MongoDB instance.
   */
  private def start(): Unit = {
    process = Some(mongodExecutable.start)
  }

  /**
   * Stops the MongoDB instance.
   */
  private def stop(): Unit = {
    process.foreach(_.stop)
    mongodExecutable.stop()
  }
}

/**
 * The MongoDB scope.
 */
trait MongoScope extends BeforeAfterWithinAround {
  self: WithServer =>

  /**
   * Some test fixtures to insert into the database.
   */
  val fixtures: Map[String, Seq[String]] = Map()

  /**
   * The ReactiveMongo API.
   */
  lazy val reactiveMongoAPI = app.injector.instanceOf[ReactiveMongoApi]

  /**
   * The application environment.
   */
  implicit val env = app.injector.instanceOf[Environment]

  def before: Unit = {
    // load fixtures?
  }

  /**
   * Drops the database after the test runs to get an isolated environment.
   */
  def after: Unit = {
    Await.result(reactiveMongoAPI.database.flatMap { db =>
      db.runCommand(DropDatabase, FailoverStrategy(10.seconds, 20))
    }, Duration(160, SECONDS))
  }
}
