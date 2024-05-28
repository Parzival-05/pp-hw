package org.sbt.trees.fineGrained

import org.sbt.trees.common.AbstractAccuratelySynchronizedTree
import java.util.*
import kotlin.jvm.optionals.getOrNull

class FineGrainedTree<K : Comparable<K>, V> :
    AbstractAccuratelySynchronizedTree<K, V, FineGrainedNode<K, V>>() {
    // ---------------------------------- find ----------------------------------
    override tailrec fun findNodeAndParent(
        node: FineGrainedNode<K, V>,
        parentNode: FineGrainedNode<K, V>?,
        key: K
    ): Pair<Optional<FineGrainedNode<K, V>>, Optional<FineGrainedNode<K, V>>> {
        val thisKey = node.getKeyValue().first
        if (thisKey == key) {
            return Pair(Optional.of(node), Optional.ofNullable(parentNode))
        } else {
            val child =
                node.getChild(key) {
                    parentNode?.unlockNode() ?: this.unlockTree()
                }
            return if (child == null) {
                Pair(Optional.empty(), Optional.of(node))
            } else {
                this.findNodeAndParent(child, node, key)
            }
        }
    }

    override fun findNodeAndParent(key: K): Pair<Optional<FineGrainedNode<K, V>>, Optional<FineGrainedNode<K, V>>>? {
        this.lockTree()
        val root = this.root ?: return null
        root.lockNode()
        return this.findNodeAndParent(root, null, key)
    }

    // ---------------------------------- insert ----------------------------------
    override fun insert(key: K, value: V): Boolean {
        val nodeAndParent = this.findNodeAndParent(key)
        val nodeToInsert = constructNode(key, value)
        return if (nodeAndParent == null) {
            this.root = nodeToInsert
            this.unlockTree()
            true
        } else {
            val (objectiveNode, parentNodeOptional) = nodeAndParent
            this.insert(objectiveNode.getOrNull(), parentNodeOptional.getOrNull(), nodeToInsert)
        }
    }

    override fun constructNode(key: K, value: V): FineGrainedNode<K, V> = FineGrainedNode(key, value)

}
