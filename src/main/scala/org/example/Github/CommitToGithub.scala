package org.example.Github

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader

import scala.util.{Failure, Success}
//import akka.http.scaladsl.model.{HttpMethod, HttpRequest, HttpResponse}
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.util.Timeout
import io.circe.parser._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import org.example.utils.Parsers.GithubParser.CommitInfo
import org.example.utils.JsonDecoder.commitInfoDecoder
import org.example.Github.ReadFileContent.base64EncodeFileContent

object CommitToGithub extends App {

  implicit val system: ActorSystem = ActorSystem("github-api-system")
  implicit val materializer: Materializer = Materializer(system)
  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val ec: ExecutionContext = system.dispatcher

  // Your GitHub Personal Access Token
  val githubToken = "your_token"

  // GitHub repository details
  val owner = "your_username"
  val repo = "repo_name"
  val branch = "master" // or whatever branch you want to commit to

  // File details
  val filePath = "path_to_the_dag" // The path of the file to commit
  val commitMessage = "Committing a new file: generated_dag.py"

  // I did not test getFileSHA - it is needed for object upload/delete, not for the initial commit (creation)
  def getFileSHA(filePath: String): Future[Option[String]] = {
    val uri = s"https://api.github.com/repos/$owner/$repo/contents/$filePath?ref=$branch"

    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = uri,
      headers = List(RawHeader("Authorization", s"Bearer $githubToken"))
    )

    Http().singleRequest(request).flatMap { response =>
      response.status match {
        case StatusCodes.OK => {
          response.entity.toStrict(5.seconds).flatMap { strictEntity =>
            val jsonString = strictEntity.data.utf8String
            decode[CommitInfo](jsonString) match {
              case Right(commits) => Future.successful(Some(commits.sha))
              case Left(error) => Future.failed(new Exception(s"Failed to parse JSON: ${error.getMessage}"))
            }
          }
        }
        case StatusCodes.NotFound => Future.successful(None)
        case _ => {
          response.entity.discardBytes()
          Future.failed(new Exception(s"Error getting file SHA: ${response.status}"))
        }
      }
    }
  }

  def commitFile(filePath: String, message: String): Future[String] = {
    val uri = s"https://api.github.com/repos/$owner/$repo/contents/$filePath"

    val encodedContent = base64EncodeFileContent(filePath)

    val jsonBody = s"""{
      "message": "$message",
      "branch": "$branch",
      "content": "$encodedContent"
    }"""

    val request = HttpRequest(
      method = HttpMethods.PUT,
      uri = uri,
      entity = HttpEntity(ContentTypes.`application/json`, jsonBody),
      headers = List(RawHeader("Authorization", s"Bearer $githubToken"))
    )

    Http().singleRequest(request).flatMap { response =>
      response.status match {
        case StatusCodes.OK | StatusCodes.Created => {
          response.entity.toStrict(5.seconds).flatMap { strictEntity =>
            val jsonString = strictEntity.data.utf8String
            println(jsonString)
            Future.successful(s"Successfully committed file to GitHub: $filePath")
          }
        }
        case _ => response.entity.toStrict(5.seconds).flatMap { strictEntity =>
          val jsonString = strictEntity.data.utf8String
          println(jsonString)
          Future.failed(new Exception("Failed to commit file to Github"))
        }
      }
    }
  }

  val commitFileResponse = commitFile(filePath, commitMessage)

  commitFileResponse.onComplete {
    case Success(_) =>
      println(s"Successfully committed the file")
      // Continue with your commit logic here
      system.terminate()
    case Failure(e) =>
      println(s"Error failed to commit: ${e.getMessage}")
      system.terminate()
  }
}
