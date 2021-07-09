package org.fairy.next.test

import org.fairy.next.extension.curTime
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.Throughput) // 基准测试的模式，采用整体吞吐量的模式
@Warmup(iterations = 3) // 预热次数
@Measurement(iterations = 10, time = 5, timeUnit = TimeUnit.SECONDS) // 测试参数，iterations = 10 表示进行10轮测试
@Threads(8) // 每个进程中的测试线程数
@Fork(2)  // 进行 fork 的次数，表示 JMH 会 fork 出两个进程来进行测试
@OutputTimeUnit(TimeUnit.MILLISECONDS) // 基准测试结果的时间类型
@State(Scope.Benchmark)
open class LookupBenchmark {

    val mapNormal : MutableMap<Long, Any> = ConcurrentHashMap(10000000)
    val map : MutableMap<Long, Any> = ConcurrentHashMap((10000000 * 0.8).toInt())
    val mapVoid : MutableMap<Long, Any> = ConcurrentHashMap((10000000 * 0.2).toInt())

    init {
        println("hi")
        var i = 0L
        repeat((1000000 * 0.8).toInt()) {
            map[i++] = Any()
        }

        repeat((1000000 * 0.2).toInt()) {
            mapVoid[i++] = Any()
        }

        repeat(1000000) {
            mapNormal[it.toLong()] = Any()
        }
        println("aaa")
    }

    @Benchmark
    fun testNormal() {
        val any = mapNormal[ThreadLocalRandom.current().nextLong(1000000L)]
    }

    @Benchmark
    fun testVoidMap() {
        val target = ThreadLocalRandom.current().nextLong(1000000L)
        val any = map[target] ?: mapVoid[target]
    }

}

fun main() {
    val options = OptionsBuilder()
        .include(LookupBenchmark::class.java.simpleName)
        .resultFormat(ResultFormatType.JSON)
        .result("benchmark_sequence.json")
        .output("benchmark_sequence.log")
        .build()
    Runner(options).run()
}

