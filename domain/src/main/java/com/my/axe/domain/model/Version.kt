// https://github.com/Ashinch/ReadYou/blob/main/app/src/main/java/me/ash/reader/data/model/general/Version.kt

package com.my.axe.domain.model

import com.my.axe.domain.model.release.Release

class Version(numbers: List<String>) : Comparable<Version> {

    private var major: Int = 0
    private var minor: Int = 0
    private var patch: Int = 0

    init {
        major = numbers.getOrNull(0)?.toIntOrNull() ?: 0
        minor = numbers.getOrNull(1)?.toIntOrNull() ?: 0
        patch = numbers.getOrNull(2)?.split("-")?.getOrNull(0)?.toIntOrNull() ?: 0
    }

    constructor() : this(listOf())
    constructor(string: String?) : this(string?.split(".") ?: listOf())

    override fun toString() = "$major.$minor.$patch"

    override operator fun compareTo(other: Version): Int = when {
        major > other.major -> 1
        major < other.major -> -1
        minor > other.minor -> 1
        minor < other.minor -> -1
        patch > other.patch -> 1
        patch < other.patch -> -1
        else -> 0
    }

    fun whetherNeedUpdate(current: Version): Boolean = this > current
}

fun String?.toVersion(): Version = Version(this?.trimStart('v'))

fun Release.toVersion(): Version = Version(tagName?.trimStart('v'))