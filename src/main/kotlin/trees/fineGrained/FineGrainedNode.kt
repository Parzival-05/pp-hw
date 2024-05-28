package org.sbt.trees.fineGrained

import org.sbt.trees.common.AbstractAccuratelySynchronizedNode
import trees.common.AbstractNode

open class FineGrainedNode<K : Comparable<K>, V>(
    override var key: K,
    override var value: V,
) : AbstractAccuratelySynchronizedNode<K, V>(key, value) {
    /**
     * acquires locking for left child and returns it
     *
     * @return left child
     */
    override fun getLeftChild(): FineGrainedNode<K, V>? {
        val node = super.getLeftChild() as FineGrainedNode<K, V>? ?: return null
        node.lockNode()
        return node
    }

    /**
     * acquires locking for right child and returns it
     *
     * @return left child
     */
    override fun getRightChild(): FineGrainedNode<K, V>? {
        val node = super.getRightChild() as FineGrainedNode<K, V>? ?: return null
        node.lockNode()
        return node
    }

    override fun getChild(key: K, cont: (AbstractNode<K, V>?) -> Unit): FineGrainedNode<K, V>? =
        super.getChild(key, cont) as FineGrainedNode<K, V>?
}