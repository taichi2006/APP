package com.example.feature.auth.repository;

import com.example.model.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthRepository {

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    @Inject
    public AuthRepository(FirebaseAuth auth, FirebaseFirestore firestore) {
        this.auth = auth;
        this.firestore = firestore;
    }

    public FirebaseUser getCurrentUser() { return auth.getCurrentUser(); }
    public String getCurrentUserId() { return auth.getUid(); }

    public void login(String email, String password, Callback<FirebaseUser> callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(res -> { if (callback != null) callback.onSuccess(res.getUser()); })
                .addOnFailureListener(e -> { if (callback != null) callback.onError(e.getMessage()); });
    }

    public void register(User user, String password, Callback<User> callback) {
        auth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnSuccessListener(res -> {
                    String uid = res.getUser() != null ? res.getUser().getUid() : null;
                    if (uid != null) saveUserInfo(uid, user, callback);
                    else if (callback != null) callback.onError("Không thể lấy mã định danh người dùng");
                })
                .addOnFailureListener(e -> { if (callback != null) callback.onError(e.getMessage()); });
    }

    public void saveUserInfo(String uid, User user, Callback<User> callback) {
        firestore.collection("user").document(uid).set(user)
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(user); })
                .addOnFailureListener(e -> { if (callback != null) callback.onError(e.getMessage()); });
    }

    public void resetPassword(String email, Callback<Void> callback) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(null); })
                .addOnFailureListener(e -> { if (callback != null) callback.onError(e.getMessage()); });
    }

    public void getUserProfile(String uid, Callback<User> callback) {
        firestore.collection("user").document(uid).get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    if (user != null && callback != null) callback.onSuccess(user);
                    else if (callback != null) callback.onError("Không tìm thấy thông tin người dùng");
                })
                .addOnFailureListener(e -> { if (callback != null) callback.onError(e.getMessage()); });
    }

    public void signInWithGoogle(String idToken, Callback<FirebaseUser> callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(res -> {
                    FirebaseUser fbUser = res.getUser();
                    if (fbUser != null) {
                        firestore.collection("user").document(fbUser.getUid()).get()
                                .addOnSuccessListener(doc -> {
                                    if (!doc.exists()) {
                                        String name = fbUser.getDisplayName() != null ? fbUser.getDisplayName().trim() : "";
                                        String[] p = name.split(" ");
                                        String first = p.length > 0 ? p[0] : "";
                                        String last = p.length > 1 ? name.substring(first.length()).trim() : "";
                                        User u = new User(first, last, fbUser.getEmail() != null ? fbUser.getEmail() : "", fbUser.getPhotoUrl() != null ? fbUser.getPhotoUrl().toString() : "");
                                        firestore.collection("user").document(fbUser.getUid()).set(u);
                                    }
                                    if (callback != null) callback.onSuccess(fbUser);
                                })
                                .addOnFailureListener(e -> { if (callback != null) callback.onSuccess(fbUser); });
                    }
                })
                .addOnFailureListener(e -> { if (callback != null) callback.onError(e.getMessage()); });
    }

    public void logout() {
        auth.signOut();
    }
}
