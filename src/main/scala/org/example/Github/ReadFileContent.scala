package org.example.Github

import java.nio.file.{Files, Paths}
import java.util.Base64

object ReadFileContent extends App {

  // specify "path_to_generated_dag.py" as FilePath
  def base64EncodeFileContent(filePath: String): String = {
    val fileContent = Files.readAllBytes(Paths.get(filePath))
    val base64EncodedContent = Base64.getEncoder.encodeToString(fileContent)

    base64EncodedContent
  }
}
