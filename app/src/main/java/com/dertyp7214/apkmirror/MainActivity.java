package com.dertyp7214.apkmirror;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.dertyp7214.apkmirror.components.BottomPopup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.dertyp7214.apkmirror.Utils.apps;


public class MainActivity extends AppCompatActivity {

    private App app;
    private TextView publisher, description, version;
    private ImageView appIcon, appBackground;
    private CollapsingToolbarLayout collapsingToolbar;
    private AppBarLayout appBarLayout;
    private Menu collapsedMenu;
    private Button open, uninstall;
    private String packageName;
    private PackageManager packageManager;
    private List<Version> versions = new ArrayList<>();
    private RecyclerView recyclerView;
    private AppVersionAdapter appVersionAdapter;
    private FloatingActionButton fab;
    private BottomPopup description_popup;
    private boolean appBarExpanded;
    private boolean launchable;
    private static int notifyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        collapsingToolbar = findViewById(R.id.toolbar_layout);
        appBarLayout = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appBackground = findViewById(R.id.app_background);
        appIcon = findViewById(R.id.app_icon);
        open = findViewById(R.id.btn_open);
        uninstall = findViewById(R.id.btn_remove);
        publisher = findViewById(R.id.txt_publisher);
        description = findViewById(R.id.txt_description);
        version = findViewById(R.id.txt_ver);

        Bundle data = getIntent().getExtras();

        if (notifyId < 1 && notifyId != 0)
            notifyId = 0;

        assert data != null;
        if (data.isEmpty()) {
            super.onBackPressed();
        }

        if (apps.containsKey(data.getString("url"))) {
            app = apps.get(data.getString("url"));
        } else {
            app = new App(data.getString("url"), data.getParcelable("icon"), this);
            app.getData();
            apps.put(data.getString("url"), app);
        }

        Home.progressDialogApp.dismiss();

        packageManager = getPackageManager();
        setTitle(app.getTitle());
        packageName = app.getPackageName();

        if (appInstalled(packageName)) {
            open.setVisibility(View.INVISIBLE); //VISIBLE
            uninstall.setVisibility(View.INVISIBLE);
            open.setOnClickListener(v -> {
                Intent intent = packageManager.getLaunchIntentForPackage(packageName);
                startActivity(intent);
            });
            uninstall.setOnClickListener(v -> {
                Intent intent =
                        new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + packageName));
                startActivity(intent);
            });
        } else {
            open.setVisibility(View.INVISIBLE);
            uninstall.setVisibility(View.INVISIBLE);
        }

        try {
            int vibrantColor = app.getAppColor();
            collapsingToolbar.setContentScrimColor(vibrantColor);
            collapsingToolbar.setStatusBarScrimColor(vibrantColor);
            appBackground.setBackgroundColor(vibrantColor);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(vibrantColor);
            setTaskDescription(new ActivityManager.TaskDescription(
                    getString(R.string.app_name) + " - " + app.getTitle(), app.getAppIcon(),
                    vibrantColor));
            if (getSharedPreferences("settings", MODE_PRIVATE).getBoolean("colored_navbar", false))
                window.setNavigationBarColor(vibrantColor);
        } catch (Exception e) {
            Palette.from(app.getAppIcon()).generate(palette -> {
                int vibrantColor = palette.getDarkVibrantColor(R.color.colorPrimary);
                app.setAppColor(vibrantColor);
                collapsingToolbar.setContentScrimColor(vibrantColor);
                collapsingToolbar.setStatusBarScrimColor(vibrantColor);
                appBackground.setBackgroundColor(vibrantColor);
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(vibrantColor);
                setTaskDescription(new ActivityManager.TaskDescription(
                        getString(R.string.app_name) + " - " + app.getTitle(), app.getAppIcon(),
                        vibrantColor));
                if (getSharedPreferences("settings", MODE_PRIVATE)
                        .getBoolean("colored_navbar", false))
                    window.setNavigationBarColor(vibrantColor);
            });
        }

        if (! Utils.isColorDark(getWindow().getNavigationBarColor())
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        else
            getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);

        int color = getResources().getColor(android.R.color.background_dark);

        appIcon.setImageBitmap(app.getAppIcon());
        publisher.setText(app.getPublisher());
        version.setText(app.getVersion());
        Utils.setTextViewHTML(description, app.getDescription(), this);

        fab = findViewById(R.id.fab);

        appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {

            if (Math.abs(verticalOffset) > 200) {
                appBarExpanded = false;
                invalidateOptionsMenu();
            } else {
                appBarExpanded = true;
                invalidateOptionsMenu();
            }
        });

        recyclerView = findViewById(R.id.rv_versions);

        appVersionAdapter = new AppVersionAdapter(versions, this);
        RecyclerView.LayoutManager mLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(appVersionAdapter);
        recyclerView.setFocusable(false);

        app.getVersions(vers -> {
            versions.clear();
            versions.addAll(vers);
            appVersionAdapter.notifyDataSetChanged();
        });

        description.setOnClickListener(v -> {
            description_popup = new BottomPopup(app.getAppColor(), v, MainActivity.this,
                    getSharedPreferences("settings", MODE_PRIVATE)
                            .getBoolean("blur_dialog", false));
            description_popup.setText(app.getDescription());
            description_popup.setUp(findViewById(R.id.main_layout), R.layout.description_popup);
            description_popup.show();
        });

        setUp();
        setUpTheme();
    }

    private void setUpTheme() {
        final ThemeManager themeManager = ThemeManager.getInstance(this);
        themeManager.isDarkTheme();
        publisher.setTextColor(themeManager.getSubTitleTextColor());
        version.setTextColor(themeManager.getSubTitleTextColor());
        description.setTextColor(themeManager.getSubTitleTextColor());
        findViewById(R.id.include).setBackgroundColor(themeManager.getBackgroundColor());
        ((CardView) findViewById(R.id.cardDesc))
                .setCardBackgroundColor(themeManager.getElementColor());
        ((CardView) findViewById(R.id.cardVer))
                .setCardBackgroundColor(themeManager.getElementColor());
        ((TextView) findViewById(R.id.textView2)).setTextColor(themeManager.getSubTitleTextColor());
        ((TextView) findViewById(R.id.textView)).setTextColor(themeManager.getTitleTextColor());
    }

    private void setUp() {
        fab.setOnClickListener(view -> download(MainActivity.this, app));

        uninstall.setOnClickListener(v -> {
            try {
                uninstall();
            } catch (Exception e) {
                setUp();
            }
        });

        final Intent LaunchIntent =
                getPackageManager().getLaunchIntentForPackage(app.getPackageName());
        if (LaunchIntent != null)
            launchable = true;
        open.setOnClickListener(v -> {
            try {
                startActivity(LaunchIntent);
            } catch (Exception e) {
                setUp();
            }
        });

        if (appInstalled(app.getPackageName())) {
            if (! isNewerVersion(app))
                fab.setVisibility(View.INVISIBLE);
            if (launchable)
                open.setVisibility(View.VISIBLE);
            if (! isSystem(app.getPackageName()))
                uninstall.setVisibility(View.VISIBLE);
        }
    }

    private boolean isNewerVersion(App app) {
        try {
            return ! app.getVersion().split(" \\(")[0].equals(getPackageManager()
                    .getPackageInfo(app.getPackageName(), 0).versionName);
        } catch (Exception e) {
            return true;
        }
    }

    private void download(Activity context, App app) {
        Toast.makeText(context, getString(R.string.notification_downloading) + " " + app.getTitle(),
                Toast.LENGTH_LONG).show();
        notifyId++;
        Log.d("NOTIFYID", notifyId + "");
        final Notifications notifications = new Notifications(context, notifyId, app.getTitle(),
                getString(R.string.notification_download), "", app.getAppIcon(), true);
        notifications.addButton(R.drawable.ic_file_download_white_24dp,
                getString(R.string.notification_cancel), new Intent(context, Reciever.class)
                        .putExtra(Reciever.ACTION, Reciever.CANCEL_DOWNLOAD)
                        .putExtra(Reciever.ID, notifyId));
        notifications.showNotification();
        app.download(context, notifyId, new App.Listener() {
            @Override
            public void onUpdate(int progress) {
                notifications.setProgress(progress);
            }

            @Override
            public void onFinish() {
                notifications.setFinished();
                setUp();
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onCancel(String message) {
                notifications.setCanceled(message);
            }
        });
    }

    private void uninstall() {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + app.getPackageName()));
        startActivityForResult(intent, 1);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (collapsedMenu != null && (! appBarExpanded || collapsedMenu.size() != 1) && (
                ! appInstalled(app.getPackageName()) || isNewerVersion(app))) {
            collapsedMenu.add(getString(R.string.notification_download))
                    .setIcon(R.drawable.ic_file_download_white_24dp)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return super.onPrepareOptionsMenu(collapsedMenu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            setUp();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        collapsedMenu = menu;
        return true;
    }

    @Override
    public void onBackPressed() {
        if (description_popup != null) {
            if (! description_popup.isShowing())
                super.onBackPressed();
            else
                description_popup.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                fab.setVisibility(View.INVISIBLE);
                finishAfterTransition();
                return true;
        }

        if (item.getTitle() == getString(R.string.notification_download)) {
            download(this, app);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean appInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isSystem(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            return (pm.getApplicationInfo(packageName, 0).flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
