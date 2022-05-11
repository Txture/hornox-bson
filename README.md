![Hornox-Bson](logo/hornoxBsonLogo.png)

Hornox is a fast, simple-stupid BSON serializer, deserializer and node extractor for the JVM.

## Features

- Full implementation of the [BSON Specification](https://bsonspec.org/) with serialization and deserialization
- Implemented in pure Kotlin, should work for all JVM languages
- DOM elements implement the [Jakarta JSON API](https://mvnrepository.com/artifact/jakarta.json/jakarta.json-api) (former `javax.json`)
- Binary format is byte-by-byte identical to the output produced by the BSON implementation in the [Java MongoDB Driver](https://www.mongodb.com/java)
- Extract individual paths from a byte array in BSON format without deserializing the whole document


## Performance

Hornox is **fast**. Its parser outperforms other popular JVM-based BSON parsers by almost 50% (or more):

<img src="https://github.com/Txture/hornox-bson/blob/main/images/benchmark.png" width="500">

The chart above shows the performance comparison between Hornox and:
- [Jackson-BSON](https://github.com/michel-kraemer/bson4jackson)
- The BSON parser contained in the official [Java MongoDB Driver](https://www.mongodb.com/java)

The dataset consists of ~3 million individual documents. The documents have a wide variety of different
node types and sizes. The collection consists of a grand total of 207MB (binary BSON data on disk). The
benchmark was repeated 10 times on a pre-warmed JVM with sufficient heap space. We have observed similar
results for smaller datasets.

Hornox owes this speed advantage primarily to one thing: its simplicity.

## Building from Source

Hornox uses a standard Gradle project structure. It requires **Java 17** as well as Kotlin **1.6**.

```sh
# build the binary
./gradlew build

# run the tests
./gradlew test
```


## Serialization

```kotlin
val doc = DocumentNode()
doc["firstName"] = TextNode("John")
doc["lastName"] = TextNode("Doe")
doc["age"] = Int32Node(42)

val byteArray = BsonSerializer.serializeBsonDocument(doc)
```

## Deserialization

```kotlin
val bytes: ByteArray = ... // e.g. load it from a file, from a HTTP response...
val document = BsonDeserializer.deserializeBsonDocument(bytes)
```

## Individual Node Extraction from BSON Byte Arrays

Hornox allows you to extract a path from the serialized (binary) BSON format.
If you're interested only in a single node from the document, this extraction
is generally **much faster** than loading the entire document DOM and then
navigating through it.

```kotlin
val byteArray: ByteArray = ... // e.g. load it from a file, from a HTTP response...
/*
  For the example, let's assume the following BSON structure:
  {
      "name": "Txture",
      "addresses": [
          {
              "country": "Austria",
              "city": "Innsbruck",
              "zipCode": "6020"
          }
      ]
  }
*/
val path = listOf("addresses", "0", "city")
val city = BsonDeserializer.extractBsonNode(byteArray, path)
// city will be a TextNode containing "Innsbruck"
```

### A note on Size Markers

The [BSON Specification](https://bsonspec.org/spec.html) specifies "size" fields in several places. The node extraction algorithm can make use of those indicators in order to significantly speed up the search when it needs to skip over document entries. However, some serializers **do not write** proper size values into those fields (in order to make the serialization process faster). You can specify with a boolean whether or not Hornox should trust the size fields in the BSON, or if it should ignore them and take the "safe route" through the binary document (at the cost of performance):

```kotlin
// by default, Hornox will not trust the size markers.
val city = BsonDeserializer.extractBsonNode(byteArray, path, trustSizeMarkers = false)
// if you are sure that your binary BSON contains valid size markers, 
// Hornox can use them for enhanced extraction performance.
val city2 = BsonDeserializer.extractBsonNode(byteArray, path, trustSizeMarkers = true)
```

If a size marker contains a **negative** value, Hornox will always ignore it. The size markers of **Binary Data Nodes** always need to be accurate, because there is no other delimiter.

When **serializing** a document, Hornox offers three options when it comes to size markers:
 - `USE_MINUS_1`: Always write -1 in all size fields, effectively invalidating them. This is the faster to write than recomputing the accurate sizes, but slower to scan through later.
 - `TRUST_DOCUMENT`: Trust the `length` field which is present in the `DocumentNode` or `ArrayNode`, and write its contents into the byte array. This is generally not recommended, as the `length` field is **not** automatically updated when the content of the document or array changes. However, it can be used to quickly serialize a document with known size fields.
 - `RECOMPUTE`: Recompute all sizes prior to serialization. This is the default. Please note that this also changes the `length` fields of the `DocumentNode`s and `ArrayNode`s as a side-effect. 
