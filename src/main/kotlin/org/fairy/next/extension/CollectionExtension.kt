package org.fairy.next.extension

fun <K, V> Map<K, V>.isMutable() : Boolean = this is MutableMap<K, V>

fun <K, V> Map<K, V>.mutable() : MutableMap<K, V> = this as MutableMap<K, V>