package org.fairy.next.extension

import com.google.gson.JsonElement
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.translation.GlobalTranslator
import java.util.*

val COMPONENT_GSON: GsonComponentSerializer = GsonComponentSerializer.builder()
    .build()

fun Component.toJsonObject(locale: Locale = Locale.US) : JsonElement {
    return COMPONENT_GSON.serializeToTree(
        GlobalTranslator.render(
            this,
            locale
        )
    )
}

fun readComponentFromJson(json: String): Component {
    return COMPONENT_GSON.deserialize(json)
}