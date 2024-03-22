package com.homework

import com.homework.exceptions.EmptyStackException
import stacks.Stack
import stacks.TreiberStack
import stacks.eliminationStack.EliminationStack
import java.lang.Thread.sleep
import kotlin.random.Random

const val THREADS = 12
const val OPS_PER_THREAD = 1_000_000

enum class StackType { ELIMINATION, TREIBER }

class StackOps(private val stack: Stack<Int>, var ops: Array<Array<Int?>>) {
    fun execute(values: Array<Int?>) {
        for (n in values) {
            if (n != null) {
                this.stack.push(n)
            } else {
                this.stack.pop()
            }
        }
    }

    fun close() {
        try {
            while (true) {
                stack.pop()
            }
        } catch (e: EmptyStackException) {
            return
        }
    }

    inner class ExecuteThread : Thread() {
        override fun run() {
            val threadId = currentThread().name.split("-").last().toInt()
            execute(ops[threadId % THREADS])
        }

        init {
            this.start()
        }
    }
}


fun alternatingFilling(i: Int): Array<Int?> {
    val random = Random(System.currentTimeMillis())
    val f = fun(n: Int) = when (i) {
        0 -> n % 2 != 0
        1 -> n % 3 != 0
        2 -> n % 4 == 3 || n % 4 == 0
        3 -> n % 4 != 0
        4 -> n % 50 != 0
        else -> true
    }
    val res = Array(
        OPS_PER_THREAD, (fun(n: Int) = if (f(n)) {
            random.nextInt()
        } else {
            null
        })
    )
    return res
}

fun randomFilling(): Array<Int?> {
    val random = Random(System.currentTimeMillis())
    val res = Array(
        OPS_PER_THREAD
    ) {
        val n = random.nextInt()
        if (n % 2 == 0) {
            n
        } else {
            null
        }
    }
    return res
}

fun measurePerformance(stackType: StackType, testData: Array<Array<Int?>>): Double {
    val stack = createStack(stackType)
    val stackOps = StackOps(stack, testData)
    val startTime = System.currentTimeMillis()
    run {
        val threads = Array(THREADS) { stackOps.ExecuteThread() }
        threads.forEach { it.join() }
    }
    val endTime = System.currentTimeMillis()
    stackOps.close()
    val millisInSecond = 1000
    return (endTime - startTime).toDouble() / millisInSecond
}


fun createStack(stackType: StackType): Stack<Int> {
    val stack = when (stackType) {
        StackType.TREIBER -> TreiberStack<Int>()
        StackType.ELIMINATION -> EliminationStack()
    }
    val value = 0
    repeat(THREADS * OPS_PER_THREAD) {
        stack.push(value)
    }
    return stack
}

fun main() {
    var acceleration: Double
    val rounds = 100 // MORE THAN 2
    var results: MutableList<Pair<Double, Double>>

    run { // разогрев компилятора
        val testData = Array(THREADS) {
            sleep(1)
            alternatingFilling(0)
        }
        repeat(rounds / 10) {
            measurePerformance(StackType.ELIMINATION, testData)
            measurePerformance(StackType.TREIBER, testData)
        }
    }

    println("|-------------------|-----------------------|-------------------|-------------------|-------------------|-----------------------|")
    println("|\t\tData\t\t|\tElimination stack\t|\t\tSD (el.)\t|\tTreiber stack\t|\tSD (tr.)\t\t|\t\tAcceleration\t|")
    println("|-------------------|-----------------------|-------------------|-------------------|-------------------|-----------------------|")

    for (i in 0..4) {
        results = mutableListOf()
        val testData = Array(THREADS) {
            sleep(1)
            alternatingFilling(i)
        }

        repeat(rounds) {
            val elim = measurePerformance(StackType.ELIMINATION, testData)
            val treiber = measurePerformance(StackType.TREIBER, testData)
            results.add(Pair(elim, treiber))
        }

        var arrElim = mutableListOf<Double>()
        var arrTreiber = mutableListOf<Double>()

        for (round in 0..<rounds) {
            arrElim.add(results[round].first)
            arrTreiber.add(results[round].second)
        }

        arrElim = removeOutliers(arrElim)
        arrTreiber = removeOutliers(arrTreiber)

        val sdElim = round(standardDeviation(arrElim))
        val sdTreiber = round(standardDeviation(arrTreiber))

        val meanElim = round(arrElim.average())
        val meanTreiber = round(arrTreiber.average())

        acceleration = round(meanTreiber / meanElim)
        val data = when (i) {
            0 -> "PUSH-POP"
            1 -> "PUSH-PUSH-POP"
            2 -> "PUSH-PUSH-POP-POP"
            3 -> "PUSH-PUSH-PUSH-POP"
            4 -> "(49 * PUSH)-POP"
            else -> "_"
        }
        println("|\t\t$data\t\t|\t\t$meanElim\t\t\t|\t\t$sdElim\t\t|\t\t$meanTreiber\t\t\t|\t\t$sdTreiber\t\t|\t$acceleration\t\t|")
    }
}


