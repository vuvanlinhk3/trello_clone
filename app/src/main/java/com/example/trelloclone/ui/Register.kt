package com.example.trelloclone.ui
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation // ĐÃ SỬA CHÍNH TẢ
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.trelloclone.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Họ và tên") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Xác nhận mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (password != confirmPassword) {
                    Toast.makeText(context, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (name.isBlank() || email.isBlank() || password.length < 6) {
                    Toast.makeText(context, "Vui lòng nhập đủ thông tin (mật khẩu tối thiểu 6 ký tự)", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                loading = true
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = task.result.user?.uid ?: run {
                                loading = false
                                Toast.makeText(context, "Không lấy được UID người dùng", Toast.LENGTH_SHORT).show()
                                return@addOnCompleteListener
                            }

                            val user = User(
                                uid = uid,
                                name = name,
                                email = email
                            )

                            firestore.collection("users").document(uid).set(user)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                    loading = false
                                    onRegisterSuccess()
                                }
                                .addOnFailureListener { e ->
                                    loading = false
                                    Toast.makeText(context, "Lỗi lưu thông tin: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            loading = false
                            Toast.makeText(context, "Lỗi đăng ký: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(if (loading) "Đang đăng ký..." else "Đăng ký")
        }
    }
}