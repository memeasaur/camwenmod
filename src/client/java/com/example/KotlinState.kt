package com.example

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.item.ItemStack

val supabaseClient: SupabaseClient = createSupabaseClient(
    supabaseUrl = "https://kshvmmimjsffefrfnuxl.supabase.co",
    supabaseKey = "sb_publishable_vyai78-ZMPXKNQR5Bm-ANA_l_1jiPBi"
) {
    install(Auth)
    install(Postgrest)
    install(Realtime)
}.auth.signInWith()

class VisiblePlayer(
    // TODO -> data?
    val tableEntry: VisiblePlayerTableEntry,
    val nullableInventorySlots: Array<ItemStack>?, // TODO -> 36
)

@Serializable
data class VisiblePlayerTableEntry(
    val uuid: String,
    @SerialName("location_x")
    val locationX: Float,
    @SerialName("location_y")
    val locationY: Float,
    @SerialName("location_z")
    val locationZ: Float,
    @SerialName("nullable_health")
    val health: Float?,
    @SerialName("team_color")
    val teamColor: String,
    @SerialName("health_consumables_used")
    val healthConsumablesUsed: Int
)

@Serializable
data class AccountInventorySlotsEntry(
    val uuid: String,
    val index: Int,
)

val partyVisibleAccountsChannel = supabaseClient.realtime.channel("party_visible_accounts")
val accountInventorySlotsChannel = supabaseClient.realtime.channel("account_inventory_slots")
val players: ArrayList<VisiblePlayer> = runBlocking { // TODO -> async
    val buffer = ArrayList<VisiblePlayerTableEntry>()
    val result = ArrayList<VisiblePlayer>()
    var flag = false
    run {
        partyVisibleAccountsChannel.postgresChangeFlow<PostgresAction>("public") {
            table = "party_visible_accounts"
        }.collect { new ->
            if (!flag) {
                buffer.add(new)
            } else {
                TODO;
            }
        }
        partyVisibleAccountsChannel.subscribe(true)
        result.addAll(
            supabaseClient.from("party_visible_accounts")
                .select()
                .decodeList<VisiblePlayerTableEntry>()
        )
    }
    run {
        accountInventorySlotsChannel.postgresChangeFlow<PostgresAction>("public") {
            table = "account_inventory_slots"
        }.collect { new ->
            TODO;
        }
        accountInventorySlotsChannel.subscribe(true)
        TODO // -> merge
        supabaseClient.from("account_inventory_slots")
            .select()
            .decodeList<AccountInventorySlotsEntry>()
    }
    TODO; // apply buffers
    flag = true // TODO ? this seems like it would still have a race condition
    return@runBlocking result
}