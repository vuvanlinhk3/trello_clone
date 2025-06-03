package com.example.trelloclone.ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    var currentPassword by mutableStateOf("")
        private set
    var newPassword by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set
    var message by mutableStateOf<String?>(null)
        private set

    fun onCurrentPasswordChange(value: String) {
        currentPassword = value
    }

    fun onNewPasswordChange(value: String) {
        newPassword = value
    }

    fun onConfirmPasswordChange(value: String) {
        confirmPassword = value
    }

    fun clearMessage() {
        message = null
    }

    fun changePassword(onSuccess: () -> Unit) {
        val user = auth.currentUser
        val email = user?.email

        if (newPassword.length < 6) {
            message = "Mật khẩu mới phải có ít nhất 6 ký tự"
            return
        }

        if (newPassword != confirmPassword) {
            message = "Mật khẩu xác nhận không khớp"
            return
        }

        if (email == null || user == null) {
            message = "Không tìm thấy người dùng"
            return
        }

        val credential = EmailAuthProvider.getCredential(email, currentPassword)

        user.reauthenticate(credential).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        message = "Đổi mật khẩu thành công"
                        currentPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                        onSuccess()
                    } else {
                        message = updateTask.exception?.message ?: "Lỗi đổi mật khẩu"
                    }
                }
            } else {
                message = "Mật khẩu hiện tại không đúng hoặc đã hết phiên đăng nhập"
            }
        }
    }
}

@Composable
fun ChangePassword(
    onBack: () -> Unit
) {
    val viewModel: ChangePasswordViewModel = viewModel()

    val currentPassword = viewModel.currentPassword
    val newPassword = viewModel.newPassword
    val confirmPassword = viewModel.confirmPassword
    val message = viewModel.message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Đổi mật khẩu", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = currentPassword,
            onValueChange = viewModel::onCurrentPasswordChange,
            label = { Text("Mật khẩu hiện tại") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = viewModel::onNewPasswordChange,
            label = { Text("Mật khẩu mới") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            label = { Text("Xác nhận mật khẩu mới") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.changePassword { } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Đổi mật khẩu")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Text("Quay lại")
        }

        message?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
