package stacks.eliminationStack

import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

class EliminationArray<T>(capacity: Int, private val duration: Long = 5L) {

    private val random = ThreadLocalRandom.current()
    private val exchanger: List<Exchanger<T>> = MutableList(capacity) { _ -> Exchanger() }

    fun visit(value: T?, range: Int): T? {
        val slot = random.nextInt(range)
        return (exchanger[slot].exchange(value, duration, TimeUnit.MILLISECONDS))
    }
}
