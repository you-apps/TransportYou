package net.youapps.transport.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import net.youapps.transport.ProtobufSavedLocations
import java.io.InputStream
import java.io.OutputStream

object SavedLocationsSerializer : Serializer<ProtobufSavedLocations> {
    override val defaultValue: ProtobufSavedLocations = ProtobufSavedLocations.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ProtobufSavedLocations {
        try {
            return ProtobufSavedLocations.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: ProtobufSavedLocations,
        output: OutputStream) = t.writeTo(output)
}

val Context.savedLocationsDataStore: DataStore<ProtobufSavedLocations> by dataStore(
    fileName = "saved_locations.pb",
    serializer = SavedLocationsSerializer
)