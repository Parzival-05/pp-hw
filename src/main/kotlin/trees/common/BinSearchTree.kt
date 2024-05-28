package org.sbt.trees.common

import trees.common.AbstractBinSearchTree

open class BinSearchTree<K : Comparable<K>, V> : AbstractBinSearchTree<K, V, Node<K, V>>() {
    // ---------------------------------- insert ----------------------------------
    override fun constructNode(key: K, value: V): Node<K, V> = Node(key, value)
}