
package com.example.rudytest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class Guide extends Activity implements OnPageChangeListener {

    private ViewPager vp;
    private ViewPagerAdapter vpAdapter;
    private List<View> views;
    private ImageView[] dots;
    private int[] ids = {
            R.id.iv1, R.id.iv2, R.id.iv3
    };
    private Button bt_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide);
        initViews();
        initDots();
    }

    private void initViews() {
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.one, null));
        views.add(inflater.inflate(R.layout.two, null));
        views.add(inflater.inflate(R.layout.three, null));
        bt_start = (Button) views.get(2).findViewById(R.id.bt_start);
        bt_start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                startActivity(new Intent(Guide.this, MainActivity.class));
                finish();
            }
        });
        vpAdapter = new ViewPagerAdapter(this, views);
        vp = (ViewPager) findViewById(R.id.viewpager);
        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(this);
    }

    private void initDots() {
        dots = new ImageView[views.size()];
        for (int i = 0; i < dots.length; i++) {
            dots[i] = (ImageView) findViewById(ids[i]);
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < dots.length; i++) {
            if (position == i) {
                dots[i].setImageResource(R.drawable.dark);
            } else {
                dots[i].setImageResource(R.drawable.light);
            }
        }

    }

}
