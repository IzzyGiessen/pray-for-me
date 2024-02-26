package com.onlysaints.prayforme.classes

import java.io.Serializable

class Prayer(var text: String = "", private val id: String = "", private val count: Int = 0,
             private val postTime: Long = 0): Serializable, Comparable<Prayer> {
    fun id() = id
    fun count() = count
    fun postTime() = postTime

    fun changeCount(count: Int) = Prayer(text, id, count, postTime)

    override fun compareTo(other: Prayer): Int = -postTime.compareTo(other.postTime)
}