package com.example.apartapp.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class AdminListingRequest(
    val title: String,
    val description: String?,
    val price: String,
    val district: String?,
    val rooms: Int?,
    val cityName: String,
    val sourceName: String,
    val publicationDate: String?,
    val images: List<String>? = null // Base64 строки
)

object ByteArraySerializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ByteArray", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ByteArray) {
        encoder.encodeString(value.joinToString(",") { it.toString() })
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        return decoder.decodeString().split(",").map { it.toByte() }.toByteArray()
    }
} 