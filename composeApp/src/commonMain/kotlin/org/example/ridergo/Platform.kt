package org.example.ridergo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform