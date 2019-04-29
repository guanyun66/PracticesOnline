package net.lzzy.practicesonline.activities.activities;

import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.fragments.SplashFragment;
import net.lzzy.practicesonline.activities.utlis.AppUtils;

/**
 * Created by lzzy_gxy on 2019/4/11.
 * Description:
 */
public abstract class BaseActivity extends AppCompatActivity {
    private Fragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getLayout());
        AppUtils.addActivity(this);
        FragmentManager manager = getSupportFragmentManager();
        fragment = manager.findFragmentById(getContainerId());
        if (fragment == null){
            fragment = createFragment();
            manager.beginTransaction().add(getContainerId(),fragment).commit();
        }

    }
    protected Fragment getFragment(){
        return fragment;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppUtils.removeActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.setRunning(getLocalClassName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppUtils.setStopped(getLocalClassName());
    }
    protected abstract int getLayout();

    protected abstract int getContainerId();

    protected abstract Fragment createFragment();


}
