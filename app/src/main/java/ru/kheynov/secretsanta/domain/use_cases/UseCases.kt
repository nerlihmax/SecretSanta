package ru.kheynov.secretsanta.domain.use_cases

import ru.kheynov.secretsanta.domain.use_cases.users.CheckUserRegisteredUseCase
import ru.kheynov.secretsanta.domain.use_cases.users.DeleteUserUseCase
import ru.kheynov.secretsanta.domain.use_cases.users.GetSelfInfoUseCase
import ru.kheynov.secretsanta.domain.use_cases.users.RegisterUserUseCase
import ru.kheynov.secretsanta.domain.use_cases.users.UpdateUserUseCase

data class UseCases(
    val registerUserUseCase: RegisterUserUseCase,
    val deleteUserUseCase: DeleteUserUseCase,
    val updateUserUseCase: UpdateUserUseCase,
    val getSelfInfoUseCase: GetSelfInfoUseCase,
    val checkUserRegistered: CheckUserRegisteredUseCase,
)