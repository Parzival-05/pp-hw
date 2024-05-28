package org.sbt.trees.common

import trees.common.AbstractNode

abstract class AbstractSynchronizedNode<K : Comparable<K>, V>(
    override var key: K,
    override var value: V,
) : AbstractNode<K, V>(key, value) {
    // ---------------------------------- lock ----------------------------------
    abstract fun lockNode()
    abstract fun unlockNode()
    abstract fun isLocked(): Boolean
}