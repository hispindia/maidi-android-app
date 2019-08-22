package org.hisp.dhis.client.sdk.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    private OnBackPressedCallback onBackPressedCallback;

    @Override
    public void onBackPressed() {
        if (onBackPressedCallback != null) {
            if (onBackPressedCallback.onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    public void setOnBackPressedCallback(OnBackPressedCallback onBackPressedCallback) {
        this.onBackPressedCallback = onBackPressedCallback;
    }
}
