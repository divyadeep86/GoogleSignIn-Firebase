package com.vh.googlesigninfirebase.signIn

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


/**
 *
 * The SignInViewModel class is a part of the Android Architecture Components, specifically the ViewModel component. It is designed to manage UI-related data in a lifecycle-conscious way, ensuring that the data survives configuration changes such as screen rotations.
 * Properties:
 * _state: A MutableStateFlow object that holds the current state of the sign-in process. It is private to ensure encapsulation and is only modifiable within the SignInViewModel class.
 * state: A public, read-only version of _state, exposed as a StateFlow. This allows observers (e.g., UI components) to listen to changes in the sign-in state without being able to modify it directly.
 * Functions:
 * onSignInResult(result: SignInResult): This function is called with a SignInResult object, which contains the result of the sign-in operation. The function updates _state with the new sign-in status (isSignInSuccessful) based on whether result.data is not null (indicating success) and any error message (signInError) if the sign-in failed.
 * resetState(): Resets the _state to its initial value (SignInState()), effectively clearing any previous sign-in results or errors. This can be useful for resetting the UI state when starting a new sign-in operation.
 * The SignInViewModel acts as a bridge between the sign-in logic and the UI components, allowing the UI to observe and react to changes in the sign-in process without directly managing the process itself. This separation of concerns makes the code more modular, easier to manage, and less prone to bugs, especially during configuration changes.
 * */
class SignInViewModel: ViewModel() {
    
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()
    
    fun onSignInResult(result: SignInResult) {
        _state.update { it.copy(
            isSignInSuccessful = result.data != null,
            signInError = result.errorMessage
        ) }
    }
    
    fun resetState() {
        _state.update { SignInState() }
    }
}