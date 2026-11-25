package com.example.feature.profile.repository;

import com.example.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ProfileRepository {

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final StorageReference storage;

    @Inject
    public ProfileRepository(FirebaseFirestore firestore, FirebaseAuth auth, StorageReference storage) {
        this.firestore = firestore;
        this.auth = auth;
        this.storage = storage;
    }

    public void getUserProfile(Callback<User> callback) {
        String uid = auth.getUid();
        if (uid == null) {
            if (callback != null) callback.onError("Chưa đăng nhập");
            return;
        }

        firestore.collection("user").document(uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        if (callback != null) callback.onError(error.getMessage() != null ? error.getMessage() : "Lỗi tải thông tin");
                    } else if (value != null) {
                        User user = value.toObject(User.class);
                        if (user != null && callback != null) {
                            callback.onSuccess(user);
                        }
                    }
                });
    }

    public void updateUser(User user, byte[] imageBytes, Callback<User> callback) {
        String uid = auth.getUid();
        if (uid == null) {
            if (callback != null) callback.onError("Chưa đăng nhập");
            return;
        }

        if (imageBytes != null && imageBytes.length > 0) {
            StorageReference imageRef = storage.child("profileImages/" + uid + "/" + UUID.randomUUID().toString());
            imageRef.putBytes(imageBytes)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri ->
                            saveUserData(uid, user.copy(uri.toString()), false, callback)))
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onError(e.getMessage());
                    });
        } else {
            saveUserData(uid, user, true, callback);
        }
    }

    private void saveUserData(String uid, User user, boolean keepOldImg, Callback<User> callback) {
        firestore.runTransaction(transaction -> {
            DocumentReference ref = firestore.collection("user").document(uid);
            if (keepOldImg) {
                User current = transaction.get(ref).toObject(User.class);
                String oldImg = (current != null && current.getImagePath() != null) ? current.getImagePath() : "";
                transaction.set(ref, user.copy(oldImg));
            } else {
                transaction.set(ref, user);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            if (callback != null) callback.onSuccess(user);
        }).addOnFailureListener(e -> {
            if (callback != null) callback.onError(e.getMessage());
        });
    }

    public void signOut() {
        auth.signOut();
    }
}
