package ru.kheynov.secretsanta.presentation.screens.create_room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.kheynov.secretsanta.R
import ru.kheynov.secretsanta.domain.entities.RoomDTO
import ru.kheynov.secretsanta.domain.use_cases.rooms.RoomsUseCases
import ru.kheynov.secretsanta.utils.Resource
import ru.kheynov.secretsanta.utils.SantaException
import ru.kheynov.secretsanta.utils.UiText
import java.time.LocalDate
import javax.inject.Inject


private const val passwordRegex = "^[A-Za-z0-9_]+\$"

@HiltViewModel
class CreateRoomFragmentViewModel @Inject constructor(
    private val useCases: RoomsUseCases,
) : ViewModel() {
    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state
    
    sealed interface State {
        object Loading : State
        object Idle : State
        data class Loaded(val room: RoomDTO.Info) : State
        data class Error(val error: Exception) : State
    }
    
    private val _actions: Channel<Action> = Channel(Channel.BUFFERED)
    val actions: Flow<Action> = _actions.receiveAsFlow()
    
    sealed interface Action {
        data class ShowError(val error: UiText) : Action
        object ShowSuccess : Action
    }
    
    private var _date: MutableStateFlow<LocalDate?> = MutableStateFlow(null)
    val date: StateFlow<LocalDate?> = _date
    
    private val ioDispatcher = Dispatchers.IO
    fun createRoom(roomName: String, password: String?, date: LocalDate?, maxPrice: String?) {
        viewModelScope.launch {
            _state.value = State.Loading
            if (roomName.isBlank()) {
                _actions.send(Action.ShowError(UiText.StringResource(R.string.room_name_empty_error)))
                _state.value = State.Idle
                return@launch
            }
            
            if (roomName.length > 20) {
                _actions.send(Action.ShowError(UiText.StringResource(R.string.room_length_too_high)))
                _state.value = State.Idle
                return@launch
            }
            
            val price: Int? = maxPrice.let {
                if (it.isNullOrBlank() || it == "0") null
                else try {
                    it.toInt()
                } catch (e: Exception) {
                    _actions.send(Action.ShowError(UiText.StringResource(R.string.wrong_max_price)))
                    _state.value = State.Idle
                    return@launch
                }
            }
            
            if ((price ?: 0) > 1000000) {
                _actions.send(Action.ShowError(UiText.StringResource(R.string.max_price_too_high_error)))
                _state.value = State.Idle
                return@launch
            }
            if ((price ?: 0) < 0) {
                _actions.send(Action.ShowError(UiText.StringResource(R.string.wrong_max_price)))
                _state.value = State.Idle
                return@launch
            }
            with(password) {
                if (isNullOrBlank()) null
                else if (length > 20) {
                    _actions.send(Action.ShowError(UiText.StringResource(R.string.password_length_too_high_error)))
                    _state.value = State.Idle
                    return@launch
                } else if (!Regex(passwordRegex).matches(this)) {
                    _actions.send(Action.ShowError(UiText.StringResource(R.string
                        .invalid_password_format)))
                    _state.value = State.Idle
                    return@launch
                } else if (length < 8) {
                    _actions.send(Action.ShowError(UiText.StringResource(R.string
                        .password_too_short)))
                    _state.value = State.Idle
                    return@launch
                } else {
                    this
                }
            }
            
            
            val room = RoomDTO.Create(
                roomName,
                password,
                date,
                price
            )
            val res = withContext(ioDispatcher) {
                useCases.createRoomUseCase(room)
            }
            when (res) {
                is Resource.Success -> {
                    _state.value = State.Loaded(res.result)
                    _actions.send(Action.ShowSuccess)
                }
                is Resource.Failure -> {
                    if (res.exception is SantaException) {
                        _state.value = State.Error(res.exception)
                    } else {
                        _state.value = State.Idle
                        _actions.send(Action.ShowError(UiText.PlainText(res.exception.javaClass.simpleName.toString())))
                    }
                }
            }
        }
    }
    
    fun setDate(localDate: LocalDate?) {
        _date.value = localDate
    }
    
    fun clearDate() {
        _date.value = null
    }
}