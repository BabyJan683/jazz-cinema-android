package com.jazzcinema.app.ui.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jazzcinema.app.model.MovieCategory
import com.jazzcinema.app.repository.MovieRepository
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val categories: List<MovieCategory>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {

    private val _uiState = MutableLiveData<HomeUiState>(HomeUiState.Loading)
    val uiState: LiveData<HomeUiState> = _uiState

    private var lastQuery: String? = null

    fun loadMovies(context: Context, search: String? = null, forceRefresh: Boolean = false) {
        lastQuery = search
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            try {
                val categories = MovieRepository.getMovies(context, search, forceRefresh)
                _uiState.value = HomeUiState.Success(categories)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    e.message ?: "Failed to load movies"
                )
            }
        }
    }

    fun search(context: Context, query: String) =
        loadMovies(context, query.takeIf { it.isNotBlank() })

    fun refresh(context: Context) = loadMovies(context, lastQuery, forceRefresh = true)
}
