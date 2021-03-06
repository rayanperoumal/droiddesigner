package io.github.rayanperoumal.droiddesigner;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;

import io.github.rayanperoumal.droiddesigner.file.FileRecyclerView;
import io.github.rayanperoumal.droiddesigner.file.FileSelection;

public class ProjectActivity extends AppCompatActivity {
    public static File parent;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_files:
                return true;
            case R.id.navigation_code:
                return true;
            case R.id.navigation_graphics:
                return true;
            case R.id.navigation_manage:
                return true;
        }
        return false;
    };

    public static void setFragment(FragmentManager fm, Fragment fragment) {
        fm.beginTransaction()
                .replace(R.id.content, fragment).commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        if (getIntent().hasExtra("path")) {
            String path = getIntent().getStringExtra("path");
            Log.i("fuckit","path:"+path);
            parent = new File(path);
            setFragment(getSupportFragmentManager(), new ResourceListFragment());
        }

    }

    public static class FileListFragment extends Fragment {
        FileRecyclerView view;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            FileSelection selection = new FileSelection(parent.getName(),new String[]{parent.getAbsolutePath()});
            for (File n:parent.listFiles()){
                selection.addSelection(new FileSelection(n.getName(),new String[]{n.getAbsolutePath()}));
            }
            view = new FileRecyclerView(getActivity(), selection);
            return view;
        }

        @Override
        public void onResume() {
            super.onResume();
            view.setOnFileSelected(selection -> {
                File f = new File(selection.getPaths()[0]);

                if (f.isDirectory())
                    view.sync(new FileSelection());
                else Log.i("File:selected", "No action for this file");
            });

        }

        public File[] catchFiles(File file) {
            if (file.exists())
                return file.listFiles();
            return new File[]{file};
        }
    }


    public static class ResourceListFragment extends Fragment {
            FileRecyclerView view;

            @Nullable
            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

                FileSelection selections = new FileSelection();
                selections.addSelection(getColorSelection());
                selections.addSelection(getStringSelection());
                view = new FileRecyclerView(getActivity(), selections);
                return view;
            }

            public FileSelection getColorSelection() {
                File f = getSubFile("app/src/res/values/colors.xml");
                FileSelection fs = new FileSelection("Couleurs",new String[]{f.getAbsolutePath()});
                fs.setOpener(getFragmentManager().beginTransaction()
                        .replace(R.id.content, new ColorOpener()));
                return fs;
            }

            public FileSelection getStringSelection() {
                File f = getSubFile("app/src/res/values/strings.xml");
                FileSelection fs = new FileSelection("Textes",new String[]{f.getAbsolutePath()});
                return fs;
            }

            public File getSubFile(String path) {
                File file = new File(parent, path);
                if (!file.exists()) {
                    try {
                        boolean b = file.createNewFile();
                        if (!b) return null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return file;
            }

            @Override
            public void onResume() {
                super.onResume();
                view.setOnFileSelected(file -> {
                    Log.i("File:selected", "Need to open file in editor");
                    file.open();
                });

            }
        }
}
