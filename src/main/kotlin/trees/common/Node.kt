package org.sbt.trees.common

import trees.common.AbstractNode

open class Node<K : Comparable<K>, V>(
    override var key: K,
    override var value: V,
) : AbstractNode<K, V>(key, value) {
    // ---------------------------------- getters ----------------------------------
    override fun getChild(
        key: K, parentNode: AbstractNode<K, V>?,
        cont: (child: AbstractNode<K, V>?, node: AbstractNode<K, V>, parentNode: AbstractNode<K, V>?) -> Unit
    ): Node<K, V>? = super.getChild(key, parentNode, cont) as Node<K, V>?
}
