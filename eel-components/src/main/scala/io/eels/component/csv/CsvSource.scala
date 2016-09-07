package io.eels.component.csv

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import io.eels.schema.Schema
import io.eels.{Part, SchemaInferrer, Source, StringInferrer}
import java.nio.file.Path

import com.sksamuel.exts.io.Using

case class CsvSource(path: Path,
                     overrideSchema: Option[Schema] = None,
                     format: CsvFormat = CsvFormat(),
                     inferrer: SchemaInferrer = StringInferrer,
                     ignoreLeadingWhitespaces: Boolean = true,
                     ignoreTrailingWhitespaces: Boolean = true,
                     skipEmptyLines: Boolean = true,
                     emptyCellValue: String = null,
                     nullValue: String = null,
                     verifyRows: Option[Boolean] = None,
                     header: Header = Header.FirstRow) extends Source with Using {

  val config: Config = ConfigFactory.load()
  val defaultVerifyRows = verifyRows.getOrElse(config.getBoolean("eel.csv.verifyRows"))

  private def createParser(): CsvParser = {
    val settings = new CsvParserSettings()
    settings.getFormat.setDelimiter(format.delimiter)
    settings.getFormat.setQuote(format.quoteChar)
    settings.getFormat.setQuoteEscape(format.quoteEscape)
    settings.setLineSeparatorDetectionEnabled(true)
    // this is always true as we will fetch the headers ourselves by reading first row
    settings.setHeaderExtractionEnabled(false)
    settings.setIgnoreLeadingWhitespaces(ignoreLeadingWhitespaces)
    settings.setIgnoreTrailingWhitespaces(ignoreTrailingWhitespaces)
    settings.setSkipEmptyLines(skipEmptyLines)
    settings.setCommentCollectionEnabled(true)
    settings.setEmptyValue(emptyCellValue)
    settings.setNullValue(nullValue)
    new com.univocity.parsers.csv.CsvParser(settings)
  }

  def withSchemaInferrer(inferrer: SchemaInferrer): CsvSource = copy(inferrer = inferrer)

  // sets whether this source has a header and if so where to read from
  def withHeader(header: Header): CsvSource = copy(header = header)

  def withSchema(schema: Schema): CsvSource = copy(overrideSchema = Some(schema))
  def withDelimiter(c: Char): CsvSource = copy(format = format.copy(delimiter = c))
  def withQuoteChar(c: Char): CsvSource = copy(format = format.copy(quoteChar = c))
  def withQuoteEscape(c: Char): CsvSource = copy(format = format.copy(quoteEscape = c))
  def withFormat(format: CsvFormat): CsvSource = copy(format = format)
  def withVerifyRows(verifyRows: Boolean): CsvSource = copy(verifyRows = Some(verifyRows))

  // use this value when the cell/record is empty quotes in the source data
  def withEmptyCellValue(emptyCellValue: String): CsvSource = copy(emptyCellValue = emptyCellValue)

  // use this value when the cell/record is empty in the source data
  def withNullValue(nullValue: String): CsvSource = copy(nullValue = nullValue)

  def withSkipEmptyLines(skipEmptyLines: Boolean): CsvSource = copy(skipEmptyLines = skipEmptyLines)
  def withIgnoreLeadingWhitespaces(ignore: Boolean): CsvSource = copy(ignoreLeadingWhitespaces = ignore)
  def withIgnoreTrailingWhitespaces(ignore: Boolean): CsvSource = copy(ignoreTrailingWhitespaces = ignore)

  override def schema(): Schema = overrideSchema.getOrElse {
    val parser = createParser()
    parser.beginParsing(path.toFile())
    val headers = header match {
      case Header.None =>
        // read the first row just to get the count of columns, then we'll call them column 1,2,3,4 etc
        // todo change the column labels to a,b,c,d
        val records = parser.parseNext()
        (0 until records.size).map(_.toString).toList
      case Header.FirstComment =>
        while (parser.getContext.lastComment() == null && parser.parseNext() != null) {
        }
        val str = Option(parser.getContext.lastComment).getOrElse("")
        str.split(format.delimiter).toList
      case Header.FirstRow => parser.parseNext().toList
    }
    parser.stopParsing()
    inferrer.schemaOf(headers)
  }

  override def parts(): List[Part] = {
    val verify = header match {
      case Header.None => false
      case _ => verifyRows.getOrElse(defaultVerifyRows)
    }
    val part = new CsvPart(createParser, path, header, verify, schema())
    List(part)
  }
}