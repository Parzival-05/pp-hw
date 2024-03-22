package stacks

import com.homework.exceptions.EmptyStackException
import com.homework.Node
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.loop

open class TreiberStack<T> : Stack<T>() {
    private val top = atomic<Node<T>?>(null)

    fun tryPush(node: Node<T>): Boolean {
        val oldTop = this.top.value
        node.next = oldTop
        return this.top.compareAndSet(oldTop, node)
    }

    override fun push(value: T) {
        val node: Node<T> = Node(value)
        this.top.loop { _ ->
            if (tryPush(node)) {
                return
            }
        }
    }

    fun tryPop(): Node<T>? {
        val oldTop = this.top.value ?: throw EmptyStackException()
        val newTop = oldTop.next
        return if (this.top.compareAndSet(oldTop, newTop)) {
            oldTop
        } else {
            null
        }
    }

    override fun pop(): T {
        this.top.loop { _ ->
            val returnNode = tryPop()
            if (returnNode != null) {
                return returnNode.value
            }
        }
    }

    override fun getTop(): T? {
        return this.top.value?.value
    }
}
