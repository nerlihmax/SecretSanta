package ru.kheynov.secretsanta.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.kheynov.secretsanta.domain.use_cases.users.UsersUseCases
import ru.kheynov.secretsanta.utils.Resource
import javax.inject.Inject

@HiltViewModel
class ProfileFragmentViewModel @Inject constructor(
    private val useCases: UsersUseCases,
) : ViewModel() {
    sealed interface State {
        object Loading : State
        data class Loaded(val name: String) : State
    }

    sealed interface Action {
        object NavigateToLoginScreen : Action
        object ShowError : Action
        object NavigateToEditUser : Action
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state

    private val _actions: Channel<Action> = Channel(Channel.BUFFERED)
    val actions: Flow<Action> = _actions.receiveAsFlow()

    private lateinit var username: String

    init {
        updateUsername()
    }

    fun updateUsername() {
        viewModelScope.launch {
            _state.value = State.Loading
            when (val res = useCases.getSelfInfoUseCase()) {
                is Resource.Failure -> _actions.send(Action.ShowError)
                is Resource.Success -> {
                    username = res.result.username
                    _state.value = State.Loaded(username)
                }
            }

        }
    }

    fun logout() {
        viewModelScope.launch {
            _actions.send(Action.NavigateToLoginScreen)
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            useCases.deleteUserUseCase()
            _actions.send(Action.NavigateToLoginScreen)
        }
    }

    fun editUsername() {
        viewModelScope.launch {
            _actions.send(Action.NavigateToEditUser)
        }
    }
}