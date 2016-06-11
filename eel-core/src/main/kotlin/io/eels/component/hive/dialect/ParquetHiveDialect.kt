package io.eels.component.hive.dialect

import io.eels.Row
import io.eels.component.Predicate
import io.eels.component.hive.HiveDialect
import io.eels.component.hive.HiveWriter
import io.eels.schema.Schema
import io.eels.util.Logging
import io.eels.util.Option
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import rx.Observable

object ParquetHiveDialect : HiveDialect, Logging {

  override fun reader(path: Path, dataSchema: Schema, projection: Schema, predicate: Option<Predicate>, fs: FileSystem): Observable<Row> {
    throw UnsupportedOperationException()

//    override def reader(path: Path, dataSchema: Schema, targetSchema: Schema, predicate: Option[Predicate])
//    (implicit fs: FileSystem): SourceReader = new SourceReader {
//      require(targetSchema.columns.nonEmpty, "Cannot create parquet reader for a target schema that has no columns")
//      val reader = ParquetReaderSupport.createReader(path, targetSchema.size < dataSchema.size, predicate, targetSchema)
//      override def close(): Unit = reader.close()
//      override def iterator: Iterator[InternalRow] = ParquetIterator(reader, targetSchema)
//    }
  }

  override fun writer(schema: Schema, path: Path, fs: FileSystem): HiveWriter = ParquetWriter()

}


class ParquetWriter : HiveWriter {

  override fun write(row: Row) {
    throw UnsupportedOperationException()
  }

  override fun close() {
    throw UnsupportedOperationException()
  }

//  // hive is case insensitive so we must lower case everything to keep it consistent
//  lazy val avroSchema = AvroSchemaFn.toAvro(schema, caseSensitive = false)
//  lazy val writer = RollingParquetWriter(path, avroSchema)
//  lazy val marshaller = new ConvertingAvroRecordMarshaller(avroSchema)
//  var count = 0l
//
//}
//
//  override fun writer(schema: Schema, path: Path)
//  (implicit fs: FileSystem): HiveWriter = {
//    ParquetLogMute()
//
//
//
//    new HiveWriter {
//      override def close(): Unit = if (count > 0) writer.close()
//      override def write(row: InternalRow): Unit = {
//      val record = marshaller.toRecord(row)
//      writer.write(record)
//      count = count + 1
//    }
//    }
//  }
}