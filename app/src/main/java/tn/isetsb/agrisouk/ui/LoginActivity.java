package tn.isetsb.agrisouk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.data.UserRepository;
import tn.isetsb.agrisouk.model.User;
import tn.isetsb.agrisouk.util.SessionManager;

/**
 * Connexion avec deux comptes de démonstration (admin et utilisateur).
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextView tvDemoHint;
    private UserRepository userRepository;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (session.isLoggedIn()) {
            openMain();
            return;
        }
        setContentView(R.layout.activity_login);

        userRepository = new UserRepository(this);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvDemoHint = findViewById(R.id.tvDemoHint);

        MaterialButton loginButton = findViewById(R.id.btnLogin);
        loginButton.setOnClickListener(view -> attemptLogin());
        findViewById(R.id.btnDemoAdmin).setOnClickListener(view ->
                fillDemo("admin@agrisouk.tn", "admin123"));
        findViewById(R.id.btnDemoUser).setOnClickListener(view ->
                fillDemo("user@agrisouk.tn", "user123"));
        findViewById(R.id.btnGoRegister).setOnClickListener(view ->
                startActivity(new Intent(this, RegisterActivity.class)));

        etPassword.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });
    }

    private void fillDemo(String email, String password) {
        etEmail.setText(email);
        etPassword.setText(password);
        tvDemoHint.setText(getString(R.string.demo_hint_format, email, password));
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    private void attemptLogin() {
        hideKeyboard();
        String email = textOf(etEmail);
        String password = textOf(etPassword);
        tilEmail.setError(null);
        tilPassword.setError(null);

        if (email.isEmpty()) {
            tilEmail.setError(getString(R.string.login_empty_fields));
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.login_empty_fields));
            return;
        }

        User user = userRepository.authenticate(email, password);
        if (user == null) {
            tilPassword.setError(getString(R.string.login_invalid));
            etPassword.requestFocus();
            return;
        }

        session.login(user);
        openMain();
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String textOf(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private void hideKeyboard() {
        InputMethodManager manager =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null && getCurrentFocus() != null) {
            manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
