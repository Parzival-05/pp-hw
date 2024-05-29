package org.sbt.trees.optimistic

import org.sbt.trees.common.AbstractAccuratelySynchronizedTree
import java.util.*
import kotlin.jvm.optionals.getOrNull

class OptimisticTree<K : Comparable<K>, V> :
    AbstractAccuratelySynchronizedTree<K, V, OptimisticNode<K, V>>() {
    // ---------------------------------- find ----------------------------------
    override fun foundObjectiveNode(objectiveNode: OptimisticNode<K, V>?, parentNode: OptimisticNode<K, V>?) {
        parentNode?.lockNode() ?: this.lockTree()
        objectiveNode?.lockNode()
    }

    override fun foundNextChildToCheck(
        child: OptimisticNode<K, V>?,
        node: OptimisticNode<K, V>,
        parentNode: OptimisticNode<K, V>?
    ) {
        if (child == null) {
            node.lockNode()
        }
    }

    private fun checkNodeAndParentAfterLock(
        node: OptimisticNode<K, V>?,
        parentNode: OptimisticNode<K, V>?,
        key: K,
        expectedNode: OptimisticNode<K, V>?,
        expectedParentNode: OptimisticNode<K, V>?
    ): Boolean {
        val thisKey = node?.getKeyValue()?.first
        if (thisKey == null || thisKey == key) {
            return expectedNode == node && expectedParentNode == parentNode
        } else {
            val child = node.getChild(key)
            return if (child == null) {
                expectedNode == null && expectedParentNode == node
            } else {
                this.checkNodeAndParentAfterLock(child, node, key, expectedNode, expectedParentNode)
            }
        }
    }

    override fun findNodeAndParent(
        key: K
    ): Pair<Optional<OptimisticNode<K, V>>, Optional<OptimisticNode<K, V>>> {
        while (true) {
            val (objectiveNodeOptional, parentOfObjectiveNodeOptional) = this.findNodeAndParent(root, null, key)
            val parentOfObjectiveNode = parentOfObjectiveNodeOptional.getOrNull()
            val objectiveNode = objectiveNodeOptional.getOrNull()
            val isInTree = checkNodeAndParentAfterLock(root, null, key, objectiveNode, parentOfObjectiveNode)
            if (isInTree) {
                return Pair(objectiveNodeOptional, parentOfObjectiveNodeOptional)
            } else {
                parentOfObjectiveNode?.unlockNode() ?: this.unlockTree()
                objectiveNode?.unlockNode()
            }
        }
    }

    override fun find(key: K): V? {
        val nodeAndParent = this.findNodeAndParent(key)

        val (objectiveNodeOptional, parentOfObjectiveNodeOptional) = nodeAndParent
        parentOfObjectiveNodeOptional.getOrNull()?.unlockNode() ?: this.unlockTree()
        val objectiveNode = objectiveNodeOptional.getOrNull() ?: return null
        objectiveNode.unlockNode()
        return objectiveNode.getKeyValue().second
    }

    // ---------------------------------- delete ----------------------------------

    override fun delete(key: K) {
        val nodeAndParent = this.findNodeAndParent(key)

        val (objectiveNodeOptional, parentOfObjectiveNodeOptional) = nodeAndParent
        val objectiveNode = objectiveNodeOptional.getOrNull()
        val parentOfObjectiveNode = parentOfObjectiveNodeOptional.getOrNull()
        if (objectiveNode == null) {
            parentOfObjectiveNode?.unlockNode() ?: this.unlockTree()
            return
        }
        val left = objectiveNode.getLeftChild()
        val right = objectiveNode.getRightChild()
        if (left == null || right == null) {
            this.deleteZeroOrOneChild(objectiveNode, parentOfObjectiveNode, left, right, key)
        } else {
            this.deleteTwoChildren(objectiveNode, parentOfObjectiveNode, left, right)
        }
        objectiveNode.unlockNode()
        return
    }

    // ---------------------------------- insert ----------------------------------
    override fun constructNode(key: K, value: V): OptimisticNode<K, V> = OptimisticNode(key, value)
}