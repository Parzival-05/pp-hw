package stacks

abstract class Stack<T> {
    abstract fun push(value: T)

    abstract fun pop(): T

    abstract fun getTop(): T?
}
