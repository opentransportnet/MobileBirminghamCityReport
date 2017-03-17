package eu.opentransportnet.cityreport.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import eu.opentransportnet.cityreport.R;
import eu.opentransportnet.cityreport.models.BaseActivity;
import eu.opentransportnet.cityreport.utils.Utils;

import java.util.Locale;

/**
 * @author Kristaps Krumins
 */
public class ChangeLangActivity extends BaseActivity {
    private ListView mLanguageList;
    private String[] mLangCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Initialization */
        setContentView(R.layout.activity_change_language);
        setToolbarTitle(R.string.title_activity_language);
        initToolbarBackBtn();

        TextView language = (TextView) findViewById(R.id.current_language);
        language.setText(Locale.getDefault().getDisplayLanguage());

        mLanguageList = (ListView) findViewById(R.id.language_list);

        Resources res = getResources();
        String[] languages = res.getStringArray(R.array.languages);
        mLangCodes = res.getStringArray(R.array.language_codes);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.black_list_item, languages);
        mLanguageList.setAdapter(adapter);
        mLanguageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Utils.changeLanguage(getBaseContext(), mLangCodes[position]);
                finish();
            }

        });
    }
}
