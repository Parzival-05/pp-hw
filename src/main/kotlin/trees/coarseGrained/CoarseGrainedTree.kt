package org.sbt.trees.coarseGrained

import org.sbt.trees.common.AbstractSynchronizedBinaryTree


open class CoarseGrainedTree<K : Comparable<K>, V> :
    AbstractSynchronizedBinaryTree<K, V, CoarseGrainedNode<K, V>>() {
    // ---------------------------------- lock ----------------------------------
    private fun <R> withLock(cont: () -> R): R {
        this.lockTree()
        val res = cont()
        this.unlockTree()
        return res
    }

    override fun find(key: K): V? = withLock { super.find(key) }
    override fun delete(key: K) = withLock { super.delete(key) }
    override fun insert(key: K, value: V): Boolean = withLock { super.insert(key, value) }
    override fun constructNode(key: K, value: V): CoarseGrainedNode<K, V> = CoarseGrainedNode(key, value)

}