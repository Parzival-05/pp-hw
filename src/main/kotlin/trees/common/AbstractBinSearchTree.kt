package trees.common

import org.sbt.trees.exceptions.UnexpectedState
import java.util.*
import kotlin.jvm.optionals.getOrNull

abstract class AbstractBinSearchTree<K : Comparable<K>, V, NodeT : AbstractNode<K, V>> {
    open var root: NodeT? = null

    // ---------------------------------- find ----------------------------------
    open fun foundObjectiveNode(objectiveNode: NodeT?, parentNode: NodeT?) = Unit
    open fun foundNextChildToCheck(child: NodeT?, node: NodeT, parentNode: NodeT?) = Unit

    /** Finding from some node */
    open fun findNodeAndParent(
        node: NodeT?,
        parentNode: NodeT?,
        key: K
    ): Pair<Optional<NodeT>, Optional<NodeT>> {
        val thisKey = node?.getKeyValue()?.first
        if (thisKey == null || thisKey == key) {
            this.foundObjectiveNode(node, parentNode)
            return Pair(Optional.ofNullable(node), Optional.ofNullable(parentNode))
        } else {
            @Suppress("UNCHECKED_CAST") val child =
                node.getChild(key, parentNode) { a, b, c ->
                    this.foundNextChildToCheck(
                        a as NodeT?,
                        b as NodeT,
                        c as NodeT?
                    )
                } as NodeT?
            return if (child == null) {
                Pair(Optional.empty(), Optional.of(node))
            } else {
                this.findNodeAndParent(child, node, key)
            }
        }
    }

    /** Finding from root */
    open fun findNodeAndParent(
        key: K
    ): Pair<Optional<NodeT>, Optional<NodeT>>? {
        return this.findNodeAndParent(this.root, null, key)
    }

    open fun find(key: K): V? {
        val root = this.root
        val nodeAndParent = this.findNodeAndParent(root, null, key)
        val (objectiveNodeOptional, parentOfObjectiveNodeOptional) = nodeAndParent
        parentOfObjectiveNodeOptional.getOrNull()
        return objectiveNodeOptional.getOrNull()?.getKeyValue()?.second
    }

    // ---------------------------------- insert ----------------------------------
    abstract fun constructNode(key: K, value: V): NodeT
    protected open fun insert(parentNode: NodeT?, node: NodeT) {
        if (parentNode == null) {
            this.root = node
        } else {
            parentNode.setChild(node.getKeyValue().first, node)
        }
    }

    protected open fun insert(objectiveNode: NodeT?, parentNode: NodeT?, nodeToInsert: NodeT): Boolean {
        if (objectiveNode == null) { // insertion
            this.insert(parentNode, nodeToInsert)
        }
        return objectiveNode == null
    }

    open fun insert(key: K, value: V): Boolean {
        val nodeAndParent = this.findNodeAndParent(key)
        val nodeToInsert = constructNode(key, value)
        return if (nodeAndParent == null) {
            this.root = nodeToInsert
            true
        } else {
            val (objectiveNode, parentNodeOptional) = nodeAndParent
            this.insert(objectiveNode.getOrNull(), parentNodeOptional.getOrNull(), nodeToInsert)
        }
    }
    // ---------------------------------- delete ----------------------------------

    open fun deleteZeroOrOneChild(
        objectiveNode: NodeT,
        parentOfObjectiveNode: NodeT?,
        left: NodeT?,
        right: NodeT?,
        key: K
    ) {
        val newNode = (left ?: right)
        if (parentOfObjectiveNode == null) {
            this.root = newNode
        } else {
            parentOfObjectiveNode.setChild(key, newNode)
        }
        return
    }

    open fun deleteTwoChildren(objectiveNode: NodeT, parentOfObjectiveNode: NodeT?, left: NodeT, right: NodeT) {
        if (parentOfObjectiveNode != null) {
            parentOfObjectiveNode.setLeftChild(left)
        } else {
            root = left
        }
        val (_, mostRightOfLeftOptional) = this.findNodeAndParent(
            node = left,
            parentNode = objectiveNode,
            key = objectiveNode.getKeyValue().first
        )
        val mostRightOfLeft = mostRightOfLeftOptional.getOrNull()
            ?: throw UnexpectedState("Found node unexpectedly became null")
        mostRightOfLeft.setRightChild(right)
    }

    open fun delete(key: K) {
        val nodeAndParent = this.findNodeAndParent(key)
        if (nodeAndParent != null) {
            val (objectiveNodeOptional, parentOfObjectiveNodeOptional) = nodeAndParent
            val parentOfObjectiveNode = parentOfObjectiveNodeOptional.getOrNull()
            val objectiveNode = objectiveNodeOptional.getOrNull() ?: return
            @Suppress("UNCHECKED_CAST") val left = objectiveNode.left as NodeT?
            @Suppress("UNCHECKED_CAST") val right = objectiveNode.right as NodeT?
            return if (left == null || right == null) {
                this.deleteZeroOrOneChild(objectiveNode, parentOfObjectiveNode, left, right, key)
            } else {
                this.deleteTwoChildren(objectiveNode, parentOfObjectiveNode, left, right)
            }
        }
    }
    // ---------------------------------- traverse ----------------------------------

    fun traverseTree(): MutableList<Pair<K, V>> {
        val list = mutableListOf<Pair<K, V>>()
        val node = root ?: return list
        traverseTree(node, list)
        return list
    }

    private fun traverseTree(node: NodeT, list: MutableList<Pair<K, V>>) {
        @Suppress("UNCHECKED_CAST") val left = node.getLeftChild() as NodeT?
        if (left != null) {
            traverseTree(left, list)
        }
        list.addLast(node.getKeyValue())
        @Suppress("UNCHECKED_CAST") val right = node.getRightChild() as NodeT?
        if (right != null) {
            traverseTree(right, list)
        }
    }
}