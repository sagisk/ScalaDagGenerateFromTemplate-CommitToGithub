package org.example.Airflow

import cats.effect.{IO, Resource}
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.FileResourceLoader

import java.io.{File, FileWriter, Writer}
import java.util.Properties

// NOTE: copy the real template into dag-template/template.vm
object Templating extends App {

  /**
   * A common pattern is to acquire a resource (eg a file or a socket), perform
   * some action on it and then run a finalizer (e.g., closing the file handle),
   * regardless of the outcome of the action.
   *  - simplest way to construct a Resource is with `Resource#make`
   *  - simplest way to consume a resource is with `Resource#use`
   * */

  private def initializeVelocityEngine(): VelocityEngine = {
    val engine = new VelocityEngine()
    val properties = new Properties()

    // Configure the resource loader
    properties.setProperty(RuntimeConstants.RESOURCE_LOADER, "file")
    properties.setProperty("file.resource.loader.class", classOf[FileResourceLoader].getName())
    properties.setProperty("file.resource.loader.path", "")  // Empty string means use absolute paths

    engine.init(properties)
    engine
  }

  private def getFileWriterResources(dirPath: String, fileName: String): Resource[IO, Writer] = Resource.make {
    val path = s"${dirPath}/${fileName}"
    // wrap in IO to handle side effects in a functional way
    IO.pure(new FileWriter(new File(path)))
  } {
    res => IO {
      res.flush()
      res.close()
    }
  }

  def uploadToPath(templatePath: String, dirPath: String, fileName: String, datasetId: String): IO[Unit] = {
    // Initialize the Velocity engine
    val velocityEngine = initializeVelocityEngine()

    getFileWriterResources(dirPath, fileName).use { writer =>
      IO.blocking {
        val context = new VelocityContext()
        context.put("DATASET_ID", datasetId)

        val template = velocityEngine.getTemplate(templatePath, "UTF-8")
        template.merge(context, writer)
      }
    }
  }

  // Main execution
  val templatePath = "path_to_the_template"
  val dirPath = "path_to_directory_where you want to save the data"
  val fileName = "generated_dag.py"
  val datasetId = "DATASET_ID_PLACEHOLDER"

  // For App trait, we need to actually run the IO
  import cats.effect.unsafe.implicits.global
  uploadToPath(templatePath, dirPath, fileName, datasetId).unsafeRunSync()
}