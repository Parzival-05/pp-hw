import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.sbt.trees.coarseGrained.CoarseGrainedNode
import org.sbt.trees.coarseGrained.CoarseGrainedTree
import org.sbt.trees.fineGrained.FineGrainedNode
import org.sbt.trees.fineGrained.FineGrainedTree
import org.sbt.trees.optimistic.OptimisticNode
import org.sbt.trees.optimistic.OptimisticTree
import trees.common.AbstractBinSearchTree
import trees.common.AbstractNode
import kotlin.test.Test

abstract class TreeTest<TreeT : AbstractBinSearchTree<Int, Int, NodeT>, NodeT : AbstractNode<Int, Int>>(
    val tree: TreeT
) {
    @Operation
    fun delete(key: Int) {
        tree.delete(key)
    }

    @Operation
    fun insert(e: Int) = tree.insert(e, e)

    @Operation
    fun find(key: Int) = tree.find(key)

    @Test
    open fun test() = ModelCheckingOptions().actorsBefore(2) // Number of operations before the parallel part
        .threads(4) // Number of threads in the parallel part
        .actorsPerThread(2) // Number of operations in each thread of the parallel part
        .actorsAfter(1) // Number of operations after the parallel part
        .iterations(300) // Generate 100 random concurrent scenarios
        .invocationsPerIteration(6) // Run each generated scenario 1000 times
        .check(this::class)
}

/*
* This have to not pass
* */
//class BinSearchTreeTest :
//    TreeTest<BinSearchTree<Int, Int>, Node<Int, Int>>(BinSearchTree())

class OptimisticTreeTest :
    TreeTest<OptimisticTree<Int, Int>, OptimisticNode<Int, Int>>(OptimisticTree())

class FineGrainedTreeTest :
    TreeTest<FineGrainedTree<Int, Int>, FineGrainedNode<Int, Int>>(FineGrainedTree())

class CoarseGrainedTreeTest :
    TreeTest<CoarseGrainedTree<Int, Int>, CoarseGrainedNode<Int, Int>>(CoarseGrainedTree())
