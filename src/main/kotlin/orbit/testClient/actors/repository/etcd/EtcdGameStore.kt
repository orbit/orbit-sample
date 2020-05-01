package orbit.testClient.actors.repository.etcd

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.etcd.jetcd.ByteSequence
import io.etcd.jetcd.Client
import io.etcd.jetcd.options.GetOption
import kotlinx.coroutines.future.await
import orbit.testClient.actors.repository.GameRecord
import orbit.testClient.actors.repository.GameStore

class EtcdGameStore(url: String) : GameStore {

    val mapper = jacksonObjectMapper()
    val keyPrefix = "game"

    private val client = Client.builder().endpoints(url).build().kvClient

    private fun toKey(gameId: String): ByteSequence {
        return ByteSequence.from("$keyPrefix/${gameId}".toByteArray())
    }

    override suspend fun get(): List<GameRecord> {
        val key = ByteSequence.from("\u0000".toByteArray())

        val option = GetOption.newBuilder()
            .withSortField(GetOption.SortTarget.KEY)
            .withSortOrder(GetOption.SortOrder.DESCEND)
            .withPrefix(ByteSequence.from(keyPrefix.toByteArray()))
            .withRange(key)
            .build()

        return client.get(key, option).await().kvs.map { game ->
            mapper.readValue<GameRecord>(game.value.bytes)
        }
    }

    override suspend fun get(gameId: String): GameRecord? {
        val response = client.get(toKey(gameId)).await()
        return response.kvs.firstOrNull()?.value?.let {
            mapper.readValue<GameRecord>(it.bytes)
        }
    }

    override suspend fun put(game: GameRecord) {
        client.put(toKey(game.id), ByteSequence.from(mapper.writeValueAsBytes(game))).await()
    }
}
