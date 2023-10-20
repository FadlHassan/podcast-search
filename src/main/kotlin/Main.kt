import com.apollographql.apollo3.api.ApolloResponse
import com.podcast.category.search.PodcastCategorySearchQuery
import kotlinx.coroutines.runBlocking
import utils.search.Podcast
import utils.search.categoryMatch
import utils.search.searchForTopRankedPodcastIn
import kotlin.system.exitProcess

fun main(): Unit = runBlocking {
    // Create a client
    println("Having trouble finding podcasts on topics you're interested in? Tell us what you like and we'll recommend you the top 20 rated podcasts that match your tastes (type 'exit' to quit):");

    while (true) {
        val userInput = readln()
        if (userInput == "exit") {
            println("Exiting...")
            break
        }
        val closestCategory = categoryMatch(userInput)
        val response: ApolloResponse<PodcastCategorySearchQuery.Data> = searchForTopRankedPodcastIn(closestCategory)
        val details = mutableListOf<Podcast>()
        for (searchResult in response.data?.podcasts?.data.orEmpty()) {
            details.add(Podcast(searchResult.title, searchResult.description))
        }
        when (details.size) {
            0 -> println("No podcasts match the search category")
            else -> {
                for ((rank, podcast) in details.withIndex()) {
                    println("#${rank+1}: ${podcast.title}")
                }
            }
        }
    }

    exitProcess(0)
}