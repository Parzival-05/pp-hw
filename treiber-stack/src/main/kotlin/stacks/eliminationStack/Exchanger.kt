package stacks.eliminationStack

import com.homework.Exception.TimeoutException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicStampedReference

class Exchanger<T> {
    companion object {
        const val EMPTY = 0
        const val WAITINGPOPPER = 1
        const val WAITINGPUSHER = 2
        const val BUSY = 3
    }

    private val slot = AtomicStampedReference<T>(null, EMPTY)

    fun exchange(myItem: T?, timeout: Long, unit: TimeUnit): T? {
        val status = if (myItem == null) WAITINGPOPPER else WAITINGPUSHER
        val nanos = unit.toNanos(timeout)
        val timeBound = System.nanoTime() + nanos
        val stampHolder = IntArray(1) { EMPTY }
        while (true) {
            if (System.nanoTime() > timeBound) {
                throw TimeoutException()
            }
            var yrItem = slot.get(stampHolder)
            val stamp = stampHolder[0]
            when (stamp) {
                EMPTY -> {
                    if (slot.compareAndSet(yrItem, myItem, EMPTY, status)) {
                        while (System.nanoTime() < timeBound) {
                            yrItem = slot.get(stampHolder)
                            if (stampHolder[0] == BUSY) {
                                slot.set(null, EMPTY)
                                return yrItem
                            }
                        }
                        if (slot.compareAndSet(myItem, null, status, EMPTY)) {
                            throw TimeoutException()
                        } else {
                            yrItem = slot.get(stampHolder)
                            slot.set(null, EMPTY)
                            return yrItem
                        }
                    }
                }

                WAITINGPOPPER -> {
                    if (status != WAITINGPOPPER && slot.compareAndSet(yrItem, myItem, WAITINGPOPPER, BUSY)) {
                        return yrItem
                    }
                }

                WAITINGPUSHER -> {
                    if (status != WAITINGPUSHER && slot.compareAndSet(yrItem, null, WAITINGPUSHER, BUSY)) {
                        return yrItem
                    }
                }

                BUSY -> {
                }
            }
        }
    }
}
