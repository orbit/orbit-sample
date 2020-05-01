package orbit.testClient.actors.repository.etcd

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.etcd.jetcd.ByteSequence
import io.etcd.jetcd.Client
import io.etcd.jetcd.options.GetOption
import kotlinx.coroutines.future.await
import orbit.testClient.actors.repository.PlayerRecord
import orbit.testClient.actors.repository.PlayerStore

class EtcdPlayerStore(
    val url: String = System.getenv("STORE_ADDRESS") ?: "http://localhost:2379"
) : PlayerStore {

    val mapper = jacksonObjectMapper()
    val keyPrefix = "game"

    private val client = Client.builder().endpoints(url).build().kvClient

    private fun toKey(gameId: String): ByteSequence {
        return ByteSequence.from("$keyPrefix/${gameId}".toByteArray())
    }

    override suspend fun get(): List<PlayerRecord> {
        val key = ByteSequence.from("\u0000".toByteArray())

        val option = GetOption.newBuilder()
            .withSortField(GetOption.SortTarget.KEY)
            .withSortOrder(GetOption.SortOrder.DESCEND)
            .withPrefix(ByteSequence.from(keyPrefix.toByteArray()))
            .withRange(key)
            .build()

        return client.get(key, option).await().kvs.map { game ->
            mapper.readValue<PlayerRecord>(game.value.bytes)
        }
    }

    override suspend fun get(id: String): PlayerRecord? {
        val response = client.get(toKey(id)).await()
        return response.kvs.firstOrNull()?.value?.let {
            mapper.readValue<PlayerRecord>(it.bytes)
        }
    }

    override suspend fun put(player: PlayerRecord) {
        client.put(toKey(player.id), ByteSequence.from(mapper.writeValueAsBytes(player))).await()
    }
}
