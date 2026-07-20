package com.vrsa.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vrsa.app.data.ConfigRepository
import com.vrsa.app.domain.ParseError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorUiState(
    val text: String = "",
    val loaded: Boolean = false,
    val errors: List<ParseError> = emptyList(),
)

class EditorViewModel(private val repository: ConfigRepository) : ViewModel() {

    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    /** Emits the number of scheduled reminders after each save, for the snackbar. */
    private val _saved = MutableSharedFlow<Int>()
    val saved: SharedFlow<Int> = _saved.asSharedFlow()

    init {
        viewModelScope.launch {
            val text = repository.loadText()
            // Apply on startup so edits made outside the app (file manager,
            // adb) take effect without a manual Save — a v1 limitation.
            val result = repository.apply(text)
            _state.value = EditorUiState(text = text, loaded = true, errors = result.errors)
        }
    }

    fun onTextChange(text: String) {
        _state.update { it.copy(text = text) }
    }

    fun save() {
        viewModelScope.launch {
            val result = repository.apply(_state.value.text)
            _state.update { it.copy(errors = result.errors) }
            _saved.emit(result.reminders.size)
        }
    }

    /** Belt-and-braces re-sync when the app returns to the foreground. */
    fun rescheduleAll() {
        viewModelScope.launch { repository.rescheduleAll() }
    }

    companion object {
        fun factory(repository: ConfigRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { EditorViewModel(repository) }
        }
    }
}
