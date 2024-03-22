package stacks.eliminationStack

import com.homework.Node
import com.homework.Exception.TimeoutException
import stacks.TreiberStack

class EliminationStack<T> : TreiberStack<T>() {
    private val eliminationArray = EliminationArray<T>(CAPACITY - 1)

    companion object {
        const val CAPACITY = 8

        val policy = object : ThreadLocal<RangePolicy>() {
            override fun initialValue(): RangePolicy {
                return RangePolicy(CAPACITY - 1)
            }
        }
    }

    override fun push(value: T) {
        val rangePolicy = policy.get()
        val node = Node(value)
        while (true) {
            if (tryPush(node)) {
                break
            } else try {
                eliminationArray.visit(value, rangePolicy.getRange())
                rangePolicy.recordEliminationSuccess()
                break
            } catch (ex: TimeoutException) {
                rangePolicy.recordEliminationFail()
            }
        }
    }

    override fun pop(): T {
        val rangePolicy = policy.get()
        while (true) {
            val returnNode = tryPop()
            if (returnNode != null) {
                return returnNode.value
            } else try {
                val otherValue = eliminationArray.visit(null, rangePolicy.getRange())
                if (otherValue != null) {
                    rangePolicy.recordEliminationSuccess()
                    return otherValue
                }
            } catch (ex: TimeoutException) {
                rangePolicy.recordEliminationFail()
            }

        }
    }
}

