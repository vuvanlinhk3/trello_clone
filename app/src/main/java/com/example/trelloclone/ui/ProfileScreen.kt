package com.example.trelloclone.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import java.io.InputStream

// ---------- ViewModel & State ----------

data class ProfileState(
    val displayName: String = "",
    val email: String = "",
    val avatarBitmap: Bitmap? = null,
    val newAvatarUri: Uri? = null,
    val emailEditable: String = "",    // email editable state
    val showDeleteDialog: Boolean = false,
    val infoMessage: String? = null
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _uiState = mutableStateOf(ProfileState())
    val uiState: State<ProfileState> = _uiState

    fun setInfoMessage(message: String?) {
        _uiState.value = _uiState.value.copy(infoMessage = message)
    }

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val user = auth.currentUser
        _uiState.value = _uiState.value.copy(
            displayName = user?.displayName.orEmpty(),
            email = user?.email.orEmpty(),
            emailEditable = user?.email.orEmpty()
        )

        val avatarRef = storage.reference.child("avatars/${user?.uid}.jpg")
        avatarRef.getBytes(1024 * 1024).addOnSuccessListener { bytes ->
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            _uiState.value = _uiState.value.copy(avatarBitmap = bitmap)
        }
    }

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(displayName = name)
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(emailEditable = email)
    }

    fun setNewAvatar(bitmap: Bitmap, uri: Uri) {
        _uiState.value = _uiState.value.copy(
            avatarBitmap = bitmap,
            newAvatarUri = uri
        )
    }

    fun saveProfile() {
        val user = auth.currentUser ?: return

        // Cập nhật tên và email nếu khác
        val updates = userProfileChangeRequest {
            displayName = _uiState.value.displayName
        }
        user.updateProfile(updates).addOnCompleteListener {
            // nếu cập nhật tên thành công thì cập nhật email (Firebase yêu cầu re-authentication nếu email thay đổi)
            if (user.email != _uiState.value.emailEditable) {
                user.updateEmail(_uiState.value.emailEditable).addOnCompleteListener { emailResult ->
                    if (emailResult.isSuccessful) {
                        _uiState.value = _uiState.value.copy(infoMessage = "Cập nhật email thành công")
                    } else {
                        _uiState.value = _uiState.value.copy(infoMessage = "Lỗi cập nhật email: ${emailResult.exception?.message}")
                    }
                }
            }
        }

        // Upload avatar nếu có
        val uri = _uiState.value.newAvatarUri
        if (uri != null) {
            val ref = storage.reference.child("avatars/${user.uid}.jpg")
            ref.putFile(uri)
        }
    }

    // Gửi email đổi mật khẩu
    fun sendPasswordResetEmail(onResult: (Boolean, String?) -> Unit) {
        val email = auth.currentUser?.email
        if (email.isNullOrEmpty()) {
            onResult(false, "Email không tồn tại")
            return
        }
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true, "Đã gửi email đổi mật khẩu đến $email")
            } else {
                onResult(false, task.exception?.message)
            }
        }
    }

    // Hiển thị dialog xác nhận xóa tài khoản
    fun showDeleteAccountDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDeleteDialog = show)
    }

    // Xóa tài khoản hiện tại
    fun deleteAccount(onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: run {
            onComplete(false, "Người dùng chưa đăng nhập")
            return
        }
        user.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true, null)
            } else {
                onComplete(false, task.exception?.message)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(infoMessage = null)
    }
}

// ---------- Composable giao diện ----------

@Composable
fun ProfileScreen(
    onLogoutSuccess: () -> Unit // callback chuyển màn hình khi đăng xuất hoặc xóa tài khoản thành công
) {
    val viewModel: ProfileViewModel = viewModel()
    val state by viewModel.uiState
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val stream: InputStream? = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(stream)
            viewModel.setNewAvatar(bitmap, it)
        }
    }

    // Hiển thị dialog xác nhận xóa tài khoản
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showDeleteAccountDialog(false) },
            title = { Text("Xác nhận") },
            text = { Text("Bạn có chắc chắn muốn xóa tài khoản không? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccount { success, message ->
                        if (success) {
                            viewModel.showDeleteAccountDialog(false)
                            onLogoutSuccess()
                        } else {
                            // Hiển thị lỗi, có thể thêm Snackbar hoặc Toast (đơn giản dùng message Text ở dưới)
                            viewModel.showDeleteAccountDialog(false)
                            viewModel.clearMessage()
                        }
                    }
                }) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showDeleteAccountDialog(false) }) {
                    Text("Hủy")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .clickable { imagePickerLauncher.launch("image/*") }
        ) {
            if (state.avatarBitmap != null) {
                Image(
                    bitmap = state.avatarBitmap!!.asImageBitmap(),
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Avatar",
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Email có thể chỉnh sửa
        OutlinedTextField(
            value = state.emailEditable,
            onValueChange = { viewModel.onEmailChanged(it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.displayName,
            onValueChange = { viewModel.onNameChanged(it) },
            label = { Text("Tên hiển thị") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = { viewModel.saveProfile() }) {
            Text("Lưu thay đổi")
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            viewModel.sendPasswordResetEmail { success, message ->
                // Hiển thị thông báo, có thể dùng Snackbar hoặc Toast. Đơn giản dùng message Text
                viewModel.setInfoMessage(message)
            }
        }) {
            Text("Đổi mật khẩu")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            onClick = { viewModel.showDeleteAccountDialog(true) }
        ) {
            Text("Xóa tài khoản", color = Color.White)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            onClick = {
                viewModel.logout()
                onLogoutSuccess()
            }
        ) {
            Text("Đăng xuất", color = Color.White)
        }

        // Hiển thị thông báo lỗi hoặc thành công
        state.infoMessage?.let { msg ->
            Spacer(Modifier.height(16.dp))
            Text(text = msg, color = Color.Red)
            // Tự động xóa thông báo sau vài giây hoặc khi người dùng nhập tiếp
        }
    }
}
