package org.sbt.trees.common

import trees.common.AbstractNode

open class Node<K : Comparable<K>, V>(
    override var key: K,
    override var value: V,
) : AbstractNode<K, V>(key, value) {
    // ---------------------------------- getters ----------------------------------
    override fun getChild(key: K, cont: (AbstractNode<K, V>?) -> Unit): Node<K, V>? {
        val node = super.getChild(key, cont) as Node<K, V>?
        cont(node)
        return node
    }
}
