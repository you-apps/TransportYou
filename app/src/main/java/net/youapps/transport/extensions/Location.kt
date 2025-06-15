package net.youapps.transport.extensions

import de.schildbach.pte.dto.Location

fun Location.displayName() = listOfNotNull(name, place).joinToString(", ")