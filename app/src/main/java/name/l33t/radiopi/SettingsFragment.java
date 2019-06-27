package name.l33t.radiopi;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

public final class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey);
    }
}