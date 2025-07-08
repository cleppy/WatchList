package com.example.watchlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.watchlist.data.Movie
import com.example.watchlist.data.TvShow // Import TvShow
import com.example.watchlist.MovieViewModel
import com.example.watchlist.ui.theme.WatchListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)
        setContent {
            WatchListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MovieViewModel = viewModel()) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Discover", "Watched", "Watchlist")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("WatchList") })
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            when (selectedTabIndex) {
                0 -> DiscoverScreen(viewModel)
                1 -> WatchedScreen(viewModel)
                2 -> WatchlistScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(viewModel: MovieViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val popularMovies by viewModel.popularMovies.collectAsState()
    val popularTvShows by viewModel.popularTvShows.collectAsState()
    val combinedSearchResults by viewModel.combinedSearchResults.collectAsState() // Use combined results

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query -> viewModel.searchAll(query) }, // Call searchAll
            label = { Text("Search Movies, TV Shows, Anime...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { /* Handled by onValueChange */ }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Display search results if there's a query, otherwise show a combined list of popular movies/TV shows
        val itemsToDisplay = if (searchQuery.isNotBlank()) combinedSearchResults else {
            // Combine popular movies and TV shows for initial display
            // You might want to sort this list or paginate it
            mutableListOf<Any>().apply {
                addAll(popularMovies)
                addAll(popularTvShows)
            }.shuffled() // Shuffle for variety, or add specific sorting logic
        }

        if (itemsToDisplay.isEmpty() && searchQuery.isNotBlank()) {
            Text(
                text = "No results found for \"$searchQuery\"",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        } else if (itemsToDisplay.isEmpty() && searchQuery.isBlank()) {
            Text(
                text = "Loading popular content...",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(itemsToDisplay) { item ->
                when (item) {
                    is Movie -> MovieCard(movie = item, viewModel = viewModel)
                    is TvShow -> TvShowCard(tvShow = item, viewModel = viewModel) // New TvShowCard
                    // Add other types if you introduce them (e.g., Anime specific)
                }
            }
        }
    }
}

@Composable
fun WatchedScreen(viewModel: MovieViewModel) {
    val watchedMovies by viewModel.watchedMovies.collectAsState()

    if (watchedMovies.isEmpty()) {
        Text(
            text = "No movies or TV shows marked as watched yet!",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(watchedMovies) { watchedMedia ->
                LocalMediaCard(watchedMedia.title, watchedMedia.posterPath) {
                    viewModel.removeMediaFromWatched(watchedMedia.movieId)
                }
            }
        }
    }
}

@Composable
fun WatchlistScreen(viewModel: MovieViewModel) {
    val watchlistMovies by viewModel.watchlistMovies.collectAsState()

    if (watchlistMovies.isEmpty()) {
        Text(
            text = "Your watchlist is empty!",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(watchlistMovies) { watchlistMedia ->
                LocalMediaCard(watchlistMedia.title, watchlistMedia.posterPath) {
                    viewModel.removeMediaFromWatchlist(watchlistMedia.movieId)
                }
            }
        }
    }
}

@Composable
fun MovieCard(movie: Movie, viewModel: MovieViewModel) {
    val isWatched by produceState(initialValue = false, movie.id) {
        value = viewModel.isMediaWatched(movie.id) // Use isMediaWatched
    }
    val isOnWatchlist by produceState(initialValue = false, movie.id) {
        value = viewModel.isMediaOnWatchlist(movie.id) // Use isMediaOnWatchlist
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500/${movie.posterPath}",
                contentDescription = "${movie.title} Poster",
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight(),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = "Rating", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Rating: ${String.format("%.1f", movie.voteAverage)}/10",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = movie.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(IntrinsicSize.Min),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Button(
                    onClick = {
                        if (isWatched) viewModel.removeMediaFromWatched(movie.id)
                        else viewModel.addMediaToWatched(movie) // Use addMediaToWatched
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isWatched) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Watched", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isWatched) "Unwatched" else "Watched", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = {
                        if (isOnWatchlist) viewModel.removeMediaFromWatchlist(movie.id)
                        else viewModel.addMediaToWatchlist(movie) // Use addMediaToWatchlist
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOnWatchlist) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Filled.Bookmark, contentDescription = "Watchlist", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isOnWatchlist) "Remove" else "Watchlist", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

// --- New Composable for TV Shows ---
@Composable
fun TvShowCard(tvShow: TvShow, viewModel: MovieViewModel) {
    val isWatched by produceState(initialValue = false, tvShow.id) {
        value = viewModel.isMediaWatched(tvShow.id)
    }
    val isOnWatchlist by produceState(initialValue = false, tvShow.id) {
        value = viewModel.isMediaOnWatchlist(tvShow.id)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500/${tvShow.posterPath}",
                contentDescription = "${tvShow.name} Poster",
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight(),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                Text(
                    text = tvShow.name, // Use 'name' for TV shows
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = "Rating", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Rating: ${String.format("%.1f", tvShow.voteAverage)}/10",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tvShow.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(IntrinsicSize.Min),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Button(
                    onClick = {
                        if (isWatched) viewModel.removeMediaFromWatched(tvShow.id)
                        else viewModel.addMediaToWatched(tvShow) // Add TvShow to watched
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isWatched) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Watched", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isWatched) "Unwatched" else "Watched", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = {
                        if (isOnWatchlist) viewModel.removeMediaFromWatchlist(tvShow.id)
                        else viewModel.addMediaToWatchlist(tvShow) // Add TvShow to watchlist
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOnWatchlist) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Filled.Bookmark, contentDescription = "Watchlist", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isOnWatchlist) "Remove" else "Watchlist", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalMediaCard(title: String, posterPath: String?, onRemoveClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w200/${posterPath}",
                contentDescription = "$title Poster",
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight(),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemoveClick) {
                Icon(Icons.Default.Close, contentDescription = "Remove")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    WatchListTheme {
        MainScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun MovieCardPreview() {
    WatchListTheme {
        MovieCard(
            movie = Movie(
                id = 123,
                title = "The Preview Movie Title That Is Very Long And Wraps To Two Lines",
                posterPath = "/t6HIqrREqkRLgHhpACykYdyrNyw.jpg",
                description = "This is a very long description for the preview movie card. It should wrap to multiple lines to test the layout and show how text overflow works effectively.",
                releaseDate = "2023-01-01",
                voteAverage = 8.5
            ),
            viewModel = viewModel()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TvShowCardPreview() {
    WatchListTheme {
        TvShowCard(
            tvShow = TvShow(
                id = 456,
                name = "Preview Anime Series Name Is Also Very Long",
                posterPath = "/path/to/tv_poster.jpg",
                description = "This is a description for a TV show/anime. It demonstrates how a TvShowCard would look in the UI.",
                firstAirDate = "2022-04-01",
                voteAverage = 7.9
            ),
            viewModel = viewModel()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LocalMediaCardPreview() {
    WatchListTheme {
        LocalMediaCard(
            title = "Preview Watched Anime Episode",
            posterPath = "/path/to/local_poster.jpg",
            onRemoveClick = {}
        )
    }
}