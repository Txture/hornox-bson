package io.txture.hornoxbson.benchmarks

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.kotlinModule
import de.undercouch.bson4jackson.BsonFactory
import io.txture.hornoxbson.BsonDeserializer
import io.txture.hornoxbson.BsonSerializer
import io.txture.hornoxbson.benchmarks.BenchmarkUtils.Stats
import io.txture.hornoxbson.benchmarks.BenchmarkUtils.benchmark
import io.txture.hornoxbson.benchmarks.BenchmarkUtils.renderTableToConsole
import io.txture.hornoxbson.model.DocumentNode
import io.txture.hornoxbson.model.Int32Node
import io.txture.hornoxbson.model.TextNode
import org.bson.*
import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.io.BasicOutputBuffer
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import kotlin.random.Random


@Tag("Benchmark")
class PersonsBenchmark {

    companion object {

        val numberOfPersons = 1_000_000

    }

    // =================================================================================================================
    // BENCHMARKS
    // =================================================================================================================

    @Test
    fun runPersonsBenchmark() {
        // write
        val writeStats = mutableListOf<Stats>()
        writeStats += this.benchmarkMongoWrite()
        writeStats += this.benchmarkJacksonBsonWrite()
        writeStats += this.benchmarkHornoxBsonWrite()
        writeStats += this.benchmarkJacksonJsonWrite()

        renderTableToConsole("Serialize ${numberOfPersons} Persons", writeStats)

        val readStats = mutableListOf<Stats>()
        readStats += this.benchmarkMongoRead()
        readStats += this.benchmarkJacksonBsonRead()
        readStats += this.benchmarkHornoxBsonRead()
        readStats += this.benchmarkJacksonJsonRead()

        renderTableToConsole("Deserialize ${numberOfPersons} Persons", readStats)
    }

    // =================================================================================================================
    // BENCHMARK PARTICIPANTS
    // =================================================================================================================

    private fun benchmarkMongoWrite(): Stats {
        val persons = (0..numberOfPersons).map { generateMongoPerson(it) }.toList()

        val codec = BsonDocumentCodec()
        val encoderContext = EncoderContext.builder().isEncodingCollectibleDocument(true).build()

        return benchmark("MongoDB BSON Write") {
            var totalBytes = 0L
            for (person in persons) {
                val bytes = writeMongoDocument(codec, encoderContext, person)
                totalBytes += bytes.size
            }
            totalBytes.toDouble()
        }
    }

    private fun benchmarkMongoRead(): Stats {
        val persons = (0..numberOfPersons).map { generateMongoPerson(it) }.toList()

        val codec = BsonDocumentCodec()
        val encoderContext = EncoderContext.builder().build()

        val encodedData = persons.map { this.writeMongoDocument(codec, encoderContext, it) }.toList()

        val decoderContext = DecoderContext.builder().build()

        return benchmark("MongoDB BSON Read") {
            var totalSize = 0L
            for (data in encodedData) {
                val deserialized = readMongoDocument(codec, decoderContext, data)
                totalSize += deserialized.size
            }
            totalSize.toDouble()
        }
    }

    private fun benchmarkJacksonBsonWrite(): Stats {
        val persons = (0..numberOfPersons).map { generateJacksonPerson(it) }.toList()

        val bsonObjectMapper = ObjectMapper(BsonFactory()).registerModule(kotlinModule())

        return benchmark("Jackson BSON Write") {
            var totalBytes = 0L
            for (person in persons) {
                val bytes = writeJacksonDocument(bsonObjectMapper, person)
                totalBytes += bytes.size
            }
            totalBytes.toDouble()
        }
    }

    private fun benchmarkJacksonBsonRead(): Stats {
        val persons = (0..numberOfPersons).map { generateJacksonPerson(it) }.toList()

        val bsonObjectMapper = ObjectMapper(BsonFactory()).registerModule(kotlinModule())

        val encodedData = persons.map { writeJacksonDocument(bsonObjectMapper, it) }

        return benchmark("Jackson BSON Read") {
            var totalSize = 0L
            for (encoded in encodedData) {
                val deserialized = readJacksonDocument(bsonObjectMapper, encoded)
                totalSize += deserialized.size()
            }
            totalSize.toDouble()
        }
    }

    private fun benchmarkHornoxBsonRead(): Stats {
        val persons = (0..numberOfPersons).map { generateJacksonPerson(it) }.toList()

        val bsonObjectMapper = ObjectMapper(BsonFactory()).registerModule(kotlinModule())

        val encodedData = persons.map { writeJacksonDocument(bsonObjectMapper, it) }

        return benchmark("Hornox BSON Read") {
            var totalSize = 0L
            for (encoded in encodedData) {
                val deserialized = BsonDeserializer.deserializeBsonDocument(encoded)
                totalSize += deserialized.fields.size
            }
            totalSize.toDouble()
        }
    }

    private fun benchmarkJacksonJsonWrite(): Stats {
        val persons = (0..numberOfPersons).map { generateJacksonPerson(it) }.toList()

        val bsonObjectMapper = ObjectMapper().registerModule(kotlinModule())

        return benchmark("Jackson JSON Write") {
            var totalBytes = 0L
            for (person in persons) {
                val bytes = writeJacksonDocument(bsonObjectMapper, person)
                totalBytes += bytes.size
            }
            totalBytes.toDouble()
        }
    }

    private fun benchmarkHornoxBsonWrite(): Stats {
        val persons = (0..numberOfPersons).map { generateHornoxPerson(it) }.toList()

        return benchmark("Hornox BSON Write") {
            var totalBytes = 0L
            for (person in persons) {
                val bytes = BsonSerializer.serializeBsonDocument(person)
                totalBytes += bytes.size
            }
            totalBytes.toDouble()
        }
    }

    private fun benchmarkJacksonJsonRead(): Stats {
        val persons = (0..numberOfPersons).map { generateJacksonPerson(it) }.toList()

        val bsonObjectMapper = ObjectMapper().registerModule(kotlinModule())

        val encodedData = persons.map { writeJacksonDocument(bsonObjectMapper, it) }

        return benchmark("Jackson JSON Read") {
            var totalSize = 0L
            for (encoded in encodedData) {
                val deserialized = readJacksonDocument(bsonObjectMapper, encoded)
                totalSize += deserialized.size()
            }
            totalSize.toDouble()
        }
    }

    // =================================================================================================================
    // DATA GENERATORS
    // =================================================================================================================

    private fun generateMongoPerson(index: Int): BsonDocument {
        val doc = BsonDocument()
        doc["firstName"] = BsonString("John")
        doc["lastName"] = BsonString("Doe${index}")
        doc["address"] = BsonDocument().also {
            it["City"] = BsonString("Entenhausen")
            it["Street"] = BsonString("Rosenstrasse")
            it["Number"] = BsonInt32(Random.nextInt(1, 200))
        }
        doc["numbers"] = BsonArray().also { array ->
            repeat(10) {
                array.add(BsonInt32(Random.nextInt(1, 100)))
            }
        }
        return doc
    }


    private fun generateJacksonPerson(index: Int): ObjectNode {
        val jnf = JsonNodeFactory.instance
        val doc = jnf.objectNode()
        doc.put("firstName", "John")
        doc.put("lastName", "Doe${index}")
        doc.set<ObjectNode>("address", jnf.objectNode().also {
            it.put("City", "Entenhausen")
            it.put("Street", "Rosenstrasse")
            it.put("Number", Random.nextInt(1, 200))
        })
        doc.set<ArrayNode>("numbers", jnf.arrayNode().also { array ->
            repeat(10) {
                array.add(Random.nextInt(1, 100))
            }
        })
        return doc
    }

    private fun generateHornoxPerson(index: Int): DocumentNode {
        return DocumentNode(-1, mutableMapOf(
            "firstName" to TextNode("John"),
            "lastName" to TextNode("Doe${index}"),
            "address" to DocumentNode(-1, mutableMapOf(
                "City" to TextNode("Entenhausen"),
                "Street" to TextNode("Rosenstrasse"),
                "Number" to Int32Node(Random.nextInt(1, 200))
            )),
            "numbers" to io.txture.hornoxbson.model.ArrayNode(-1, (0..9).asSequence().map { Int32Node(Random.nextInt(1, 100)) }.toMutableList())
        ))
    }

    // =================================================================================================================
    // HELPER METHODS
    // =================================================================================================================

    private fun writeMongoDocument(codec: BsonDocumentCodec, encoderContext: EncoderContext, person: BsonDocument): ByteArray {
        BasicOutputBuffer().use { buffer ->
            BsonBinaryWriter(buffer).use { writer ->
                codec.encode(writer, person, encoderContext)
            }
            return buffer.toByteArray()
        }
    }

    private fun readMongoDocument(codec: BsonDocumentCodec, decoderContext: DecoderContext, data: ByteArray): BsonDocument {
        BsonBinaryReader(ByteBuffer.wrap(data)).use { reader ->
            return codec.decode(reader, decoderContext)
        }
    }

    private fun writeJacksonDocument(objectMapper: ObjectMapper, person: ObjectNode): ByteArray {
        return objectMapper.writeValueAsBytes(person)
    }

    private fun readJacksonDocument(objectMapper: ObjectMapper, data: ByteArray): ObjectNode {
        return objectMapper.readTree(data) as ObjectNode
    }


}