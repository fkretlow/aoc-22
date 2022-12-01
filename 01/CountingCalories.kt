fun main() {
    var state: State = ReadyState(0, NGreatest<Int>(3))
    while (true) {
        val line = readLine() ?: break
        state = state.consume(line.toIntOrNull())
    }
    val L = state.topThree.toList()
    L.forEach { println(it.toString()) }
    println(L.asSequence().sum())
}


// A little state machine. Probably overkill, but the diagram looked so nice and easy...
abstract class State(currentSum: Int, topThree: NGreatest<Int>) {

    val currentSum = currentSum;
    val topThree = topThree;

    abstract fun consume(input: Int?): State
}

class ReadyState(currentSum: Int, topThree: NGreatest<Int>) : State(currentSum, topThree) {

    override fun consume(input: Int?): State = when (input) {
        null -> ReadyState(currentSum, topThree)
        else -> CountingState(input, topThree)
    }
}

class CountingState(currentSum: Int, topThree: NGreatest<Int>) : State(currentSum, topThree) {

    override fun consume(input: Int?): State = when (input) {
        null -> ReadyState(0, topThree.insert(currentSum))
        else -> CountingState(currentSum + input, topThree)
    }
}


// A sorted list that only ever keeps the n greatest elements.
class NGreatest<T : Comparable<T>>(capacity: Int) {
    private val capacity: Int = capacity
    private var head: ListNode<T>? = null

    fun insert(x: T): NGreatest<T> {
        head = _insert(x, head, 0)
        return this
    }

    fun toList(): List<T> {
        val L: MutableList<T> = mutableListOf()
        var N: ListNode<T>? = head
        while (N != null) {
            L.add(N.value)
            N = N.next
        }
        return L
    }

    private fun _insert(x: T?, N: ListNode<T>?, count: Int): ListNode<T>? {
        if (count >= capacity)  return null
        if (x == null)          return N?.takeN(capacity - count)
        if (N == null)          return ListNode<T>(x, null)
        if (x >= N.value)       return ListNode<T>(x, _insert(null, N, count + 1))
        else                    return ListNode<T>(N.value, _insert(x, N.next, count + 1))
    }

    private data class ListNode<T>(val value: T, val next: ListNode<T>?) {
        fun takeN(n: Int): ListNode<T>? {
            if (n == 0)             return null
            else                    return ListNode<T>(value, next?.takeN(n - 1))
        }
    }
}
