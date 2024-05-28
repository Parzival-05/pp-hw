package org.sbt.trees.optimistic

import org.sbt.trees.common.AbstractAccuratelySynchronizedTree
import java.util.*
import kotlin.jvm.optionals.getOrNull

class OptimisticTree<K : Comparable<K>, V> :
    AbstractAccuratelySynchronizedTree<K, V, OptimisticNode<K, V>>() {
    // ---------------------------------- find ----------------------------------

    override fun findNodeAndParent(
        node: OptimisticNode<K, V>,
        parentNode: OptimisticNode<K, V>?,
        key: K
    ): Pair<Optional<OptimisticNode<K, V>>, Optional<OptimisticNode<K, V>>> {
        val thisKey = node.getKeyValue().first
        if (thisKey == key) {
            parentNode?.lockNode() ?: this.lockTree()
            node.lockNode()
            return Pair(Optional.of(node), Optional.ofNullable(parentNode))
        } else {
            val child = node.getChild(key)
            return if (child == null) {
                node.lockNode()
                Pair(Optional.empty(), Optional.of(node))
            } else {
                this.findNodeAndParent(child, node, key)
            }
        }
    }

    private fun checkNodeAndParentAfterLock(
        node: OptimisticNode<K, V>,
        parentNode: OptimisticNode<K, V>?,
        key: K
    ): Boolean {
        val thisKey = node.getKeyValue().first
        if (thisKey == key) {
            return if (parentNode == null) {
                this.isLocked() && node.isLocked()
            } else {
                parentNode.isLocked() && node.isLocked()
            }
        } else {
            val child = node.getChild(key)
            return if (child == null) {
                false
            } else {
                this.checkNodeAndParentAfterLock(child, node, key)
            }
        }
    }


    override fun findNodeAndParent(
        key: K
    ): Pair<Optional<OptimisticNode<K, V>>, Optional<OptimisticNode<K, V>>>? {
        val root = this.root ?: return null
        while (true) {
            val res = this.findNodeAndParent(root, null, key)
            val isInTree = checkNodeAndParentAfterLock(root, null, key)
            if (isInTree) {
                return res
            } else {
                val (objectiveNodeOptional, parentOfObjectiveNodeOptional) = res
                val parentOfObjectiveNode = parentOfObjectiveNodeOptional.getOrNull()
                val objectiveNode = objectiveNodeOptional.getOrNull()
                parentOfObjectiveNode?.unlockNode() ?: this.unlockTree()
                objectiveNode?.unlockNode()
            }
        }
    }

    override fun find(key: K): V? {
        val nodeAndParent = this.findNodeAndParent(key)
        return if (nodeAndParent == null) {
            null
        } else {
            val (objectiveNodeOptional, parentOfObjectiveNodeOptional) = nodeAndParent
            parentOfObjectiveNodeOptional.getOrNull()?.unlockNode() ?: this.unlockTree()
            val objectiveNode = objectiveNodeOptional.getOrNull() ?: return null
            objectiveNode.unlockNode()
            objectiveNode.getKeyValue().second
        }
    }
    // ---------------------------------- delete ----------------------------------

    override fun delete(key: K) {
        val nodeAndParent = this.findNodeAndParent(key)
        if (nodeAndParent != null) {
            val (objectiveNodeOptional, parentOfObjectiveNodeOptional) = nodeAndParent
            val objectiveNode = objectiveNodeOptional.getOrNull()
            val parentOfObjectiveNode = parentOfObjectiveNodeOptional.getOrNull()
            if (objectiveNode == null) {
                parentOfObjectiveNode?.unlockNode() ?: this.unlockTree()
                return
            }
            val left = objectiveNode.getLeftChild()
            val right = objectiveNode.getRightChild()
            return if (left == null || right == null) {
                this.deleteZeroOrOneChild(objectiveNode, parentOfObjectiveNode, left, right, key)
            } else {
                this.deleteTwoChildren(objectiveNode, parentOfObjectiveNode, left, right)
                objectiveNode.unlockNode()
            }
        }
    }

    // ---------------------------------- insert ----------------------------------

    override fun insert(key: K, value: V): Boolean {
        val nodeAndParent = this.findNodeAndParent(key)
        val nodeToInsert = constructNode(key, value)
        return if (nodeAndParent == null) {
            this.root = nodeToInsert
            true
        } else {
            val (objectiveNode, parentNodeOptional) = nodeAndParent
            this.insert(objectiveNode.getOrNull(), parentNodeOptional.get(), nodeToInsert)
        }
    }

    override fun constructNode(key: K, value: V): OptimisticNode<K, V> = OptimisticNode(key, value)
}