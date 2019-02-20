package karrel.kr.co.draganddropsample;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by 이주영 on 2017-01-17.
 */

public class MyApplication extends Application {


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
