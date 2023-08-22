package com.lagradost

import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.app

import okio.ByteString.Companion.decodeBase64

open class FuseVideoExtractor : ExtractorApi() {    
    override val name: String = "fusevideo"
    override val mainUrl: String = "https://fusevideo.io"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val refer = url
        val headers = mapOf(
            "Accept" to "*/*",
            "Accept-Language" to "en-US,en;q=0.5",
        )
        val document = app.get(url, headers = headers).document

        val scriptSourceUrl =
            document.select("""script[src^="https://fusevideo.io/f/u/u/u/u?"]""")
                .attr("src") // Get the URL where the script function is
        val scriptDocument =
            app.get(scriptSourceUrl, headers = headers).document // Open the script function
        val base64CodeRegex =
            Regex("n=atob\\(\"([^\"]+)\"\\)") // Search for the base64 code

        val code64 = base64CodeRegex.find(scriptDocument.toString())?.groupValues?.get(1)

        val decoded = code64?.decodeBase64()?.utf8() // Decode the base64 code

        val regexLink = Regex("""\"(https:\\\/\\\/[^"]*)""") // Extract the m3u8 link
        val m3u8found = regexLink.find(decoded.toString())?.groupValues?.get(1)
        val m3u8 = m3u8found.toString().replace("\\", "") // Remove the backslashes from the m3u8 link

        return listOfNotNull(
            ExtractorLink(
                name,
                name,
                m3u8,
                refer,
                Qualities.Unknown.value,
                true,
                headers = headers
            )
        )
    }
}
