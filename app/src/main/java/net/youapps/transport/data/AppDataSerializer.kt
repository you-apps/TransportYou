package net.youapps.transport.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import net.youapps.transport.ProtobufAppData
import java.io.InputStream
import java.io.OutputStream

object AppDataSerializer : Serializer<ProtobufAppData> {
    override val defaultValue: ProtobufAppData = ProtobufAppData.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ProtobufAppData {
        try {
            return ProtobufAppData.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: ProtobufAppData,
        output: OutputStream) = t.writeTo(output)
}

val Context.appData: DataStore<ProtobufAppData> by dataStore(
    fileName = "app_data.pb",
    serializer = AppDataSerializer
)