import stacks.eliminationStack.EliminationStack

import org.jetbrains.kotlinx.lincheck.annotations.*
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.junit.jupiter.api.Test

class StackTests {
    private val stack = EliminationStack<Int>()

    @Operation
    fun pop() = stack.pop()

    @Operation
    fun push(e: Int) = stack.push(e)

    @Operation
    fun getTop() = stack.getTop()

    @Test
    fun test() = StressOptions().actorsPerThread(3).check(this::class)
}

