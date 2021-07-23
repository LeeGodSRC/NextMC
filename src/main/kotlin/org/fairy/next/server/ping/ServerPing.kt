package org.fairy.next.server.ping

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.kyori.adventure.text.Component
import org.fairy.next.extension.log
import org.fairy.next.extension.toJsonObject
import java.util.*

val gson = Gson()

data class ServerPing(
    val component: Component,
    val favicon: String?,
    val serverName: String,
    val protocol: Int,
    val max: Int,
    val online: Int,
    val samples: List<ServerSample>
) {

    fun toJsonString(locale: Locale): String {
        val jsonObject = JsonObject()
        jsonObject.add("description", this.component.toJsonObject(locale))

        val versionJsonObject = JsonObject()
        versionJsonObject.addProperty("name", serverName)
        versionJsonObject.addProperty("protocol", protocol)
        jsonObject.add("version", versionJsonObject)

        val playerJsonObject = JsonObject()
        playerJsonObject.addProperty("max", this.max)
        playerJsonObject.addProperty("online", this.online)
        if (samples.isNotEmpty()) {
            val sampleArray = JsonArray()
            samples.forEach {
                val sampleObject = JsonObject()
                sampleObject.addProperty("id", it.uuid)
                sampleObject.addProperty("name", it.name)
            }
            playerJsonObject.add("sample", sampleArray)
        }
        jsonObject.add("players", playerJsonObject)
        jsonObject.addProperty("favicon", this.favicon)
        val toJson = GsonBuilder().setPrettyPrinting().create().toJson(jsonObject)
        log.info(toJson)
        return gson.toJson(jsonObject)
    }

}

data class ServerSample(val uuid: String, val name: String)