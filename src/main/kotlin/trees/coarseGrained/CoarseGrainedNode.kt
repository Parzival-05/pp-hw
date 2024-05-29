package org.sbt.trees.coarseGrained

import org.sbt.trees.common.AbstractSynchronizedNode
import trees.common.AbstractNode

open class CoarseGrainedNode<K : Comparable<K>, V>(
    override var key: K,
    override var value: V,
) : AbstractSynchronizedNode<K, V>(key, value) {
    // ---------------------------------- lock ----------------------------------
    /** Does nothing */
    override fun lockNode() = Unit

    /** Does nothing */
    override fun unlockNode() = Unit

    /** @returns false */
    override fun isLocked(): Boolean = false

    // ---------------------------------- getters ----------------------------------
    override fun getLeftChild(): CoarseGrainedNode<K, V>? = super.getLeftChild() as CoarseGrainedNode<K, V>?
    override fun getRightChild(): CoarseGrainedNode<K, V>? = super.getRightChild() as CoarseGrainedNode<K, V>?
    override fun getChild(
        key: K, parentNode: AbstractNode<K, V>?,
        cont: (child: AbstractNode<K, V>?, node: AbstractNode<K, V>, parentNode: AbstractNode<K, V>?) -> Unit
    ): CoarseGrainedNode<K, V>? =
        super.getChild(key, parentNode, cont) as CoarseGrainedNode<K, V>?
}