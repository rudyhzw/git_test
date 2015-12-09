package com.example.rudytest;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import java.util.List;

public class ViewPagerAdapter extends PagerAdapter{
    private List<View> views;
    private Context context;
    
    public ViewPagerAdapter(Context context, List<View> views){
        this.views = views;
        this.context = context;
    }

    
    public void destroyItem(View container, int position, Object object) {
        ((android.support.v4.view.ViewPager) container).removeView(views.get(position));
    };
    
    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public Object instantiateItem(View container, int position) {
        
        ((android.support.v4.view.ViewPager) container).addView(views.get(position));
        return views.get(position);
    }
    
    
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        // TODO Auto-generated method stub
        return (arg0 == arg1);
    }

}
