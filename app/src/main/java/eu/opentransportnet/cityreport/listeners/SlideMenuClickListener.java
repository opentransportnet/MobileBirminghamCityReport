package eu.opentransportnet.cityreport.listeners;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import eu.opentransportnet.cityreport.activities.DisclaimerActivity;
import eu.opentransportnet.cityreport.activities.MainActivity;
import eu.opentransportnet.cityreport.utils.SessionManager;

/**
 * @author Ilmārs Svilsts
 */
public class SlideMenuClickListener implements ListView.OnItemClickListener {
    private DrawerLayout drawer;
    MainActivity activity;

    public SlideMenuClickListener(DrawerLayout drawer, MainActivity activity) {
        this.drawer = drawer;
        this.activity = activity;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // display view for selected nav drawer item
        switch (position) {
            case 0:
                drawer.closeDrawers();
                Intent disActivity = new Intent(activity, DisclaimerActivity.class);
                activity.startActivity(disActivity);
                break;
            case 1:
                drawer.closeDrawers();
                activity.deleteUser();
                break;
            case 2:
                drawer.closeDrawers();
                new SessionManager(activity).logoutUser();
                break;
        }
    }
}