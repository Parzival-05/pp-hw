package trees.common

import org.sbt.trees.exceptions.UnexpectedState
import java.util.*
import kotlin.jvm.optionals.getOrNull

abstract class AbstractBinSearchTree<K : Comparable<K>, V, NodeT : AbstractNode<K, V>> {
    open var root: NodeT? = null
    // ---------------------------------- find ----------------------------------

    /** Finding from some node */
    open fun findNodeAndParent(
        node: NodeT,
        parentNode: NodeT?,
        key: K
    ): Pair<Optional<NodeT>, Optional<NodeT>> {
        val thisKey = node.getKeyValue().first
        if (thisKey == key) {
            return Pair(Optional.of(node), Optional.ofNullable(parentNode))
        } else {
            @Suppress("UNCHECKED_CAST") val child =
                node.getChild(key) as NodeT? // bin tree invariant: children of node with type A has only A type
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
        val root = this.root ?: return null
        return this.findNodeAndParent(root, null, key)
    }

    open fun find(key: K): V? {
        val root = this.root ?: return null
        val nodeAndParent = this.findNodeAndParent(root, null, key)
        val (objectiveNodeOptional, parentOfObjectiveNodeOptional) = nodeAndParent
        parentOfObjectiveNodeOptional.getOrNull()
        return objectiveNodeOptional.getOrNull()?.getKeyValue()?.second
    }

    // ---------------------------------- insert ----------------------------------
    abstract fun constructNode(key: K, value: V): AbstractNode<K, V>
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
        @Suppress("UNCHECKED_CAST") val nodeToInsert = constructNode(key, value) as NodeT
        return if (nodeAndParent == null) {
            this.root = nodeToInsert
            true
        } else {
            val (objectiveNode, parentNodeOptional) = nodeAndParent
            this.insert(objectiveNode.getOrNull(), parentNodeOptional.getOrNull(), nodeToInsert)
        }
    }
    // ---------------------------------- delete ----------------------------------

    open fun delete1Children(
        objectiveNode: NodeT,
        parentOfObjectiveNode: NodeT?,
        left: NodeT?,
        right: NodeT?,
        key: K
    ) {
        val newNode = (left ?: right)
        if (parentOfObjectiveNode == null) {
            this.root = newNode as NodeT
        } else {
            parentOfObjectiveNode.setChild(key, newNode)
        }
        return
    }

    open fun delete2Children(objectiveNode: NodeT, parentOfObjectiveNode: NodeT?, left: NodeT, right: NodeT) {
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
                this.delete1Children(objectiveNode, parentOfObjectiveNode, left, right, key)
            } else {
                this.delete2Children(objectiveNode, parentOfObjectiveNode, left, right)
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