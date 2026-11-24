package tn.isetsb.agrisouk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.data.UserRepository;
import tn.isetsb.agrisouk.model.User;
import tn.isetsb.agrisouk.util.SessionManager;

/**
 * Création d'un compte utilisateur (rôle USER).
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPhone;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirm;
    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirm;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRepository = new UserRepository(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarRegister);
        toolbar.setNavigationOnClickListener(view -> finish());
        findViewById(R.id.btnBackToLogin).setOnClickListener(view -> finish());

        tilName = findViewById(R.id.tilRegName);
        tilEmail = findViewById(R.id.tilRegEmail);
        tilPhone = findViewById(R.id.tilRegPhone);
        tilPassword = findViewById(R.id.tilRegPassword);
        tilConfirm = findViewById(R.id.tilRegConfirm);
        etName = findViewById(R.id.etRegName);
        etEmail = findViewById(R.id.etRegEmail);
        etPhone = findViewById(R.id.etRegPhone);
        etPassword = findViewById(R.id.etRegPassword);
        etConfirm = findViewById(R.id.etRegConfirm);

        findViewById(R.id.btnRegister).setOnClickListener(view -> attemptRegister());
    }

    private void attemptRegister() {
        clearErrors();
        String name = textOf(etName);
        String email = textOf(etEmail).toLowerCase(Locale.ROOT);
        String phone = textOf(etPhone);
        String password = textOf(etPassword);
        String confirm = textOf(etConfirm);

        if (name.length() < 3) {
            tilName.setError(getString(R.string.error_name));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email));
            return;
        }
        if (!phone.isEmpty() && !phone.matches("\\d{8}")) {
            tilPhone.setError(getString(R.string.error_phone));
            return;
        }
        if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password));
            return;
        }
        if (!password.equals(confirm)) {
            tilConfirm.setError(getString(R.string.error_confirm));
            return;
        }

        long userId = userRepository.register(name, email, phone, password);
        if (userId == -1) {
            tilEmail.setError(getString(R.string.error_email_exists));
            return;
        }

        User user = new User(userId, name, email, phone, User.ROLE_USER);
        new SessionManager(this).login(user);
        Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void clearErrors() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilPassword.setError(null);
        tilConfirm.setError(null);
    }

    private String textOf(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
