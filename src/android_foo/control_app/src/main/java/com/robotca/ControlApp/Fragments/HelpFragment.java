package com.robotca.ControlApp.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.robotca.ControlApp.R;

import java.util.ArrayList;

/**
 * Fragment for showing help information.
 */
public class HelpFragment extends Fragment
{
    // View made static to fix a bug, but there might be a better way
    private static View view;

    /**
     * Default Constructor.
     */
    public HelpFragment() {}

    @Override
    public void onCreate(Bundle instance)
    {
        super.onCreate(instance);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_help, container, false);

            TabHost mTabHost = (TabHost) view.findViewById(android.R.id.tabhost);
            mTabHost.setup();

            ViewPager mViewPager = (ViewPager) view.findViewById(R.id.pager);
            TabsAdapter mTabsAdapter = new TabsAdapter(getActivity(), mTabHost, mViewPager);

            // Here we load the content for each tab.
            mTabsAdapter.addTab(mTabHost.newTabSpec("one").setIndicator("Setup"), PageOneFragment.class, null);
            mTabsAdapter.addTab(mTabHost.newTabSpec("two").setIndicator("Using"), PageTwoFragment.class, null);
            mTabsAdapter.addTab(mTabHost.newTabSpec("three").setIndicator("FAQ"), PageThreeFragment.class, null);
        }


        return view;
    }

    /**
     * Custom adapter for the three help tabs.
     */
    public static class TabsAdapter extends FragmentPagerAdapter
            implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener
    {
        private final Context mContext;
        private final TabHost mTabHost;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<>();

        /**
         * Container for some information about one tab.
         */
        static final class TabInfo
        {
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(Class<?> _class, Bundle _args)
            {
                clss = _class;
                args = _args;
            }
        }

        /**
         * Factory class for creating a content view for a tab.
         */
        static class DummyTabFactory implements TabHost.TabContentFactory
        {
            private final Context mContext;

            public DummyTabFactory(Context context)
            {
                mContext = context;
            }

            public View createTabContent(String tag)
            {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        /**
         * Creates a TabsAdapter.
         * @param activity The parent Activity
         * @param tabHost The TabHost
         * @param pager The ViewPager
         */
        public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager)
        {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mTabHost = tabHost;
            mViewPager = pager;
            mTabHost.setOnTabChangedListener(this);
            mViewPager.setAdapter(this);
            //noinspection deprecation // Needed for min API level
            mViewPager.setOnPageChangeListener(this);
        }

        /**
         * Adds a tab to this TabsAdapter.
         * @param tabSpec The TabSpec for the tab
         * @param clss The class of the Fragment to be contained in the tab
         * @param args Arguments for the tab
         */
        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args)
        {
            tabSpec.setContent(new DummyTabFactory(mContext));
            tabSpec.getTag();

            TabInfo info = new TabInfo(clss, args);
            mTabs.add(info);
            mTabHost.addTab(tabSpec);
            notifyDataSetChanged();
        }

        /**
         * @return The number of tabs in this TabsAdapter
         */
        @Override
        public int getCount()
        {
            return mTabs.size();
        }

        /**
         * Returns the Fragment for the tab at the specified position.
         * @param position The position in this TabsAdapter
         * @return The newly created Fragment for the tab at the position
         */
        @Override
        public Fragment getItem(int position)
        {
            TabInfo info = mTabs.get(position);

            Log.d("HelpFragment", "Creating Fragment " + position);

            return Fragment.instantiate(mContext, info.clss.getName(), info.args);

        }

        /**
         * Callback for when a tab is opened.
         * @param tabId The id of the tab
         */
        @Override
        public void onTabChanged(String tabId)
        {
            int position = mTabHost.getCurrentTab();
            mViewPager.setCurrentItem(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {
        }

        /**
         * Callback for when a tab is selected.
         * @param position The position of the selected tab
         */
        @Override
        public void onPageSelected(int position)
        {
            // Unfortunately when TabHost changes the current tab, it kindly
            // also takes care of putting focus on it when not in touch mode.
            // The jerk.
            // This hack tries to prevent this from pulling focus out of our
            // ViewPager.
            TabWidget widget = mTabHost.getTabWidget();
            int oldFocusability = widget.getDescendantFocusability();
            widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            mTabHost.setCurrentTab(position);
            widget.setDescendantFocusability(oldFocusability);
        }

        public void onPageScrollStateChanged(int state)
        {}
    }
}