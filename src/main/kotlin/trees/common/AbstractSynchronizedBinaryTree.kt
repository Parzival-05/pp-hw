package org.sbt.trees.common

import kotlinx.atomicfu.atomic
import trees.common.AbstractBinSearchTree
import trees.common.AbstractNode

abstract class AbstractSynchronizedBinaryTree<K : Comparable<K>, V, NodeT : AbstractNode<K, V>> :
    AbstractBinSearchTree<K, V, NodeT>() {
    // ---------------------------------- lock ----------------------------------

    open val lock = atomic(false)

    protected fun lockTree() {
        while (true) {
            if (lock.compareAndSet(expect = false, update = true)) {
                break
            }
            Thread.sleep(5)
        }
    }

    protected fun unlockTree() {
        while (true) {
            if (lock.compareAndSet(expect = true, update = false)) {
                break
            }
            Thread.sleep(5)
        }
    }
}