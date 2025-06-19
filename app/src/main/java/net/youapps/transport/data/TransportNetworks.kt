package net.youapps.transport.data

import de.schildbach.pte.AvvAachenProvider
import de.schildbach.pte.AvvAugsburgProvider
import de.schildbach.pte.BartProvider
import de.schildbach.pte.BayernProvider
import de.schildbach.pte.BsvagProvider
import de.schildbach.pte.BvgProvider
import de.schildbach.pte.CHSearchProvider
import de.schildbach.pte.CmtaProvider
import de.schildbach.pte.DbProvider
import de.schildbach.pte.DingProvider
import de.schildbach.pte.DsbProvider
import de.schildbach.pte.DubProvider
import de.schildbach.pte.GvhProvider
import de.schildbach.pte.InvgProvider
import de.schildbach.pte.KvvProvider
import de.schildbach.pte.LinzProvider
import de.schildbach.pte.LuProvider
import de.schildbach.pte.MerseyProvider
import de.schildbach.pte.MvgProvider
import de.schildbach.pte.MvvProvider
import de.schildbach.pte.NasaProvider
import de.schildbach.pte.NetworkId
import de.schildbach.pte.NetworkProvider
import de.schildbach.pte.NsProvider
import de.schildbach.pte.NvbwProvider
import de.schildbach.pte.NvvProvider
import de.schildbach.pte.OebbProvider
import de.schildbach.pte.RtaChicagoProvider
import de.schildbach.pte.SeProvider
import de.schildbach.pte.ShProvider
import de.schildbach.pte.SydneyProvider
import de.schildbach.pte.TlemProvider
import de.schildbach.pte.VbbProvider
import de.schildbach.pte.VblProvider
import de.schildbach.pte.VbnProvider
import de.schildbach.pte.VgnProvider
import de.schildbach.pte.VgsProvider
import de.schildbach.pte.VmobilProvider
import de.schildbach.pte.VmtProvider
import de.schildbach.pte.VmvProvider
import de.schildbach.pte.VrnProvider
import de.schildbach.pte.VrrProvider
import de.schildbach.pte.VrsProvider
import de.schildbach.pte.VvmProvider
import de.schildbach.pte.VvoProvider
import de.schildbach.pte.VvsProvider
import de.schildbach.pte.VvtProvider
import de.schildbach.pte.VvvProvider
import de.schildbach.pte.WienProvider
import de.schildbach.pte.ZvvProvider
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

enum class Country {
    Germany,
    Austria,
    France,
    Australia,
    Denmark,
    GreatBritain,
    Belgium,
    Switzerland,
    Liechtenstein,
    Luxembourg,
    USA,
    Netherlands,
    Sweden,
    UnitedArabEmirates
}

enum class Continent {
    Europe,
    Africa,
    Asia,
    NorthAmerica,
    CentralAmerica,
    SouthAmerica,
    Oceania
}

class TransportNetwork(
    val id: NetworkId,
    val name: String,
    val country: Country,
    val continent: Continent,
    val factory: () -> NetworkProvider
)

object TransportNetworks {
    val networks = arrayOf(
        TransportNetwork(
            id = NetworkId.DB,
            name = "Deutsche Bahn",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = {
                DbProvider()
            }
        ),
        TransportNetwork(
            id = NetworkId.BVG,
            name = "Berliner Verkehrsbetriebe",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { BvgProvider("{\"aid\":\"1Rxs112shyHLatUX4fofnmdxK\",\"type\":\"AID\"}") }
        ),
        TransportNetwork(
            id = NetworkId.NVV,
            name = "NVV/RMV (Hesse)",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { NvvProvider("{\"type\":\"AID\",\"aid\":\"Kt8eNOH7qjVeSxNA\"}") }
        ),
        TransportNetwork(
            id = NetworkId.BVG,
            name = "BVG",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = {
                BvgProvider("{\"aid\":\"1Rxs112shyHLatUX4fofnmdxK\",\"type\":\"AID\"}")
            }
        ),
        TransportNetwork(
            id = NetworkId.VBB,
            name = "VBB",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = {
                VbbProvider("{\"type\":\"AID\",\"aid\":\"hafas-vbb-apps\"}", "RCTJM2fFxFfxxQfI".toByteArray(Charsets.UTF_8))
            }
        ),
        TransportNetwork(
            id = NetworkId.BAYERN,
            name = "Bayern",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { BayernProvider() }
        ),
        TransportNetwork(
            id = NetworkId.AVV_AUGSBURG,
            name = "AVV Augsburg",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = {
                AvvAugsburgProvider("{\"type\":\"AID\",\"aid\":\"jK91AVVZU77xY5oH\"}")
            }
        ),
        TransportNetwork(
            id = NetworkId.MVV,
            name = "MVV",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { MvvProvider() }
        ),
        TransportNetwork(
            id = NetworkId.INVG,
            name = "INVG",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = {
                InvgProvider("{\"type\":\"AID\",\"aid\":\"GITvwi3BGOmTQ2a5\"}", "ERxotxpwFT7uYRsI".toByteArray(Charsets.UTF_8))
            }
        ),
        TransportNetwork(
            id = NetworkId.VBN,
            name = "VBN",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = {
                VbnProvider("{\"type\":\"AID\",\"aid\":\"rnOHBWhesvc7gFkd\"}", "SP31mBufSyCLmNxp".toByteArray(Charsets.UTF_8))
            }
        ),
        TransportNetwork(
            id = NetworkId.SH,
            name = "nah.sh",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = {
                ShProvider("{\"type\":\"AID\",\"aid\":\"r0Ot9FLFNAFxijLW\"}")
            }
        ),
        TransportNetwork(
            id = NetworkId.AVV_AACHEN,
            name = "AVV Aachen",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = {
                AvvAachenProvider("{\"type\":\"AID\",\"aid\":\"4vV1AcH3N511icH\"}")
            }
        ),
        TransportNetwork(
            id = NetworkId.VGN,
            name = "VGN",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { VgnProvider() }
        ),
        TransportNetwork(
            id = NetworkId.VVM,
            name = "VVM",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { VvmProvider() }
        ),
        TransportNetwork(
            id = NetworkId.VMV,
            name = "VMV",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { VmvProvider() }
        ),
        TransportNetwork(
            id = NetworkId.GVH,
            name = "GVH",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { GvhProvider() }
        ),
        TransportNetwork(
            id = NetworkId.BSVAG,
            name = "BSVAG",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { BsvagProvider() }
        ),
        TransportNetwork(
            id = NetworkId.VVO,
            name = "VVO",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { VvoProvider() }
        ),
        TransportNetwork(
            id = NetworkId.NASA,
            name = "NASA",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { NasaProvider("{\"aid\":\"nasa-apps\",\"type\":\"AID\"}") }
        ),
        TransportNetwork(
            id = NetworkId.VRR,
            name = "VRR",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { VrrProvider() }
        ),
        TransportNetwork(
            id = NetworkId.MVG,
            name = "MVG",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { MvgProvider() }
        ),
        TransportNetwork(
            id = NetworkId.VRN,
            name = "VRN",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { VrnProvider() }
        ),
        TransportNetwork(
            id = NetworkId.VVS,
            name = "VVS Stuttgart",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { VvsProvider("http://www2.vvs.de/oeffi/".toHttpUrlOrNull()) }
        ),
        TransportNetwork(
            id = NetworkId.DING,
            name = "DING",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { DingProvider() }
        ),
        TransportNetwork(
            id = NetworkId.KVV,
            name = "KVV Karlsruhe",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { KvvProvider("https://projekte.kvv-efa.de/oeffi/".toHttpUrlOrNull()) }
        ),
        TransportNetwork(
            id = NetworkId.NVBW,
            name = "NVBW",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { NvbwProvider() }
        ),
        TransportNetwork(
            id = NetworkId.VVV,
            name = "VVV",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { VvvProvider() }
        ),
        TransportNetwork(
            id = NetworkId.VGS,
            name = "VGS",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = {
                VgsProvider(
                    "{\"type\":\"AID\",\"aid\":\"51XfsVqgbdA6oXzHrx75jhlocRg6Xe\"}",
                    "HJtlubisvxiJxss".toByteArray(Charsets.UTF_8)
                )
            }
        ),
        TransportNetwork(
            id = NetworkId.VRS,
            name = "VRS",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { VrsProvider(VRS_CLIENT_CERTIFICATE) }
        ),
        TransportNetwork(
            id = NetworkId.VMT,
            name = "VMT",
            country = Country.Germany,
            continent = Continent.Europe,
            factory = { VmtProvider("{\"aid\":\"vj5d7i3g9m5d7e3\",\"type\":\"AID\"}") }
        ),
        TransportNetwork(
            id = NetworkId.OEBB,
            name = "OEBB",
            country = Country.Austria,
            continent = Continent.Europe,
            factory = {
                OebbProvider("{\"type\":\"AID\",\"aid\":\"OWDL4fE4ixNiPBBm\"}")
            }
        ),
        TransportNetwork(
            id = NetworkId.LINZ,
            name = "Linz",
            country = Country.Austria,
            continent = Continent.Europe,
            factory = {
                LinzProvider()
            }
        ),
        TransportNetwork(
            id = NetworkId.VVT,
            name = "VVT",
            country = Country.Austria,
            continent = Continent.Europe,
            factory = {
                VvtProvider("{\"type\":\"AID\",\"aid\":\"wf7mcf9bv3nv8g5f\"}")
            }
        ),
        TransportNetwork(
            id = NetworkId.WIEN,
            name = "Wien",
            country = Country.Austria,
            continent = Continent.Europe,
            factory = {
                WienProvider()
            }
        ),
        TransportNetwork(
            id = NetworkId.VMOBIL,
            name = "Vmobil",
            country = Country.Liechtenstein,
            continent = Continent.Europe,
            factory = {
                VmobilProvider(VAO)
            },
        ),
        TransportNetwork(
            id = NetworkId.SEARCHCH,
            name = "SBB",
            country = Country.Switzerland,
            continent = Continent.Europe,
            factory = {
                CHSearchProvider()
            }
        ),
        TransportNetwork(
            id = NetworkId.VBL,
            name = "VBL",
            country = Country.Switzerland,
            continent = Continent.Europe,
            factory = {
                VblProvider()
            }
        ),
        TransportNetwork(
            id = NetworkId.ZVV,
            name = "ZVV",
            country = Country.Switzerland,
            continent = Continent.Europe,
            factory = {
                ZvvProvider("{\"type\":\"AID\",\"aid\":\"hf7mcf9bv3nv8g5f\"}")
            }
        ),
        TransportNetwork(
            id = NetworkId.LU,
            name = "Luxembourg",
            country = Country.Luxembourg,
            continent = Continent.Europe,
            factory = {
                LuProvider("{\"type\":\"AID\",\"aid\":\"Aqf9kNqJLjxFx6vv\"}")
            }
        ),
        TransportNetwork(
            id = NetworkId.NS,
            name = "NS",
            country = Country.Netherlands,
            continent = Continent.Europe,
            factory = {
                NsProvider()
            }
        ),
        TransportNetwork(
            id = NetworkId.DSB,
            name = "Danish State Railways",
            country = Country.Denmark,
            continent = Continent.Europe,
            factory = {
                DsbProvider("{\"type\":\"AID\",\"aid\":\"irkmpm9mdznstenr-android\"}")
            }
        ),
        TransportNetwork(
            id = NetworkId.SE,
            name = "Swedish Railways",
            country = Country.Sweden,
            continent = Continent.Europe,
            factory = {
                SeProvider("{\"type\":\"AID\",\"aid\":\"h5o3n7f4t2m8l9x1\"}")
            }
        ),
        TransportNetwork(
            id = NetworkId.TLEM,
            name = "TLEM",
            country = Country.GreatBritain,
            continent = Continent.Europe,
            factory = {
                TlemProvider()
            }
        ),
        TransportNetwork(
            id = NetworkId.MERSEY,
            name = "Merseyside",
            country = Country.GreatBritain,
            continent = Continent.Europe,
            factory = {
                MerseyProvider()
            }
        ),
        TransportNetwork(
            id = NetworkId.RTACHICAGO,
            name = "Chicago Transit",
            country = Country.USA,
            continent = Continent.NorthAmerica,
            factory = {
                RtaChicagoProvider()
            }
        ),
        TransportNetwork(
            id = NetworkId.BART,
            name = "BART",
            country = Country.USA,
            continent = Continent.NorthAmerica,
            factory = {
                BartProvider("{\"type\":\"AID\",\"aid\":\"kEwHkFUCIL500dym\"}")
            }
        ),
        TransportNetwork(
            id = NetworkId.CMTA,
            name = "CMTA",
            country = Country.USA,
            continent = Continent.NorthAmerica,
            factory = {
                CmtaProvider("{\"type\":\"AID\",\"aid\":\"web9j2nak29uz41irb\"}")
            }
        ),
        TransportNetwork(
            id = NetworkId.DUB,
            name = "Dubai",
            country = Country.UnitedArabEmirates,
            continent = Continent.Asia,
            factory = {
                DubProvider()
            }
        ),
        TransportNetwork(
            id = NetworkId.SYDNEY,
            name = "Sydney",
            country = Country.Australia,
            continent = Continent.Oceania,
            factory = {
                SydneyProvider()
            }
        )
    )

    private const val VAO = "{\"aid\":\"hf7mcf9bv3nv8g5f\",\"pw\":\"87a6f8ZbnBih32\",\"type\":\"USER\",\"user\":\"mobile\"}"
    @OptIn(ExperimentalEncodingApi::class)
    private val VRS_CLIENT_CERTIFICATE: ByteArray? = Base64.decode(
        "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"
    )
}