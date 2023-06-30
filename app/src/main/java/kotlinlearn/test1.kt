package kotlinlearn

fun main() {

    val m = mapOf<String, Int>(
        "0" to 0,
        "3" to 3,
    )

    val a = m.toList().sortedWith { pairA, pairB -> pairA.second - pairB.second }

    println(a)
}