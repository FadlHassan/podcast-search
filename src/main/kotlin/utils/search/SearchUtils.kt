package utils.search

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import com.podcast.category.search.PodcastCategorySearchQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*

data class Podcast(val title: String, val description: String)

class AuthorizationInterceptor(private val token: String) : HttpInterceptor {
    override suspend fun intercept(request: HttpRequest,  chain: HttpInterceptorChain): HttpResponse {
        return chain.proceed(request.newBuilder().addHeader("Authorization", "Bearer $token").build())
    }
}

/**
 * Runs the python script, which matches the user input to the closest Podchaser category using an OpenAi text embedding model
 * @param userInput the user input
 * @return Podchaser category slug
 */
suspend fun categoryMatch(userInput: String): String {
    val processBuilder = ProcessBuilder()
    val properties = Properties()
    withContext(Dispatchers.IO) {
        properties.load(FileInputStream("config.properties"))
    }
    val pythonInterpreter = properties.getProperty("PYTHON_INTERPRETER")
    val pythonCategoryMatchScript = properties.getProperty("PYTHON_CATEGORY_MATCH_SCRIPT")
    processBuilder.command(pythonInterpreter, pythonCategoryMatchScript, userInput)
    return try {
        withContext(Dispatchers.IO) {
            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val resultFromPython = reader.readLine()
            process.waitFor()
            resultFromPython
        }
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

/**
 * Searches for the top 20 podcasts for the given category slug, by make a GraphQl query to the Podchaser API
 * @param category the podcast category
 * @return an ApolloResponse, containing the GraphQL query result data.
 */
suspend fun searchForTopRankedPodcastIn(category: String): ApolloResponse<PodcastCategorySearchQuery.Data> {
    val properties = Properties()
    val apolloClient: ApolloClient?
    withContext(Dispatchers.IO) {
        properties.load(FileInputStream("config.properties"))
    }
    val accessToken = properties.getProperty("PODCHASER_TOKEN")
    apolloClient = ApolloClient.Builder().serverUrl("https://api.podchaser.com/graphql").addHttpInterceptor(
        AuthorizationInterceptor(accessToken)
    ).build()
    return apolloClient.query(PodcastCategorySearchQuery(category = category)).execute()
}
