package de.stephanlindauer.criticalmass_berlin.sections;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import de.stephanlindauer.criticalmass_berlin.R;

public class CollectionDemoActivity extends FragmentActivity {

    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_demo);

        mDemoCollectionPagerAdapter =       new DemoCollectionPagerAdapter( getSupportFragmentManager() );
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
    }

}