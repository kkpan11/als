package org.mulesoft.als.suggestions.test

import amf.core.remote.Hint
import amf.core.unsafe.PlatformSecrets
import common.diff.FileAssertionTest
import org.mulesoft.als.suggestions.client.Suggestion
import org.mulesoft.als.suggestions.test.SuggestionNode._
import org.mulesoft.common.io.{Fs, SyncFile}
import org.scalatest.{Assertion, AsyncFreeSpec, AsyncFunSuite}
import upickle.default.write

import scala.concurrent.{ExecutionContext, Future}

trait SuggestionByDirectoryTest extends AsyncFreeSpec with BaseSuggestionsForTest with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  def basePath: String

  def dir: SyncFile = Fs.syncFile(basePath)

  def origin: Hint

  def fileExtension: String

  s"Suggestions test for vendor ${origin.vendor.toString} by directory" - {
    forDirectory(dir)
  }

  private def forDirectory(dir: SyncFile): Unit = {
    val (subDirs, files) =
      dir.list.filter(_ != "expected").map(l => Fs.syncFile(dir.path + "/" + l)).partition(_.isDirectory)
    val validFiles = files.filter(f => f.name.endsWith(fileExtension) || f.name.endsWith(fileExtension + ".ignore"))
    if (subDirs.nonEmpty || validFiles.nonEmpty) {
      s"in directory: ${dir.name}" - {
        subDirs.foreach(forDirectory)
        validFiles.foreach { f =>
          if (f.name.endsWith(".ignore")) s"Golden: ${f.name}" ignore {
            Future.successful(succeed)
          } else {
            s"Suggest over ${f.name}" in {
              testSuggestion(f)
            }
          }
        }
      }
    }
  }

  def writeDataToString(data: List[Suggestion]): String =
    write[List[SuggestionNode]](data.map(SuggestionNode.sharedToTransport), 2)

  private def testSuggestion(f: SyncFile): Future[Assertion] = {

    val expected = f.parent + "/expected/" + f.name + ".json"
    for {
      s   <- suggest("file://" + f.path, origin.vendor.toString, None)
      tmp <- writeTemporaryFile(expected)(writeDataToString(s.toList))
      r   <- assertDifferences(tmp, expected)
    } yield r
  }
}