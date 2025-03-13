package org.example.utils

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.example.utils.Parsers.GithubParser.CommitInfo

object JsonDecoder {
  implicit val commitInfoDecoder: Decoder[CommitInfo] = deriveDecoder[CommitInfo]
}