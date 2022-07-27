package io.github.nomisrev

/**
 * Splits a string given those delimiters.
 * [removedDelimiter] is removed after splitting,
 * whereas [keptDelimiter] is kept around.
 *
 * For example, if we split `this[0].thing`
 * we get back `listOf("this", "[0]", "thing")`.
 */
public fun String.splitTwice(
  removedDelimiter: String = ".",
  keptDelimiter: String = "["
): List<String> =
  split(removedDelimiter).flatMap {
    when {
      !it.contains('[') -> listOf(it)
      it.startsWith('[') ->
        // if it starts with '[', remove the first useless empty match
        it.split(keptDelimiter).drop(1).map { keptDelimiter + it }
      else -> {
        // we know there's a '[', and not at the beginning
        val (init, rest) = it.split(keptDelimiter, limit = 2)
        listOf(init) + rest.split(keptDelimiter).map { keptDelimiter + it }
      }
    }
  }

// remember, group capture starts at 1, not at 0!
public val MatchResult.firstMatch: String?
  get() = groupValues.getOrNull(1)

public fun matchIndexInBrackets(selector: String): Int? =
  Regex("""\[([0-9]+)\]""").matchEntire(selector)?.firstMatch?.toInt()

public fun matchIndicesInBrackets(selector: String): List<Int>? =
  Regex("""\[([0-9]+(,[0-9]+)*)\]""").matchEntire(selector)?.firstMatch?.let {
    it.split(',').map { it.toInt() }
  }

public fun matchStartIndex(selector: String): Int? =
  Regex("""\[([0-9]+):\]""").matchEntire(selector)?.firstMatch?.toInt()

public fun matchStartEndIndex(selector: String): Pair<Int, Int>? =
  Regex("""\[([0-9]+):([0-9]+)\]""").matchEntire(selector)?.groupValues?.let {
    it[1].toInt() to it[2].toInt()
  }

public fun matchNameInBrackets(selector: String): String? =
  Regex("""\['([^']*)'\]""").matchEntire(selector)?.firstMatch
