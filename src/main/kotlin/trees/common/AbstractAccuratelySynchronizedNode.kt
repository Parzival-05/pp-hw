package org.sbt.trees.common

import kotlinx.atomicfu.atomic

abstract class AbstractAccuratelySynchronizedNode<K : Comparable<K>, V>(
    override var key: K,
    override var value: V,
) : AbstractSynchronizedNode<K, V>(key, value) {
    // ---------------------------------- lock ----------------------------------

    private val lock = atomic(false)
    override fun lockNode() {
        while (true) {
            if (lock.compareAndSet(expect = false, update = true)) {
                break
            }
            Thread.sleep(5)
        }
    }

    override fun unlockNode() {
        while (true) {
            if (lock.compareAndSet(expect = true, update = false)) {
                break
            }
            Thread.sleep(5)
        }
    }

    override fun isLocked(): Boolean = lock.value
}