package org.sbt.trees.fineGrained

import org.sbt.trees.common.AbstractAccuratelySynchronizedTree
import java.util.*
import kotlin.jvm.optionals.getOrNull

class FineGrainedTree<K : Comparable<K>, V> :
    AbstractAccuratelySynchronizedTree<K, V, FineGrainedNode<K, V>>() {
    // ---------------------------------- find ----------------------------------
    override fun foundObjectiveNode(objectiveNode: FineGrainedNode<K, V>?, parentNode: FineGrainedNode<K, V>?) = Unit

    override fun foundNextChildToCheck(
        child: FineGrainedNode<K, V>?,
        node: FineGrainedNode<K, V>,
        parentNode: FineGrainedNode<K, V>?
    ) {
        parentNode?.unlockNode() ?: this.unlockTree()
    }

    override fun findNodeAndParent(key: K): Pair<Optional<FineGrainedNode<K, V>>, Optional<FineGrainedNode<K, V>>>? {
        this.lockTree()
        val root = this.root ?: return null
        root.lockNode()
        return this.findNodeAndParent(root, null, key)
    }

    // ---------------------------------- delete ----------------------------------
    override fun deleteTwoChildren(
        objectiveNode: FineGrainedNode<K, V>,
        parentOfObjectiveNode: FineGrainedNode<K, V>?,
        left: FineGrainedNode<K, V>,
        right: FineGrainedNode<K, V>
    ) {
        left.lockNode()
        return super.deleteTwoChildren(objectiveNode, parentOfObjectiveNode, left, right)
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
