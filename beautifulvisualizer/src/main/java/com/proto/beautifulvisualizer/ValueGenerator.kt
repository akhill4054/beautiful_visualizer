package com.proto.beautifulvisualizer

/**
 * Generates n random looking values from m provided values.
 * @param m, values to be provided.
 * @param n, values to be generated.
 * */
class ValueGenerator(
    val m: Int,
    val n: Int
) {
    private val vIndexMapping = IntArray(n)

    val values = IntArray(n)

    init {
        for (i in 0 until n) vIndexMapping[i] = i

        // Shuffling the array using 'Fisher–Yates shuffle Algorithm'.
        for (i in vIndexMapping.size - 1 downTo 1) {
            val j = (0..i).random()
            vIndexMapping[i] = j
        }
    }

//    private fun generateValues(provided: IntArray) {
//        when {
//            m == n -> {
//                for (i in 0 until n) values[vIndexMapping[i]] = provided[i]
//            }
//            n > m -> {
//                val groupSize = mLocalProvidedValues.size / m
//
//                for (i in mLocalProvidedValues.indices) {
//                    val start = i * groupSize
//                    val end = if (i == m - 1) {
//                        mLocalProvidedValues.size - 1
//                    } else {
//                        start + groupSize - 1
//                    }
//
//                    var avg = 0F
//                    for (j in start..end) avg += mLocalProvidedValues[j]
//                    avg /= groupSize
//
//                    mValues[i] = avg
//                }
//            }
//            else -> { // mLocalProvidedValues > size(values)/size(providedValues)
//                val groupSize = mValues.size / mLocalProvidedValues.size
//
//                // Updating values at propagation start indices
//                for (i in mLocalProvidedValues.indices) {
//                    mValues[i * groupSize] = mLocalProvidedValues[i]
//
//                    val start = i * groupSize + 1
//                    val end = if (i == mLocalProvidedValues.size - 1) {
//                        mValues.size - 1
//                    } else {
//                        (i + 1) * groupSize - 1
//                    }
//
//                    var off = 1
//                    val mOff = end - start + 1
//                    val v = mValues[start - 1]
//                    val m = -3 * v / (4 * mOff)
//
//                    for (j in start..end) {
//                        mValues[j] = m * off++ + v
//                    }
//                }
//            }
//        }
//    }
}