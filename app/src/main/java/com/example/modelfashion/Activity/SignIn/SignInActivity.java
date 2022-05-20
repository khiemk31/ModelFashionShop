package com.example.modelfashion.Activity.SignIn;

import static com.example.modelfashion.Utility.Constants.KEY_ACTIVE;
import static com.example.modelfashion.Utility.Constants.KEY_ADDRESS;
import static com.example.modelfashion.Utility.Constants.KEY_AVARTAR;
import static com.example.modelfashion.Utility.Constants.KEY_BIRTHDAY;
import static com.example.modelfashion.Utility.Constants.KEY_CHECK_BOX;
import static com.example.modelfashion.Utility.Constants.KEY_FULL_NAME;
import static com.example.modelfashion.Utility.Constants.KEY_ID;
import static com.example.modelfashion.Utility.Constants.KEY_MAT_KHAU;
import static com.example.modelfashion.Utility.Constants.KEY_PHONE;
import static com.example.modelfashion.Utility.Constants.KEY_SEX;
import static com.example.modelfashion.Utility.Constants.KEY_TAI_KHOAN;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.modelfashion.Activity.MainActivity;
import com.example.modelfashion.Common.ProgressLoadingCommon;
import com.example.modelfashion.Model.response.Login.LoginRequest;
import com.example.modelfashion.Model.response.Login.LoginResponse;
import com.example.modelfashion.R;
import com.example.modelfashion.Utility.Constants;
import com.example.modelfashion.Utility.PreferenceManager;
import com.example.modelfashion.network.ApiClient;
import com.example.modelfashion.network.ApiInterface;
import com.example.modelfashion.network.Repository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.util.regex.Pattern;

import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    ImageView btn_back;
    EditText edtAccount, edtPassword;
    Button btnLogin;
    TextView tvSignUp, tvForgotPassword;
    CheckBox cbSaveValue;
    PreferenceManager sharedPreferences;
    ApiInterface apiInterface;
    ProgressLoadingCommon progressLoadingCommon;
    CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        sharedPreferences = new PreferenceManager(this);
        viewHolder();
        setListener();
        layThongTinDangNhap();
    }

    // tham chiếu
    private void viewHolder() {
        btn_back = findViewById(R.id.btn_signIn_back);
        edtAccount = findViewById(R.id.edtAccount);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        cbSaveValue = findViewById(R.id.cbSaveValue);
        apiInterface = ApiClient.provideApiInterface(SignInActivity.this);
        progressLoadingCommon = new ProgressLoadingCommon();
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private Boolean validate() {
        Pattern special = Pattern.compile("[!#$%&*^()_+=|<>?{}\\[\\]~-]");
        if (edtAccount.getText().toString().trim().isEmpty() || edtPassword.getText().toString().trim().isEmpty()) {
            Toast.makeText(SignInActivity.this, "Không để trống số điện thoại hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (special.matcher(edtAccount.getText().toString().trim()).find() || special.matcher(edtPassword.getText().toString().trim()).find()) {
            Toast.makeText(SignInActivity.this, "Không được viết kí tự đặc biệt", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (edtPassword.getText().toString().trim().length() < 6) {
            Toast.makeText(SignInActivity.this, "Mật khẩu ít nhất 6 kí tự", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //thêm chức năng vào các nút bấm
    private void setListener() {
        btn_back.setOnClickListener(v -> {
            onBackPressed();
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    progressLoadingCommon.showProgressLoading(SignInActivity.this);
                    login();
                }
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignInActivity.this, ForgotPasswordActivity.class));
            }
        });
    }

    private void login() {
        Repository repository = new Repository(this);
        disposable.add(repository.login(new LoginRequest(edtAccount.getText().toString().trim(),
                edtPassword.getText().toString().trim())) // truyen phone va password vao day
                .doOnSubscribe(disposable -> {
                    // hien loading
                }).subscribe(loginResponse -> {
                    if (cbSaveValue.isChecked()) {
                        luuThongTinDangNhap();
                    } else {
                        sharedPreferences.putString(KEY_ID, loginResponse.getData());
                    }

                    sharedPreferences.putString(KEY_ID, loginResponse.getData());
                    sharedPreferences.putBoolean(Constants.KEY_CHECK_LOGIN, true);
                    Toast.makeText(SignInActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    getUserDetail(loginResponse.getData());
                }, throwable -> {
                    Toast.makeText(SignInActivity.this, "Error Happened.Try again!", Toast.LENGTH_SHORT).show();
                }));
    }

    private void luuThongTinDangNhap() {
        sharedPreferences.putString(KEY_TAI_KHOAN, edtAccount.getText().toString().trim());
        sharedPreferences.putString(KEY_MAT_KHAU, edtPassword.getText().toString().trim());
        sharedPreferences.putBoolean(KEY_CHECK_BOX, cbSaveValue.isChecked());
    }

    private void layThongTinDangNhap() {
        edtAccount.setText(sharedPreferences.getString(KEY_TAI_KHOAN));
        edtPassword.setText(sharedPreferences.getString(KEY_MAT_KHAU));
        cbSaveValue.setChecked(sharedPreferences.getBoolean(KEY_CHECK_BOX));
    }

    private void getUserDetail(String userId) {
        Repository repository = new Repository(this);
        disposable.add(repository.getUserDetail(userId)
                .doOnSubscribe(disposable -> {
                    progressLoadingCommon.showProgressLoading(this);
                }).subscribe(userDetailResponse -> {
                    Log.d("ahuhu", "userDetailResponse: " + userDetailResponse.toString());

                    sharedPreferences.putString(KEY_ID, userDetailResponse.getData().getUserId());
                    sharedPreferences.putString(KEY_AVARTAR, userDetailResponse.getData().getAvatar());
                    sharedPreferences.putString(KEY_FULL_NAME, userDetailResponse.getData().getUsername());
                    sharedPreferences.putInt(KEY_ACTIVE, userDetailResponse.getData().getActive());
                    sharedPreferences.putString(KEY_PHONE, userDetailResponse.getData().getPhone());
                    sharedPreferences.putString(KEY_BIRTHDAY, userDetailResponse.getData().getDateOfBirth());
                    sharedPreferences.putString(KEY_ADDRESS, userDetailResponse.getData().getAddress());
                    sharedPreferences.putInt(KEY_SEX, userDetailResponse.getData().getGender());


//                    Log.e("register", String.valueOf(registerResponse.toString()));
//
//                    try {
//                        JSONObject obj = new JSONObject(registerResponse.toString());
//                        String fullName = obj.getString("userName");
//                        preferenceManager.putString(Constants.KEY_FULL_NAME, fullName);
//                        preferenceManager.putString(Constants.KEY_PHONE, obj.getString("phone"));
//                        preferenceManager.putString(Constants.KEY_ADDRESS, obj.getString("address"));
//                        preferenceManager.putString(Constants.KEY_BIRTHDAY, obj.getString("dateOfBirth"));
//                        preferenceManager.putInt(Constants.KEY_SEX, obj.getInt("gender"));
//                        preferenceManager.putString(Constants.KEY_AVARTAR, obj.getString("avatar"));
//                        Log.d("My App", obj.toString());
//
//                    } catch (Throwable t) {
//                        Log.e("My App", "Could not parse malformed JSON: \"" + registerResponse.toString() + "\"");
//                    }
//
//                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
//                    startActivity(intent);
                }, throwable -> {
                    Toast.makeText(SignInActivity.this, "Error Happened.Try again!", Toast.LENGTH_SHORT).show();
                    Log.d("ahuhu", "userDetailResponse: error" + throwable.toString());
                }));
    }

    //chặn back
    @Override
    public void onBackPressed() {
        super.onBackPressed();  // optional depending on your needs
    }

    @Override
    protected void onDestroy() {
        disposable.clear();
        super.onDestroy();
    }


}