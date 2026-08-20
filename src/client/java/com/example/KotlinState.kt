//package com.example
//
//import io.github.jan.supabase.SupabaseClient
//import io.github.jan.supabase.auth.Auth
//import io.github.jan.supabase.auth.auth
//import io.github.jan.supabase.createSupabaseClient
//import io.github.jan.supabase.postgrest.Postgrest
//import io.github.jan.supabase.postgrest.from
//import io.github.jan.supabase.realtime.PostgresAction
//import io.github.jan.supabase.realtime.Realtime
//import io.github.jan.supabase.realtime.channel
//import io.github.jan.supabase.realtime.postgresChangeFlow
//import io.github.jan.supabase.realtime.realtime
//import kotlinx.coroutines.channels.Channel
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.runBlocking
//import kotlinx.serialization.SerialName
//import kotlinx.serialization.Serializable
//import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
//import net.minecraft.item.ItemStack
//import java.util.UUID
//
//val supabaseClient: SupabaseClient = createSupabaseClient(
//    supabaseUrl = "https://kshvmmimjsffefrfnuxl.supabase.co",
//    supabaseKey = "sb_publishable_vyai78-ZMPXKNQR5Bm-ANA_l_1jiPBi"
//) {
//    install(Auth)
//    install(Postgrest)
//    install(Realtime)
//}.auth.signInWith()
//
//// TODO -> data?
//class VisiblePlayer(
//    var tableEntry: VisiblePlayerTableEntry,
//) {
//    val inventorySlots: Array<ItemStack> = Array(36) { ItemStack.EMPTY }
//}
//
//@Serializable
//data class VisiblePlayerTableEntry(
//    val uuid: UUID,
//    @SerialName("location_x")
//    val locationX: Float,
//    @SerialName("location_y")
//    val locationY: Float,
//    @SerialName("location_z")
//    val locationZ: Float,
//    @SerialName("nullable_health")
//    val health: Float?,
//    @SerialName("team_color")
//    val teamColor: String,
//    @SerialName("health_consumables_used")
//    val healthConsumablesUsed: Int
//)
//
//@Serializable
//data class AccountInventorySlotsEntry(
//    val uuid: String,
//    val index: Int,
//)
//
//val players: ArrayList<VisiblePlayer> = runBlocking { // TODO -> async
//    val result = ArrayList<VisiblePlayer>()
//    run {
//        val buffer = Channel<PostgresAction>(Channel.UNLIMITED)
//        val partyVisibleAccountsChannel = supabaseClient.realtime.channel("party_visible_accounts")
//        launch {
//            partyVisibleAccountsChannel.postgresChangeFlow<PostgresAction>("public") {
//                table = "party_visible_accounts"
//            }
//                .collect { action -> buffer.send(action) }
//        }
//        partyVisibleAccountsChannel.subscribe(true)
//        result.addAll(
//            supabaseClient.from("party_visible_accounts")
//                .select()
//                .decodeList<VisiblePlayerTableEntry>()
//                .map { each -> VisiblePlayer(each) }
//        )
////        {
////            when (action) {
////                is PostgresAction.Insert -> {
////                    val new = VisiblePlayer(
////                        action.decodeRecord<VisiblePlayerTableEntry>(),
////                        null
////                    )
////                    players.add(new)
////                }
////
////                is PostgresAction.Delete -> {
////                    val index = players.indexOfFirst {
////                        it.tableEntry.uuid == action.decodeOldRecord<VisiblePlayerTableEntry>().uuid
////                    }
////                    players.removeAt(index)
////                }
////
////                is PostgresAction.Update -> {
////                    val new = action.decodeRecord<VisiblePlayerTableEntry>()
////                    val index = players.indexOfFirst {
////                        it.tableEntry.uuid == new.uuid
////                    }
////                    players[index].tableEntry = new
////                }
////
////                else -> {
////                    // TODO -> crash?
////                    Unit
////                }
////            }
////        }
//        TODO; // -> launch task on main thread that consumes the channel
//    }
//    run {
//        val buffer = Channel<PostgresAction>(Channel.UNLIMITED)
//        val accountInventorySlotsChannel = supabaseClient.realtime.channel("account_inventory_slots")
//        launch {
//            accountInventorySlotsChannel.postgresChangeFlow<PostgresAction>("public") {
//                table = "account_inventory_slots"
//            }
//                .collect { new -> buffer.send(new) }
//        }
//        accountInventorySlotsChannel.subscribe(true)
//        supabaseClient.from("account_inventory_slots")
//            .select()
//            .decodeList<AccountInventorySlotsEntry>()
//            .forEach { each ->  // TODO -> supabase groupBy?
//                {
//                    val lookupTable =;
//                }
//            }
//        ClientTickEvents.START_CLIENT_TICK.register { client ->
//            {
//                while (!buffer.isEmpty) {
//                    TODO;
//                }
//            }
//        }
//    }
//    return@runBlocking result
//}