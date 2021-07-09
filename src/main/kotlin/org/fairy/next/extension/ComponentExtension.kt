package org.fairy.next.extension

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.translation.GlobalTranslator
import java.util.*

val COMPONENT_GSON: GsonComponentSerializer = GsonComponentSerializer.builder()
    .build()

fun Component.toJsonString(locale: Locale = Locale.US) : String {
    return COMPONENT_GSON.serialize(
        GlobalTranslator.render(
            this,
            locale
        )
    )
}

fun readComponentFromJson(json: String): Component {
    return COMPONENT_GSON.deserialize(json)
}