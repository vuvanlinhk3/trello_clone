package com.example.trelloclone.ui

// Import các thư viện cần thiết
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

// State lưu trữ thông tin giao diện người dùng
data class ProfileState(
    val displayName: String = "",       // Tên hiển thị người dùng
    val email: String = "",             // Email người dùng
    val avatarBitmap: Bitmap? = null,   // Ảnh đại diện đã load
    val newAvatarUri: Uri? = null       // URI ảnh mới chọn từ thiết bị
)

// ViewModel quản lý logic của màn hình hồ sơ
class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // Trạng thái UI được lưu trong State
    private val _uiState = mutableStateOf(ProfileState())
    val uiState: State<ProfileState> = _uiState

    init {
        loadUserData()  // Khi ViewModel được tạo, tải dữ liệu người dùng
    }

    // Tải dữ liệu người dùng hiện tại từ FirebaseAuth và FirebaseStorage
    private fun loadUserData() {
        val user = auth.currentUser
        _uiState.value = _uiState.value.copy(
            displayName = user?.displayName.orEmpty(),
            email = user?.email.orEmpty()
        )

        // Tải ảnh đại diện từ Firebase Storage (nếu có)
        val avatarRef = storage.reference.child("avatars/${user?.uid}.jpg")
        avatarRef.getBytes(1024 * 1024).addOnSuccessListener { bytes ->
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            _uiState.value = _uiState.value.copy(avatarBitmap = bitmap)
        }
    }

    // Khi tên thay đổi từ TextField, cập nhật vào state
    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(displayName = name)
    }

    // Khi người dùng chọn ảnh mới, cập nhật ảnh và Uri vào state
    fun setNewAvatar(bitmap: Bitmap, uri: Uri) {
        _uiState.value = _uiState.value.copy(
            avatarBitmap = bitmap,
            newAvatarUri = uri
        )
    }

    // Lưu tên mới và avatar mới (nếu có) lên Firebase
    fun saveProfile() {
        val user = auth.currentUser ?: return

        // Cập nhật tên hiển thị
        val updates = userProfileChangeRequest {
            displayName = _uiState.value.displayName
        }
        user.updateProfile(updates)

        // Nếu có ảnh mới, upload lên Firebase Storage
        val uri = _uiState.value.newAvatarUri ?: return
        val ref = storage.reference.child("avatars/${user.uid}.jpg")
        ref.putFile(uri)
    }

    // Đăng xuất người dùng
    fun logout() {
        auth.signOut()
    }
}

// ---------- Giao diện hồ sơ người dùng ----------

@Composable
fun ProfileScreen(
    onRegisterSuccess: () -> Unit // Callback để chuyển màn sau khi logout
) {
    val viewModel: ProfileViewModel = viewModel()
    val state by viewModel.uiState
    val context = LocalContext.current

    // Cho phép chọn ảnh từ thiết bị (gallery)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Mở ảnh từ Uri và chuyển thành Bitmap
            val stream: InputStream? = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(stream)
            viewModel.setNewAvatar(bitmap, it)
        }
    }

    // Giao diện cột chính
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        // Vùng avatar (nhấn để chọn ảnh mới)
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

        // Hiển thị email (chỉ đọc)
        Text(text = state.email, style = MaterialTheme.typography.bodyMedium)

        // TextField chỉnh sửa tên hiển thị
        OutlinedTextField(
            value = state.displayName,
            onValueChange = { viewModel.onNameChanged(it) },
            label = { Text("Tên hiển thị") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Nút lưu thông tin lên Firebase
        Button(onClick = { viewModel.saveProfile() }) {
            Text("Lưu thay đổi")
        }

        Spacer(modifier = Modifier.height(16.dp)) // khoảng cách giữa 2 nút
        // Nút đăng xuất
        Button(
            onClick = {
                viewModel.logout()
                onRegisterSuccess() // Thông báo để điều hướng hoặc reset UI
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Đăng xuất", color = Color.White)
        }
    }
}
