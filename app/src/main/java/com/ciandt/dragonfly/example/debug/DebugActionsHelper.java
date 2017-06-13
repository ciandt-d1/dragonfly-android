package com.ciandt.dragonfly.example.debug;

import android.Manifest;
import android.app.Activity;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
        List<Action> actions = new ArrayList<Action>();

        actions.addAll(getButtonActions(target));
        actions.addAll(getSwitchActions());
        actions.addAll(getSpinnerActions());

        return actions.toArray(new Action[actions.size()]);
    }

    public static List<ButtonAction> getButtonActions(final DebuggableActivity target) {
        final List<ButtonAction> actions = new ArrayList<ButtonAction>();

        actions.add(new ButtonAction("Copy model from assets", new ButtonAction.Listener() {

            @Override
            public void onClick() {
                Dexter.withActivity(target.getActivityInstance())
                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {

                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                copyTensorFlowModelFromAssetsToExternalStorage(target);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                Log.d(LOG_TAG, String.format("onPermissionRationaleShouldBeShown() - PermissionDeniedResponse: %s", response));
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                Log.d(LOG_TAG, String.format("onPermissionRationaleShouldBeShown() - PermissionRequest: %s, PermissionToken: %s", permission, token));
                            }
                        }).check();
            }
        }));

        return actions;
    }

    public static List<SwitchAction> getSwitchActions() {
        List<SwitchAction> actions = new ArrayList<SwitchAction>();

        return actions;
    }

    public static List<SpinnerAction> getSpinnerActions() {
        List<SpinnerAction> actions = new ArrayList<SpinnerAction>();

        return actions;
    }

    public static void closeDebugDrawer(final DebuggableActivity target) {
        DebugDrawer debugDrawer = target.getCurrentDebugDrawer();

        if (debugDrawer != null && debugDrawer.isDrawerOpen()) {
            debugDrawer.closeDrawer();
        }
    }

    private static void copyTensorFlowModelFromAssetsToExternalStorage(final DebuggableActivity target)

    {
        AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                Toast.makeText(target.getActivityInstance(), "Copying model from assets...", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                Boolean result;
                try {
                    result = copyModelFromAssets("models/1/model.pb") && copyModelFromAssets("models/1/labels.txt");
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    result = false;
                }

                return result;

            }

            @NonNull
            private Boolean copyModelFromAssets(String srcFilePath) throws Exception {
                byte[] buffer = new byte[1024];

                File outputFile = new File(target.getActivityInstance().getFilesDir(), srcFilePath);

                InputStream is = null;
                OutputStream os = null;

                boolean result = false;
                try {
                    is = target.getActivityInstance().getAssets().open(srcFilePath);
                    os = new FileOutputStream(outputFile);

                    int n;
                    while (-1 != (n = is.read(buffer))) {
                        os.write(buffer, 0, n);
                    }

                    result = true;
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage(), e);

                    result = false;
                } finally {
                    if (is != null) {
                        is.close();
                    }

                    if (os != null) {
                        os.close();
                    }
                }

                return result;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);

                if (result) {
                    Toast.makeText(target.getActivityInstance(), "Model successfully copied!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(target.getActivityInstance(), "Failed to copy model!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public interface DebuggableActivity {

        DebugDrawer getCurrentDebugDrawer();

        DebugDrawer buildNewDebugDrawer();

        Activity getActivityInstance();
    }
}
