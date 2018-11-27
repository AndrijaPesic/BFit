package rs.elfak.mosis.akitoske.bfit.utils;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.validator.routines.EmailValidator;

import rs.elfak.mosis.akitoske.bfit.App;
import rs.elfak.mosis.akitoske.bfit.R;

public class Validator {

    public static void validateEmail(EditText emailEt) {
        String email = emailEt.getText().toString().trim();

        if (email.isEmpty()) {
            emailEt.setError(App.getContext().getString(R.string.required_field_error));
        } else if (!EmailValidator.getInstance().isValid(email)) {
            emailEt.setError(App.getContext().getString(R.string.email_bad_format_error));
        } else {
            emailEt.setError(null);
        }
    }

    public static void validatePassword(EditText passwordEt) {
        String password = passwordEt.getText().toString().trim();
        // Minimum 8 characters, at least one letter and one number.
        String pattern = "^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z0-9]{8,}$";

        if (password.isEmpty()) {
            passwordEt.setError(App.getContext().getString(R.string.required_field_error));
        } else if (!password.matches(pattern)) {
            passwordEt.setError(App.getContext().getString(R.string.password_bad_format_error));
        } else {
            passwordEt.setError(null);
        }
    }

    public static void validateRepeatPassword(EditText passwordEt, EditText repeatPasswordEt) {
        String password = passwordEt.getText().toString().trim();
        String repeatPassword = repeatPasswordEt.getText().toString().trim();

        if (repeatPassword.isEmpty()) {
            repeatPasswordEt.setError(App.getContext().getString(R.string.required_field_error));
        } else if (!password.equals(repeatPassword)) {
            repeatPasswordEt.setError(App.getContext().getString(R.string.register_repeat_password_error));
        } else {
            repeatPasswordEt.setError(null);
        }
    }

    public static void validateDisplayName(EditText displayNameEt) {
        String displayName = displayNameEt.getText().toString().trim();
        // From 2 to 12 alphanumeric characters, must begin with a letter
        String pattern = "^[A-Za-z][A-Za-z0-9]{1,11}$";

        if (displayName.isEmpty()) {
            displayNameEt.setError(App.getContext().getString(R.string.required_field_error));
        } else if (!displayName.matches(pattern)) {
            displayNameEt.setError(App.getContext().getString(R.string.register_display_name_format_error));
        } else {
            displayNameEt.setError(null);
        }
    }

    public static void validateFullName(EditText fullNameEt) {
        String fullName = fullNameEt.getText().toString().trim();
        // From 2 to 30 letter characters, must begin with a letter, spaces allowed
        String pattern = "^[A-Za-z][A-Za-z ]{1,29}$";

        if (fullName.isEmpty()) {
            fullNameEt.setError(App.getContext().getString(R.string.required_field_error));
        } else if (!fullName.matches(pattern)) {
            fullNameEt.setError(App.getContext().getString(R.string.register_full_name_format_error));
        } else {
            fullNameEt.setError(null);
        }
    }

    public static void validatePhone(EditText phoneEt) {
        String phone = phoneEt.getText().toString().trim();
        // Optionary "+" sign followed by 9 to 12 digits
        String pattern = "^\\+?[0-9]{9,12}$";

        if (phone.isEmpty()) {
            phoneEt.setError(App.getContext().getString(R.string.required_field_error));
        } else if (!phone.matches(pattern)) {
            phoneEt.setError(App.getContext().getString(R.string.register_phone_format_error));
        } else {
            phoneEt.setError(null);
        }
    }

    public static void validateAvatar(ImageView avatar, TextView errorTv) {
        if (avatar.getDrawable() == null) {
            errorTv.setError(App.getContext().getString(R.string.register_avatar_error));
        } else {
            errorTv.setError(null);
        }
    }
}
