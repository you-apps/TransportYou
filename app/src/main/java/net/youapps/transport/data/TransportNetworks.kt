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
import de.schildbach.pte.SncbProvider
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
                DbProvider(
                    "{\"type\":\"AID\",\"aid\":\"n91dB8Z77MLdoR0K\"}",
                    "bdI8UVj40K5fvxwf".toByteArray(Charsets.UTF_8)
                )
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
            factory = { VrsProvider() }
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
            id = NetworkId.SNCB,
            name = "SNCB",
            country = Country.Belgium,
            continent = Continent.Europe,
            factory = {
                SncbProvider("{\"type\":\"AID\",\"aid\":\"sncb-mobi\"}")
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
}