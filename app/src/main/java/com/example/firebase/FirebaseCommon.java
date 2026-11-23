package com.example.firebase;

import com.example.data.CartProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseCommon {

    public interface OnResult<T> {
        void onResult(T result, Exception exception);
    }

    public enum QuantityChanging {
        INCREASE, DECREASE
    }

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public FirebaseCommon(FirebaseFirestore firestore, FirebaseAuth auth) {
        this.firestore = firestore;
        this.auth = auth;
    }

    private CollectionReference getCartCollection() {
        String uid = auth.getUid();
        if (uid == null) {
            uid = "";
        }
        return firestore.collection("user").document(uid).collection("cart");
    }

    public void addProductToCart(CartProduct cartProduct, OnResult<CartProduct> onResult) {
        getCartCollection().document().set(cartProduct)
                .addOnSuccessListener(aVoid -> {
                    if (onResult != null) {
                        onResult.onResult(cartProduct, null);
                    }
                })
                .addOnFailureListener(e -> {
                    if (onResult != null) {
                        onResult.onResult(null, e);
                    }
                });
    }

    public void increaseQuantity(String documentId, OnResult<String> onResult) {
        firestore.runTransaction(transaction -> {
            DocumentReference documentRef = getCartCollection().document(documentId);
            DocumentSnapshot document = transaction.get(documentRef);
            CartProduct productObject = document.toObject(CartProduct.class);
            if (productObject != null) {
                int newQuan = Math.max(1, productObject.getQuantity() + 1);
                CartProduct newProdObject = productObject.copy(newQuan);
                transaction.set(documentRef, newProdObject);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            if (onResult != null) {
                onResult.onResult(documentId, null);
            }
        }).addOnFailureListener(e -> {
            if (onResult != null) {
                onResult.onResult(null, e);
            }
        });
    }

    public void decreaseQuantity(String documentId, OnResult<String> onResult) {
        firestore.runTransaction(transaction -> {
            DocumentReference documentRef = getCartCollection().document(documentId);
            DocumentSnapshot document = transaction.get(documentRef);
            CartProduct productObject = document.toObject(CartProduct.class);
            if (productObject != null) {
                int currentQuan = productObject.getQuantity();
                if (currentQuan <= 1) {
                    // Reached 0/1: delete from cart
                    transaction.delete(documentRef);
                } else {
                    int newQuan = Math.max(1, currentQuan - 1);
                    CartProduct newProdObject = productObject.copy(newQuan);
                    transaction.set(documentRef, newProdObject);
                }
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            if (onResult != null) {
                onResult.onResult(documentId, null);
            }
        }).addOnFailureListener(e -> {
            if (onResult != null) {
                onResult.onResult(null, e);
            }
        });
    }
}
