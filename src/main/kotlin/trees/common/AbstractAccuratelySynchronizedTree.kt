package org.sbt.trees.common

import java.util.*
import kotlin.jvm.optionals.getOrNull

abstract class AbstractAccuratelySynchronizedTree<K : Comparable<K>, V, NodeT : AbstractAccuratelySynchronizedNode<K, V>> :
    AbstractSynchronizedBinaryTree<K, V, NodeT>() {
    // ---------------------------------- lock ----------------------------------
    open fun isLocked(): Boolean = lock.value

    // ---------------------------------- find ----------------------------------
    abstract override fun findNodeAndParent(
        key: K
    ): Pair<Optional<NodeT>, Optional<NodeT>>?

    /** finds value of node with key = [key] and unlocks node with its parent */
    override fun find(key: K): V? {
        val nodeAndParent = this.findNodeAndParent(key)
        return if (nodeAndParent == null) {
            this.unlockTree()
            null
        } else {
            val (objectiveNodeOptional, parentOfObjectiveNodeOptional) = nodeAndParent
            parentOfObjectiveNodeOptional.getOrNull()?.unlockNode() ?: this.unlockTree()
            val objectiveNode = objectiveNodeOptional.getOrNull() ?: return null
            objectiveNode.unlockNode()
            objectiveNode.getKeyValue().second
        }
    }

    // ---------------------------------- insert ----------------------------------
    override fun insert(objectiveNode: NodeT?, parentNode: NodeT?, nodeToInsert: NodeT): Boolean {
        if (objectiveNode == null) { // insertion
            this.insert(parentNode, nodeToInsert)
        }
        if (parentNode == null) { // unlocking
            this.unlockTree()
        } else {
            parentNode.unlockNode()
        }
        return if (objectiveNode == null) { // checking insertion
            true
        } else { // already in tree
            objectiveNode.unlockNode()
            false
        }
    }

    // ---------------------------------- delete ----------------------------------
    override fun delete1Children(
        objectiveNode: NodeT,
        parentOfObjectiveNode: NodeT?,
        left: NodeT?,
        right: NodeT?,
        key: K
    ) {
        val newNode = (left ?: right)
        if (parentOfObjectiveNode == null) {
            this.root = newNode
            this.unlockTree()
        } else {
            parentOfObjectiveNode.setChild(key, newNode)
            parentOfObjectiveNode.unlockNode()
        }
        return
    }

    override fun delete2Children(objectiveNode: NodeT, parentOfObjectiveNode: NodeT?, left: NodeT, right: NodeT) {
        left.lockNode()
        right.lockNode()
        if (parentOfObjectiveNode != null) {
            parentOfObjectiveNode.setLeftChild(left)
            parentOfObjectiveNode.unlockNode()
        } else {
            root = left
            this.unlockTree()
        }
        val (_, mostRightOfLeftOptional) = this.findNodeAndParent(
            node = left,
            parentNode = objectiveNode,
            key = objectiveNode.getKeyValue().first
        )
        val mostRightOfLeft = mostRightOfLeftOptional.get()
        mostRightOfLeft.setRightChild(right)
        mostRightOfLeft.unlockNode()
        right.unlockNode()
    }

    override fun delete(key: K) {
        val nodeAndParent = this.findNodeAndParent(key)
        return if (nodeAndParent == null) {
            this.unlockTree()
        } else {
            val (objectiveNodeOptional, parentOfObjectiveNodeOptional) = nodeAndParent
            val parentOfObjectiveNode = parentOfObjectiveNodeOptional.getOrNull()
            val objectiveNode = objectiveNodeOptional.getOrNull()
            if (objectiveNode == null) {
                parentOfObjectiveNode?.unlockNode() ?: this.unlockTree()
                return
            }
            @Suppress("UNCHECKED_CAST") val left = objectiveNode.left as NodeT?
            @Suppress("UNCHECKED_CAST") val right = objectiveNode.right as NodeT?
            if (left == null || right == null) {
                this.delete1Children(objectiveNode, parentOfObjectiveNode, left, right, key)
            } else {
                this.delete2Children(objectiveNode, parentOfObjectiveNode, left, right)
            }
        }
    }
}