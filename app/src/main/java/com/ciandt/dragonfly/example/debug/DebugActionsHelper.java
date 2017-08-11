package com.ciandt.dragonfly.example.debug;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import com.ciandt.dragonfly.example.R;
import com.ciandt.dragonfly.example.config.FirebaseConfig;
import com.ciandt.dragonfly.example.data.DatabaseManager;
import com.ciandt.dragonfly.example.data.ProjectRepository;
import com.ciandt.dragonfly.example.data.remote.RemoteProjectService;
import com.ciandt.dragonfly.example.features.feedback.model.Feedback;
import com.ciandt.dragonfly.example.features.login.LoginActivity;
import com.ciandt.dragonfly.example.infrastructure.extensions.String_ExtensionKt;
import com.ciandt.dragonfly.infrastructure.DragonflyLogger;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.palaima.debugdrawer.DebugDrawer;
import io.palaima.debugdrawer.actions.Action;
import io.palaima.debugdrawer.actions.ActionsModule;
import io.palaima.debugdrawer.actions.ButtonAction;
import io.palaima.debugdrawer.actions.SpinnerAction;
import io.palaima.debugdrawer.actions.SwitchAction;
import io.palaima.debugdrawer.commons.BuildModule;
import io.palaima.debugdrawer.commons.DeviceModule;
import io.palaima.debugdrawer.commons.SettingsModule;

/**
 * Created by Ivam Luz on 15/05/16.
 */
public class DebugActionsHelper {

    private static final String LOG_TAG = DebugActionsHelper.class.getSimpleName();

    public static DebugDrawer buildDebugDrawer(DebuggableActivity target) {
        Activity activity = target.getActivityInstance();

        return new DebugDrawer.Builder(activity).modules(
                new DeviceModule(activity),
                new BuildModule(activity),
                new SettingsModule(activity),
                new ActionsModule(DebugActionsHelper.getAllDebugActions(target)))
                .setDrawerListener(new DrawerLayout.DrawerListener() {

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Do nothing
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Do nothing
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Do nothing
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // Do nothing
                    }
                }).build();
    }

    public static Action[] getAllDebugActions(final DebuggableActivity target) {
        List<Action> actions = new ArrayList<>();

        actions.addAll(getButtonActions(target));
        actions.addAll(getSwitchActions());
        actions.addAll(getSpinnerActions());

        return actions.toArray(new Action[actions.size()]);
    }

    public static List<ButtonAction> getButtonActions(final DebuggableActivity target) {
        final List<ButtonAction> actions = new ArrayList<>();

        actions.add(new ButtonAction("Logout", new ButtonAction.Listener() {

            @Override
            public void onClick() {
                Context context = target.getActivityInstance();

                FirebaseAuth.getInstance().signOut();

                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context.getApplicationContext())
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();

                googleApiClient.connect();
                googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        if (googleApiClient.isConnected()) {
                            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                                    new ResultCallback<Status>() {

                                        @Override
                                        public void onResult(@NonNull Status status) {
                                            if (googleApiClient.isConnected()) {
                                                googleApiClient.clearDefaultAccountAndReconnect();
                                                googleApiClient.disconnect();
                                            }
                                        }
                                    }
                            );
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                });

                context.startActivity(LoginActivity.Companion.create(context));
            }
        }));

        actions.add(new ButtonAction("Clear projects database", new ButtonAction.Listener() {

            @Override
            public void onClick() {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        Context context = target.getActivityInstance();

                        ProjectRepository repository = new ProjectRepository(DatabaseManager.INSTANCE.getDatabase());
                        repository.clear();
                    }
                }).start();
            }
        }));

        actions.add(new ButtonAction("Start Firebase Project Service", new ButtonAction.Listener() {

            @Override
            public void onClick() {
                Context context = target.getActivityInstance();
                context.startService(new Intent(context, RemoteProjectService.class));
            }
        }));

        actions.add(new ButtonAction("Stop Firebase Project Service", new ButtonAction.Listener() {

            @Override
            public void onClick() {
                Context context = target.getActivityInstance();
                context.stopService(new Intent(context, RemoteProjectService.class));
            }
        }));

        actions.add(new ButtonAction("Save single file to GCS", new ButtonAction.Listener() {

            @Override
            public void onClick() {
                Context context = target.getActivityInstance();

                try {
                    final InputStream stream = context.getAssets().open("sunflower.jpg");

                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                    String fileName = "";
                    StorageReference imageReference = storageRef.child("ciandt/0fV9DGC75FSrm8hagLu0SZId8gg2/file.jpg");
                    StorageMetadata metadata = new StorageMetadata.Builder()
                            .setContentType("image/jpeg")
                            .build();

                    UploadTask uploadTask = imageReference.putStream(stream, metadata);

                    uploadTask.addOnFailureListener(new OnFailureListener() {

                        @Override
                        public void onFailure(@NonNull Exception e) {
                            try {
                                stream.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }

                            DragonflyLogger.error(LOG_TAG, "uploadTask - failure | error: " + e.getMessage());
                        }
                    });

                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            StorageMetadata metadata = task.getResult().getMetadata();
                            String gcsPath = metadata.getBucket() + "/" + metadata.getPath();

                            DragonflyLogger.debug(LOG_TAG, "uploadTask - success | feedback successfully updated with gcsPath: " + gcsPath);
                        }
                    });

                    uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = 100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                            DragonflyLogger.debug(LOG_TAG, "Upload is " + progress + "done");
                        }
                    });

                    uploadTask.addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                            DragonflyLogger.debug(LOG_TAG, "Upload is paused");
                        }
                    });
                } catch (IOException e) {
                    DragonflyLogger.debug(LOG_TAG, e.getMessage());
                }
            }
        }));

        actions.add(new ButtonAction("Save multiple files to GCS", new ButtonAction.Listener() {

            @Override
            public void onClick() {
                final CountDownLatch mainCountDownLatch = new CountDownLatch(1);

                String startAt = "ciandt__0fV9DGC75FSrm8hagLu0SZId8gg2__";
                String endAt = "ciandt__0fV9DGC75FSrm8hagLu0SZId8gg2__\uf8ff";

                final StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                final DatabaseReference stashDbRef = databaseReference.child(FirebaseConfig.COLLECTION_FEEDBACK_STASH);

                ExecutorService executor = Executors.newSingleThreadExecutor();

                stashDbRef
                        .orderByChild("tenantUserProject")
                        .startAt(startAt)
                        .endAt(endAt)
                        .limitToFirst(FirebaseConfig.SYNC_ITEMS_PER_RUN)
                        .addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long childCount = 0;
                                if (dataSnapshot != null) {
                                    childCount = dataSnapshot.getChildrenCount();
                                }

                                if (childCount == 0) {
                                    mainCountDownLatch.countDown();
                                    return;
                                }

                                final CountDownLatch itemsCountDownLatch = new CountDownLatch((int) childCount);

                                for (final DataSnapshot child : dataSnapshot.getChildren()) {
                                    Feedback feedback = child.getValue(Feedback.class);
                                    if (feedback == null) {
                                        return;
                                    }

                                    try {
                                        final InputStream stream = new FileInputStream(feedback.getImageLocalPath());
                                        try {
                                            String fileName = String_ExtensionKt.lastSegment(feedback.getImageLocalPath(), File.separator);
                                            String path = String.format("%s/%s/%s", feedback.getTenant(), feedback.getUserId(), fileName);
                                            StorageReference imageReference = storageRef.child(path);

                                            StorageMetadata metadata = new StorageMetadata.Builder()
                                                    .setContentType("image/jpeg")
                                                    .build();

//                                    val feedbackKey = if (feedback.key != null)
//                                        feedback.key
//                                    else
//                                        databaseRef.child(FirebaseConfig.COLLECTION_FEEDBACK_STASH).push().key

//                                            stashDbRef.child(child.getKey()).setValue(feedback);


                                            imageReference.putStream(stream, metadata)
                                                    .addOnFailureListener(new OnFailureListener() {

                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            itemsCountDownLatch.countDown();
                                                            DragonflyLogger.debug(LOG_TAG, "itemsCountDownLatch decreased to " + itemsCountDownLatch.getCount());

                                                            try {
                                                                stream.close();
                                                            } catch (IOException e1) {
                                                                e1.printStackTrace();
                                                            }

                                                            DragonflyLogger.error(LOG_TAG, "uploadTask - failure | error: " + e.getMessage());
                                                        }
                                                    })
                                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            try {
                                                                stream.close();
                                                            } catch (IOException e1) {
                                                                e1.printStackTrace();
                                                            }

                                                            itemsCountDownLatch.countDown();
                                                            DragonflyLogger.debug(LOG_TAG, "itemsCountDownLatch decreased to " + itemsCountDownLatch.getCount());

                                                            StorageMetadata storageMetadata = taskSnapshot.getMetadata();
                                                            if (storageMetadata != null) {
                                                                String gcsPath = storageMetadata.getBucket() + "/" + storageMetadata.getPath();

                                                                DragonflyLogger.debug(LOG_TAG, "uploadTask - success | feedback successfully updated with gcsPath: " + gcsPath);
                                                            }
                                                        }
                                                    });
                                        } catch (Exception e) {
                                            try {
                                                stream.close();
                                            } catch (IOException e1) {
                                                e1.printStackTrace();
                                            }

                                            itemsCountDownLatch.countDown();
                                            DragonflyLogger.debug(LOG_TAG, "itemsCountDownLatch decreased to " + itemsCountDownLatch.getCount());
                                        }
                                    } catch (FileNotFoundException e) {
                                        DragonflyLogger.warn(LOG_TAG, e.getMessage());

                                        itemsCountDownLatch.countDown();
                                        DragonflyLogger.debug(LOG_TAG, "itemsCountDownLatch decreased to " + itemsCountDownLatch.getCount());
                                    }
                                }

                                try {
                                    itemsCountDownLatch.await();
                                    DragonflyLogger.debug(LOG_TAG, "itemsCountDownLatch wait finished");

                                    mainCountDownLatch.countDown();
                                } catch (InterruptedException ignored) {

                                }
                            }


                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                DragonflyLogger.error(LOG_TAG, databaseError.toString());
                            }
                        });

                try {
                    mainCountDownLatch.await();
                    DragonflyLogger.debug(LOG_TAG, "mainCountDownLatch wait finished");
                } catch (InterruptedException ignored) {

                }

//                final StorageReference storageRef = FirebaseStorage.getInstance().getReference();
//
//                final StorageMetadata metadata = new StorageMetadata.Builder()
//                        .setContentType("image/jpeg")
//                        .build();
//
//
//                new Thread(new Runnable() {
//
//                    List<String> images = Arrays.asList("sunflower", "frog");
//
//                    public void run() {
//                        for (final String image : images) {
//                            try {
//                                final InputStream streamFlower = context.getAssets().open(image + ".jpg");
//
//                                StorageReference imageReference = storageRef.child("ciandt/0fV9DGC75FSrm8hagLu0SZId8gg2/" + image + ".jpg");
//
//
//                                UploadTask uploadTask = imageReference.putStream(streamFlower, metadata);
//
//                                uploadTask.addOnFailureListener(new OnFailureListener() {
//
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        try {
//                                            streamFlower.close();
//                                        } catch (IOException e1) {
//                                            e1.printStackTrace();
//                                        }
//
//                                        DragonflyLogger.error(LOG_TAG, "uploadTask " + image + " - failure | error: " + e.getMessage());
//                                    }
//                                });
//
//                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//
//                                    @Override
//                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                                        try {
//                                            streamFlower.close();
//                                        } catch (IOException e1) {
//                                            e1.printStackTrace();
//                                        }
//
//                                        StorageMetadata metadata = task.getResult().getMetadata();
//                                        String gcsPath = metadata.getBucket() + "/" + metadata.getPath();
//
//                                        DragonflyLogger.debug(LOG_TAG, "uploadTask " + image + " - success | feedback successfully updated with gcsPath: " + gcsPath);
//                                    }
//                                });
//
//                                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//
//                                    @Override
//                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                                        double progress = 100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
//                                        DragonflyLogger.debug(LOG_TAG, "Upload is '" + image + "' " + progress + " done");
//                                    }
//                                });
//
//                                uploadTask.addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
//
//                                    @Override
//                                    public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
//                                        DragonflyLogger.debug(LOG_TAG, "Upload " + image + " is paused");
//                                    }
//                                });
//
//                            } catch (IOException e) {
//                                DragonflyLogger.debug(LOG_TAG, e.getMessage());
//                            }
//                        }
//                    }
//                }).start();

//
//                Thread flowerThread = new Thread(new Runnable() {
//
//                    public void run() {
//                        try {
//                            final InputStream streamFlower = context.getAssets().open("sunflower.jpg");
//
//                            StorageReference flowerReference = storageRef.child("ciandt/0fV9DGC75FSrm8hagLu0SZId8gg2/flower.jpg");
//
//
//                            UploadTask flowerUploadTask = flowerReference.putStream(streamFlower, metadata);
//
//                            flowerUploadTask.addOnFailureListener(new OnFailureListener() {
//
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    try {
//                                        streamFlower.close();
//                                    } catch (IOException e1) {
//                                        e1.printStackTrace();
//                                    }
//
//                                    DragonflyLogger.error(LOG_TAG, "uploadTask flower - failure | error: " + e.getMessage());
//                                }
//                            });
//
//                            flowerUploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//
//                                @Override
//                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                                    try {
//                                        streamFlower.close();
//                                    } catch (IOException e1) {
//                                        e1.printStackTrace();
//                                    }
//
//                                    StorageMetadata metadata = task.getResult().getMetadata();
//                                    String gcsPath = metadata.getBucket() + "/" + metadata.getPath();
//
//                                    DragonflyLogger.debug(LOG_TAG, "uploadTask flower - success | feedback successfully updated with gcsPath: " + gcsPath);
//                                }
//                            });
//
//                            flowerUploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//
//                                @Override
//                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                                    double progress = 100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
//                                    DragonflyLogger.debug(LOG_TAG, "Upload is flower " + progress + "done");
//                                }
//                            });
//
//                            flowerUploadTask.addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
//
//                                @Override
//                                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
//                                    DragonflyLogger.debug(LOG_TAG, "Upload flower is paused");
//                                }
//                            });
//
//                        } catch (IOException e) {
//                            DragonflyLogger.debug(LOG_TAG, e.getMessage());
//                        }
//
//                    }
//                });
//
//                Thread frogThread = new Thread(new Runnable() {
//
//                    public void run() {
//                        // your code goes here...
//                        try {
//                            StorageReference frogReference = storageRef.child("ciandt/0fV9DGC75FSrm8hagLu0SZId8gg2/frog.jpg");
//
//                            final InputStream streamFrog = context.getAssets().open("frog.jpg");
//                            UploadTask frogUploadTask = frogReference.putStream(streamFrog, metadata);
//
//                            frogUploadTask.addOnFailureListener(new OnFailureListener() {
//
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    try {
//                                        streamFrog.close();
//                                    } catch (IOException e1) {
//                                        e1.printStackTrace();
//                                    }
//
//                                    DragonflyLogger.error(LOG_TAG, "uploadTask frog - failure | error: " + e.getMessage());
//                                }
//                            });
//
//                            frogUploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//
//                                @Override
//                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                                    try {
//                                        streamFrog.close();
//                                    } catch (IOException e1) {
//                                        e1.printStackTrace();
//                                    }
//
//
//                                    StorageMetadata metadata = task.getResult().getMetadata();
//                                    String gcsPath = metadata.getBucket() + "/" + metadata.getPath();
//
//                                    DragonflyLogger.debug(LOG_TAG, "uploadTask frog  - success | feedback successfully updated with gcsPath: " + gcsPath);
//                                }
//                            });
//
//                            frogUploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//
//                                @Override
//                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                                    double progress = 100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
//                                    DragonflyLogger.debug(LOG_TAG, "Upload is frog " + progress + "done");
//                                }
//                            });
//
//                            frogUploadTask.addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
//
//                                @Override
//                                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
//                                    DragonflyLogger.debug(LOG_TAG, "Upload frog is paused");
//                                }
//                            });
//                        } catch (IOException e) {
//                            DragonflyLogger.debug(LOG_TAG, e.getMessage());
//                        }
//                    }
//                });
//
//                flowerThread.start();
//                frogThread.start();

            }
        }));

        return actions;
    }

    public static List<SwitchAction> getSwitchActions() {
        List<SwitchAction> actions = new ArrayList<>();

        return actions;
    }

    public static List<SpinnerAction> getSpinnerActions() {
        List<SpinnerAction> actions = new ArrayList<>();

        return actions;
    }

    public static void closeDebugDrawer(final DebuggableActivity target) {
        DebugDrawer debugDrawer = target.getCurrentDebugDrawer();

        if (debugDrawer != null && debugDrawer.isDrawerOpen()) {
            debugDrawer.closeDrawer();
        }
    }

    public interface DebuggableActivity {

        DebugDrawer getCurrentDebugDrawer();

        DebugDrawer buildNewDebugDrawer();

        Activity getActivityInstance();
    }
}
