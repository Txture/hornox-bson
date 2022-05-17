package io.txture.hornoxbson.benchmarks

import org.chronos.ng.chronodocs.impl.json.AsciiTable
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

object BenchmarkUtils {

    fun benchmark(name: String, warmups: Int = 10, repeats: Int = 10, function: () -> Double): Stats {
        println("WARMUP [${name}]")
        repeat(warmups) {
            println(".")
            function()
        }
        println("MEASURE [${name}]")
        val results = mutableListOf<Double>()
        val measurements = mutableListOf<Long>()
        repeat(repeats) {
            println(".")
            measureTimeMillis {
                results += function()
            }.let { measurements += it }
        }
        val result = results.sum() / repeats
        return Stats(
            name = name,
            runs = repeats,
            min = measurements.minOrNull()!!,
            max = measurements.maxOrNull()!!,
            avg = measurements.average(),
            stdev = measurements.stdev(),
            result = result
        )
    }

    fun List<Long>.stdev(): Double {
        val sum = this.sum()
        val mean = sum.toDouble() / this.size
        val standardDeviation = this.sumOf { (it - mean).pow(2.0) }
        return sqrt(standardDeviation / this.size)
    }

    fun renderTableToConsole(title: String, stats: List<Stats>) {
        val table = AsciiTable("|l|r|r|r|r|r|r|")
        table.addRow("Name", "Runs", "Min (ms)", "Max (ms)", "Avg (ms)", "StDev (ms)", "Output Bytes")
        table.addRule('-')
        for (stat in stats) {
            table.addRow(
                stat.name,
                stat.runs,
                stat.min,
                stat.max,
                stat.avg,
                String.format("%.3f", stat.stdev),
                stat.result
            )
        }
        table.addRule('-')

        println()
        println("BENCHMARK: ${title}")
        println(table.render())
        println()
    }

    class Stats(
        val name: String,
        val runs: Int,
        val min: Long,
        val max: Long,
        val avg: Double,
        val stdev: Double,
        val result: Double,
    )

}