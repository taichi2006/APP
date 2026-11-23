package com.example.feature.auth;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.example.data.User;
import com.example.util.RegisterValidation;
import com.example.util.Resource;
import com.example.util.ValidationCheck;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;

@HiltViewModel
public class UserAccountViewModel extends AndroidViewModel {
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final StorageReference storage;

    private final MutableStateFlow<Resource<User>> _user;
    private final MutableStateFlow<Resource<User>> _updateInfo;

    @Inject
    public UserAccountViewModel(
        FirebaseFirestore firestore,
        FirebaseAuth auth,
        StorageReference storage,
        @NonNull Application application
    ) {
        super(application);
        this.firestore = firestore;
        this.auth = auth;
        this.storage = storage;
        this._user = StateFlowKt.MutableStateFlow(new Resource.Unspecified<>());
        this._updateInfo = StateFlowKt.MutableStateFlow(new Resource.Unspecified<>());

        getUserData();
    }

    public StateFlow<Resource<User>> getUser() {
        return _user;
    }

    public StateFlow<Resource<User>> getUpdateInfo() {
        return _updateInfo;
    }

    public void getUserData() {
        _user.setValue(new Resource.Loading<>());
        String uid = auth.getUid();
        if (uid == null) {
            _user.setValue(new Resource.Error<>("Chưa đăng nhập"));
            return;
        }

        firestore.collection("user").document(uid).get()
            .addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    _user.setValue(new Resource.Success<>(user));
                }
            })
            .addOnFailureListener(e -> {
                _user.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi tải thông tin"));
            });
    }

    public void updateUser(User user, Uri imageUri) {
        boolean areInputValid = ValidationCheck.validateEmail(user.getEmail()) instanceof RegisterValidation.Success
            && user.getFirstName() != null && !user.getFirstName().trim().isEmpty()
            && user.getLastName() != null && !user.getLastName().trim().isEmpty();

        if (!areInputValid) {
            _updateInfo.setValue(new Resource.Error<>("Check your inputs!"));
            return;
        }

        _updateInfo.setValue(new Resource.Loading<>());

        if (imageUri == null) {
            saveUserInformation(user, true);
        } else {
            saveUserInfoWithNewImg(user, imageUri);
        }
    }

    private void saveUserInformation(User user, boolean shouldRetrieveOldImg) {
        String uid = auth.getUid();
        if (uid == null) return;

        firestore.runTransaction(transaction -> {
            DocumentReference ref = firestore.collection("user").document(uid);
            if (shouldRetrieveOldImg) {
                User currentUser = transaction.get(ref).toObject(User.class);
                String oldImg = (currentUser != null && currentUser.getImagePath() != null) ? currentUser.getImagePath() : "";
                User newUser = user.copy(oldImg);
                transaction.set(ref, newUser);
            } else {
                transaction.set(ref, user);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            _updateInfo.setValue(new Resource.Success<>(user));
        }).addOnFailureListener(e -> {
            _updateInfo.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi lưu thông tin"));
        });
    }

    private void saveUserInfoWithNewImg(User user, Uri imageUri) {
        try {
            Bitmap imgBitMap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            imgBitMap.compress(Bitmap.CompressFormat.JPEG, 96, byteArrayOutputStream);
            byte[] imgByteArray = byteArrayOutputStream.toByteArray();

            String uid = auth.getUid();
            StorageReference imgDirectory = storage.child("profileImages/" + uid + "/" + UUID.randomUUID().toString());
            imgDirectory.putBytes(imgByteArray)
                .addOnSuccessListener(taskSnapshot -> {
                    imgDirectory.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            saveUserInformation(user.copy(uri.toString()), false);
                        })
                        .addOnFailureListener(e -> {
                            _updateInfo.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi tải ảnh"));
                        });
                })
                .addOnFailureListener(e -> {
                    _updateInfo.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi tải ảnh"));
                });
        } catch (IOException e) {
            _updateInfo.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi đọc ảnh"));
        }
    }
}
