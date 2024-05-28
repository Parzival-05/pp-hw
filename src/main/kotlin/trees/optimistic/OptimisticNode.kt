package org.sbt.trees.optimistic

import org.sbt.trees.common.AbstractAccuratelySynchronizedNode
import trees.common.AbstractNode

class OptimisticNode<K : Comparable<K>, V>(
    override var key: K,
    override var value: V,
) : AbstractAccuratelySynchronizedNode<K, V>(key, value) {
    override fun getLeftChild(): OptimisticNode<K, V>? =
        super.getLeftChild() as OptimisticNode<K, V>?

    override fun getRightChild(): OptimisticNode<K, V>? =
        super.getRightChild() as OptimisticNode<K, V>?


    override fun getChild(key: K, cont: (AbstractNode<K, V>?) -> Unit): OptimisticNode<K, V>? =
        super.getChild(key, cont) as OptimisticNode<K, V>?
}
