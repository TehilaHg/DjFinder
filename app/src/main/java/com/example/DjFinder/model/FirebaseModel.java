package com.example.DjFinder.model;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MemoryCacheSettings;
import com.google.firebase.firestore.MemoryLruGcSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FirebaseModel {
    FirebaseFirestore db;
    FirebaseStorage storage;
    private FirebaseAuth mAuth;

    // CTOR not public - reachable only from classes in the package
    FirebaseModel(){
        db = FirebaseFirestore.getInstance();

        MemoryCacheSettings memoryCacheSettings = MemoryCacheSettings.newBuilder()
                .setGcSettings(MemoryLruGcSettings.newBuilder()
                        .setSizeBytes(0)
                        .build())
                .build();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(memoryCacheSettings)
                .build();
        db.setFirestoreSettings(settings);
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

    }

    public void getAllPostsSince(Long since, Model.Listener<List<Post>> callback){
        db.collection(Post.COLLECTION)
                .whereGreaterThanOrEqualTo(Post.LAST_UPDATED, new Timestamp(since,0))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Post> list = new LinkedList<>();
                        if(task.isSuccessful()){
                            QuerySnapshot jsonsList = task.getResult();
                            for (DocumentSnapshot json : jsonsList){
                                // Factory
                                Post post = Post.fromJson(json.getData());
                                list.add(post);
                            }
                        }
                        callback.onComplete(list);
                    }
                });
    }

    public void getAllUsersSince(Long since, Model.Listener<List<User>> callback) {
        db.collection(User.COLLECTION)
                .whereGreaterThan(User.LAST_UPDATED,new Timestamp(since,0))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    List<User> data = new ArrayList<>();
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for (DocumentSnapshot document:task.getResult()){
                                data.add(User.fromJson(document.getData()));
                            }
                            callback.onComplete(data);
                        }
                    }
                });
    }

    public void addPost(Post post, Model.Listener<Void> listener){
        db.collection(Post.COLLECTION).document(post.getId()).set(post.toJson()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listener.onComplete(null);
            }
        });
    }

    void uploadImage (String name, Bitmap bitmap, Model.Listener<String> listener)
    {
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        // Create a reference to 'images/"name".jpg'
        StorageReference imagesRef = storageRef.child("images/" + name + ".jpg");// Get the data from an ImageView as bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imagesRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                listener.onComplete(null);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        listener.onComplete(uri.toString());
                    }
                });
            }
        });
    }

    public void getUser(Model.Listener<Boolean> listener){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        listener.onComplete(currentUser!=null);
    }

    public void signUp(User newUser, String password, Model.Listener<Void> listener) {
        mAuth.createUserWithEmailAndPassword(newUser.email, password)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(newUser.userName)
                                    .build();
                            user.updateProfile(profile)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            // Now that the profile is updated, we can add the user to Firestore
                                            db.collection(User.COLLECTION).document(newUser.userName).set(User.toJson(newUser))
                                                    .addOnCompleteListener(firestoreTask -> {
                                                        if (firestoreTask.isSuccessful()) {
                                                            Model.getInstance().username = newUser.userName;
                                                            Model.getInstance().refreshAllUsers();
                                                            listener.onComplete(null);
                                                        } else {
                                                            listener.onComplete(null);
                                                            Log.e("FirebaseModel", "Error creating user document", firestoreTask.getException());
                                                        }
                                                    });
                                        } else {
                                            listener.onComplete(null);
                                            Log.e("FirebaseModel", "Error updating user profile", profileTask.getException());
                                        }
                                    });
                        } else {
                            listener.onComplete(null);
                            Log.e("FirebaseModel", "User is null after successful creation");
                        }
                    } else {
                        listener.onComplete(null);
                        Log.e("FirebaseModel", "Error creating user", authTask.getException());
                    }
                });
    }

    public void logIn(String username, String password, Model.Listener<Boolean> listener) {
        db.collection(User.COLLECTION).document(username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            User user = User.fromJson(task.getResult().getData());
                            mAuth.signInWithEmailAndPassword(user.email, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            listener.onComplete(task.isSuccessful());
                                        }
                                    });
                        } else {
                            listener.onComplete(false);
                        }
                    }
                });
    }


    public void getUserByUsername(String username,Model.Listener<User> listener) {
        db.collection(User.COLLECTION).document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                listener.onComplete(User.fromJson(task.getResult().getData()));
            }
        });
    }

    public boolean isLoggedIn(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser!=null;
    }

    public String getLoggedUserUsername() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getDisplayName();
        }
        return null; // או ערך ברירת מחדל אחר
    }

    public void logOut() {
        mAuth.signOut();
    }

    public void deletePost(Post post) {
        db.collection(Post.COLLECTION).document(post.id).delete();
    }

    public void updatePost(Post post, Model.Listener<Void> listener) {
        db.collection(Post.COLLECTION).document(post.id).update(post.toJson()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Model.getInstance().refreshAllPosts();
                listener.onComplete(null);
            }
        });
    }

    public void updateLikedPosts(User user) {
        db.collection(User.COLLECTION).document(user.userName).update(User.toJson(user))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {}
                });
    }

    public void updateUser(User user, String oldUsername, Model.Listener<Void> listener) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Log.e("FirebaseModel", "updateUser: FirebaseUser is null");
            listener.onComplete(null);
            return;
        }

        Log.d("FirebaseModel", "updateUser: Updating user from " + oldUsername + " to " + user.userName);

        // עדכון המסמך הקיים
        db.collection(User.COLLECTION).document(oldUsername)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // המסמך קיים, נעדכן אותו
                        db.collection(User.COLLECTION).document(oldUsername)
                                .set(User.toJson(user))
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("FirebaseModel", "updateUser: User document updated successfully");
                                    // אם שם המשתמש השתנה, נעדכן את המזהה של המסמך
                                    if (!oldUsername.equals(user.userName)) {
                                        db.collection(User.COLLECTION).document(oldUsername)
                                                .delete()
                                                .addOnSuccessListener(v -> {
                                                    Log.d("FirebaseModel", "updateUser: Old document deleted successfully");
                                                    db.collection(User.COLLECTION).document(user.userName)
                                                            .set(User.toJson(user))
                                                            .addOnSuccessListener(unused -> {
                                                                Log.d("FirebaseModel", "updateUser: New document created successfully");
                                                                updateAuthProfile(firebaseUser, user, oldUsername, listener);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e("FirebaseModel", "updateUser: Error creating new document: " + e.getMessage());
                                                                listener.onComplete(null);
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("FirebaseModel", "updateUser: Error deleting old document: " + e.getMessage());
                                                    listener.onComplete(null);
                                                });
                                    } else {
                                        updateAuthProfile(firebaseUser, user, oldUsername, listener);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirebaseModel", "updateUser: Error updating user document: " + e.getMessage());
                                    listener.onComplete(null);
                                });
                    } else {
                        // המסמך לא קיים, ניצור אחד חדש
                        Log.d("FirebaseModel", "updateUser: User document doesn't exist, creating new one");
                        db.collection(User.COLLECTION).document(user.userName)
                                .set(User.toJson(user))
                                .addOnSuccessListener(unused -> {
                                    Log.d("FirebaseModel", "updateUser: New user document created successfully");
                                    updateAuthProfile(firebaseUser, user, oldUsername, listener);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirebaseModel", "updateUser: Error creating new user document: " + e.getMessage());
                                    listener.onComplete(null);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseModel", "updateUser: Error checking user document existence: " + e.getMessage());
                    listener.onComplete(null);
                });
    }

    private void updateAuthProfile(FirebaseUser firebaseUser, User user, String oldUsername, Model.Listener<Void> listener) {
        Log.d("FirebaseModel", "updateAuthProfile: Updating auth profile for user " + user.userName);
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(user.userName)
                .build();

        firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseModel", "updateAuthProfile: Auth profile updated successfully");
                        updateReferences(oldUsername, user.userName, listener);
                    } else {
                        Log.e("FirebaseModel", "updateAuthProfile: Error updating Auth profile: " + task.getException().getMessage());
                        listener.onComplete(null);
                    }
                });
    }

    private void updateReferences(String oldUsername, String newUsername, Model.Listener<Void> listener) {
        Log.d("FirebaseModel", "updateReferences: Updating posts for user from " + oldUsername + " to " + newUsername);
        // עדכון התייחסויות בפוסטים
        db.collection(Post.COLLECTION).whereEqualTo("username", oldUsername)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    int count = 0;
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        batch.update(document.getReference(), "username", newUsername);
                        count++;
                    }
                    Log.d("FirebaseModel", "updateReferences: Found " + count + " posts to update");
                    if (count > 0) {
                        int finalCount = count;
                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("FirebaseModel", "updateReferences: Successfully updated " + finalCount + " posts");
                                    listener.onComplete(null);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirebaseModel", "updateReferences: Error updating posts: " + e.getMessage());
                                    listener.onComplete(null);
                                });
                    } else {
                        Log.d("FirebaseModel", "updateReferences: No posts to update");
                        listener.onComplete(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseModel", "updateReferences: Error querying posts: " + e.getMessage());
                    listener.onComplete(null);
                });
    }




    public void isUsernameTaken(String username, Model.Listener<Boolean> listener) {
        db.collection(User.COLLECTION).document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    listener.onComplete(task.getResult().getData()!=null);
                }
            }
        });
    }

    public void isEmailTaken(String email, Model.Listener<Boolean> listener) {
        db.collection(User.COLLECTION).whereEqualTo(User.EMAIL,email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    listener.onComplete(!task.getResult().getDocuments().isEmpty());
                }
            }
        });
    }


    public void updatePostsUsername(String oldUsername, String newUsername, Runnable onComplete) {
        db.collection("posts")
                .whereEqualTo("userName", oldUsername)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        batch.update(document.getReference(), "userName", newUsername);
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> onComplete.run())
                            .addOnFailureListener(e -> {
                                Log.e("FirebaseModel", "Error updating posts username", e);
                                onComplete.run();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseModel", "Error querying posts", e);
                    onComplete.run();
                });
    }
}