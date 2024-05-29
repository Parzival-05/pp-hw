package trees.common

import org.sbt.trees.exceptions.UnexpectedState

abstract class AbstractNode<K : Comparable<K>, V>(
    open var key: K,
    open var value: V
) {
    open var left: AbstractNode<K, V>? = null
    open var right: AbstractNode<K, V>? = null

    // ---------------------------------- getters & setters ----------------------------------
    open fun getLeftChild(): AbstractNode<K, V>? {
        return this.left
    }

    open fun getRightChild(): AbstractNode<K, V>? {
        return this.right
    }

    open fun setLeftChild(node: AbstractNode<K, V>?) {
        this.left = node
    }

    open fun setRightChild(node: AbstractNode<K, V>?) {
        this.right = node
    }

    open fun getKeyValue(): Pair<K, V> {
        return Pair(this.key, this.value)
    }

    /**
     * Doesn't guarantee that the son has a key == [key]
     *
     * @return if ([key] < [this.getKey()]) then (this.left) else if
     *     ([key] > [this.getKey()]) then this.right else throw UnexpectedState
     */
    open fun getChild(
        key: K,
        parentNode: AbstractNode<K, V>? = null,
        cont: (child: AbstractNode<K, V>?, node: AbstractNode<K, V>, parentNode: AbstractNode<K, V>?) -> Unit = { _, _, _ -> }
    ): AbstractNode<K, V>? {
        val thisKey = this.key
        val node = if (key == thisKey) {
            throw UnexpectedState("Son and its parent have the same keys")
        } else if (key < thisKey) {
            this.getLeftChild()
        } else {
            this.getRightChild()
        }
        cont(node, this, parentNode)
        return node
    }

    /**
     * Doesn't guarantee that the son has a key == [key].
     *
     * Replaces one of the child with [node] depending on [key], using
     * [setLeftChild] or [setRightChild]
     *
     * @see getChild
     * @see setLeftChild
     * @see setRightChild
     */
    open fun setChild(key: K, node: AbstractNode<K, V>?) {
        val thisKey = this.getKeyValue().first
        if (key == thisKey) {
            throw UnexpectedState("Son and its parent have the same keys")
        } else {
            val setter = if (key < thisKey) {
                this::setLeftChild
            } else {
                this::setRightChild
            }
            setter(node)
        }
    }
}
