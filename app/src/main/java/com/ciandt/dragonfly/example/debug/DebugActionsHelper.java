package com.ciandt.dragonfly.example.debug;

import com.ciandt.dragonfly.example.data.DatabaseManager;
import com.ciandt.dragonfly.example.data.ProjectRepository;
import com.ciandt.dragonfly.example.data.remote.RemoteProjectService;
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.ArrayList;
import java.util.List;

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

        actions.add(new ButtonAction("Set flowers update", new ButtonAction.Listener() {

            @Override
            public void onClick() {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        ProjectRepository repository = new ProjectRepository(DatabaseManager.INSTANCE.getDatabase());
                        repository.updateVersionStatus("scotts-v1-quantized", 1, 2);
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

        actions.add(new ButtonAction("Get Firebase ID Token", new ButtonAction.Listener() {

            @Override
            public void onClick() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    DragonflyLogger.INSTANCE.warn(LOG_TAG, "No user signed in.", null);
                }

                user.getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {

                    @Override
                    public void onSuccess(GetTokenResult getTokenResult) {
                        DragonflyLogger.INSTANCE.info(LOG_TAG, String.format("Firebase ID Token: %s", getTokenResult.getToken()));
                    }
                }).addOnFailureListener(new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        DragonflyLogger.INSTANCE.error(LOG_TAG, e);
                    }
                });
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
