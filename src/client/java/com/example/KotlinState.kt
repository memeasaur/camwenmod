package com.example

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

val supabaseClient: SupabaseClient = createSupabaseClient(
    supabaseUrl = "https://kshvmmimjsffefrfnuxl.supabase.co",
    supabaseKey = "sb_publishable_vyai78-ZMPXKNQR5Bm-ANA_l_1jiPBi"
) {
    install(Auth)
    install(Postgrest)
    install(Realtime)
}