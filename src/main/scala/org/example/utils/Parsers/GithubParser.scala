package org.example.utils.Parsers

object GithubParser {
  // Case classes that match the structure of the GitHub API response
  case class CommitInfo(sha: String, url: String)
}
