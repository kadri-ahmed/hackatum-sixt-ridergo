package utils

import dto.LocationDto
import models.TripPurpose
import models.TripType
import models.UserContext

/**
 * Utility to extract user context information from chat messages
 */
object ChatContextExtractor {
    
    /**
     * Extract user context from a message
     */
    fun extractFromMessage(message: String): UserContext {
        val lowerMessage = message.lowercase()
        
        // Extract traveler count
        val travelerCount = extractTravelerCount(lowerMessage)
        
        // Extract trip purpose
        val tripPurpose = extractTripPurpose(lowerMessage)
        
        // Extract location
        val location = extractLocation(lowerMessage)
        
        // Extract dates
        val dates = extractDates(lowerMessage)
        
        // Extract trip type
        val tripType = extractTripType(lowerMessage)
        
        // Extract budget
        val budget = extractBudget(lowerMessage)
        
        // Extract preferences
        val preferences = extractPreferences(lowerMessage)
        
        // Extract special needs
        val specialNeeds = extractSpecialNeeds(lowerMessage)
        
        return UserContext(
            travelerCount = travelerCount,
            tripPurpose = tripPurpose,
            location = location,
            startDate = dates.first,
            endDate = dates.second,
            tripType = tripType,
            budget = budget,
            preferences = preferences,
            specialNeeds = specialNeeds
        )
    }
    
    private fun extractTravelerCount(message: String): Int? {
        // Patterns: "2 people", "for 3", "with my family", "solo", "alone"
        val patterns = listOf(
            Regex("""(\d+)\s*(?:people|persons|travelers|passengers|guests)"""),
            Regex("""(?:for|with)\s+(\d+)"""),
            Regex("""(\d+)\s*(?:of us|travelers)"""),
            Regex("""solo|alone|just me""").let { if (it.find(message) != null) 1 else null },
            Regex("""couple|two of us""").let { if (it.find(message) != null) 2 else null },
            Regex("""family|with kids|with children""").let { if (it.find(message) != null) null else null } // Too vague
        )
        
        for (pattern in patterns) {
            when (pattern) {
                is Regex -> {
                    val match = pattern.find(message)
                    if (match != null) {
                        val number = match.groupValues.getOrNull(1)?.toIntOrNull()
                        if (number != null && number > 0 && number <= 9) return number
                    }
                }
                is Int -> return pattern
            }
        }
        
        // Try to find standalone numbers that might indicate count
        val numberPattern = Regex("""\b([1-9])\b""")
        val matches = numberPattern.findAll(message).toList()
        if (matches.size == 1) {
            val num = matches[0].value.toIntOrNull()
            if (num != null && num in 1..9) return num
        }
        
        return null
    }
    
    private fun extractTripPurpose(message: String): TripPurpose? {
        return when {
            message.contains(Regex("""\b(business|work|meeting|conference|corporate)\b""")) -> TripPurpose.BUSINESS
            message.contains(Regex("""\b(vacation|holiday|trip|travel|tour|sightseeing|adventure)\b""")) -> TripPurpose.VACATION
            message.contains(Regex("""\b(family|kids|children|with my|family trip)\b""")) -> TripPurpose.FAMILY
            else -> null
        }
    }
    
    private fun extractLocation(message: String): LocationDto? {
        // Common city patterns
        val cityPatterns = listOf(
            Regex("""\b(munich|munchen|münchen)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(berlin)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(frankfurt)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(hamburg)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(cologne|koln|köln)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(stuttgart)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(düsseldorf|dusseldorf)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(dresden)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(leipzig)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(hanover|hannover)\b""", RegexOption.IGNORE_CASE),
        )
        
        val cityCountryMap = mapOf(
            "munich" to "Germany",
            "munchen" to "Germany",
            "münchen" to "Germany",
            "berlin" to "Germany",
            "frankfurt" to "Germany",
            "hamburg" to "Germany",
            "cologne" to "Germany",
            "koln" to "Germany",
            "köln" to "Germany",
            "stuttgart" to "Germany",
            "düsseldorf" to "Germany",
            "dusseldorf" to "Germany",
            "dresden" to "Germany",
            "leipzig" to "Germany",
            "hanover" to "Germany",
            "hannover" to "Germany"
        )
        
        for (pattern in cityPatterns) {
            val match = pattern.find(message)
            if (match != null) {
                val cityName = match.value.lowercase()
                val country = cityCountryMap[cityName] ?: "Germany"
                // Capitalize first letter
                val capitalizedCity = if (cityName.isNotEmpty()) {
                    cityName[0].uppercaseChar() + cityName.substring(1)
                } else {
                    cityName
                }
                return LocationDto(
                    city = capitalizedCity,
                    country = country
                )
            }
        }
        
        // Airport codes
        val airportPattern = Regex("""\b([A-Z]{3})\b""")
        val airportMatch = airportPattern.find(message.uppercase())
        if (airportMatch != null) {
            val code = airportMatch.value
            // Common German airport codes
            val airportCityMap = mapOf(
                "MUC" to Pair("Munich", "Germany"),
                "BER" to Pair("Berlin", "Germany"),
                "FRA" to Pair("Frankfurt", "Germany"),
                "HAM" to Pair("Hamburg", "Germany"),
                "CGN" to Pair("Cologne", "Germany"),
                "STR" to Pair("Stuttgart", "Germany"),
                "DUS" to Pair("Düsseldorf", "Germany"),
                "DRS" to Pair("Dresden", "Germany"),
                "LEJ" to Pair("Leipzig", "Germany"),
                "HAJ" to Pair("Hanover", "Germany")
            )
            airportCityMap[code]?.let { (city, country) ->
                return LocationDto(city = city, country = country, airportCode = code)
            }
        }
        
        return null
    }
    
    private fun extractDates(message: String): Pair<String?, String?> {
        // ISO date patterns: YYYY-MM-DD, DD/MM/YYYY, DD.MM.YYYY, "next week", "tomorrow", etc.
        val isoPattern = Regex("""(\d{4})-(\d{2})-(\d{2})""")
        val slashPattern = Regex("""(\d{1,2})/(\d{1,2})/(\d{4})""")
        val dotPattern = Regex("""(\d{1,2})\.(\d{1,2})\.(\d{4})""")
        
        val dates = mutableListOf<String>()
        
        // Try ISO format
        isoPattern.findAll(message).forEach { match ->
            dates.add(match.value)
        }
        
        // Try slash format (DD/MM/YYYY)
        slashPattern.findAll(message).forEach { match ->
            val (day, month, year) = match.destructured
            dates.add("$year-$month-$day")
        }
        
        // Try dot format (DD.MM.YYYY)
        dotPattern.findAll(message).forEach { match ->
            val (day, month, year) = match.destructured
            dates.add("$year-$month-$day")
        }
        
        return when (dates.size) {
            0 -> Pair(null, null)
            1 -> Pair(dates[0], null)
            else -> Pair(dates[0], dates[1])
        }
    }
    
    private fun extractTripType(message: String): TripType? {
        return when {
            message.contains(Regex("""\b(one.?way|single|one direction)\b""")) -> TripType.ONE_WAY
            message.contains(Regex("""\b(round.?trip|return|roundtrip)\b""")) -> TripType.ROUND_TRIP
            else -> null
        }
    }
    
    private fun extractBudget(message: String): Double? {
        // Patterns: "€100", "100 euros", "budget of 200", "under 150"
        val patterns = listOf(
            Regex("""(?:€|euro|eur)\s*(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE),
            Regex("""(\d+(?:\.\d+)?)\s*(?:€|euro|eur|euros)""", RegexOption.IGNORE_CASE),
            Regex("""(?:budget|max|maximum|up to|under|below)\s*(?:of|is|:)?\s*(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
        )
        
        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                val amount = match.groupValues.getOrNull(1)?.toDoubleOrNull()
                if (amount != null && amount > 0) return amount
            }
        }
        
        return null
    }
    
    private fun extractPreferences(message: String): List<String> {
        val preferences = mutableListOf<String>()
        
        if (message.contains(Regex("""\b(luxury|premium|high.?end|upscale)\b""", RegexOption.IGNORE_CASE))) {
            preferences.add("luxury")
        }
        if (message.contains(Regex("""\b(eco.?friendly|electric|hybrid|green|sustainable)\b""", RegexOption.IGNORE_CASE))) {
            preferences.add("eco-friendly")
        }
        if (message.contains(Regex("""\b(spacious|roomy|large|big|comfortable)\b""", RegexOption.IGNORE_CASE))) {
            preferences.add("spacious")
        }
        if (message.contains(Regex("""\b(sporty|fast|performance|sports car)\b""", RegexOption.IGNORE_CASE))) {
            preferences.add("sporty")
        }
        if (message.contains(Regex("""\b(compact|small|economy|cheap|affordable)\b""", RegexOption.IGNORE_CASE))) {
            preferences.add("economy")
        }
        if (message.contains(Regex("""\b(suv|4wd|awd|off.?road|mountain|all.?terrain)\b""", RegexOption.IGNORE_CASE))) {
            preferences.add("suv")
        }
        
        return preferences
    }
    
    private fun extractSpecialNeeds(message: String): List<String> {
        val needs = mutableListOf<String>()
        
        if (message.contains(Regex("""\b(wheelchair|accessible|disability)\b""", RegexOption.IGNORE_CASE))) {
            needs.add("wheelchair accessible")
        }
        if (message.contains(Regex("""\b(child.?seat|baby.?seat|car.?seat|infant)\b""", RegexOption.IGNORE_CASE))) {
            needs.add("child seat")
        }
        if (message.contains(Regex("""\b(gps|navigation|navi)\b""", RegexOption.IGNORE_CASE))) {
            needs.add("gps")
        }
        if (message.contains(Regex("""\b(automatic|auto|automatic transmission)\b""", RegexOption.IGNORE_CASE))) {
            needs.add("automatic")
        }
        
        return needs
    }
}
