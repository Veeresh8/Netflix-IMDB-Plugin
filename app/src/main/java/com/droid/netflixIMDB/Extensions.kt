package com.droid.netflixIMDB

fun String.cleanTrailingChars(input: String): String {

    var index: Int = input.length - 1
    while (index > 0) {
        if (input[index] != '-') {
            break
        }
        index--
    }
    return input.substring(0, index + 1)

}