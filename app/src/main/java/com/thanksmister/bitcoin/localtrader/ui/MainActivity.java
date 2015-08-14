/*
 * Copyright (c) 2015 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.thanksmister.bitcoin.localtrader.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.zxing.android.IntentIntegrator;
import com.google.zxing.android.IntentResult;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.thanksmister.bitcoin.localtrader.BaseActivity;
import com.thanksmister.bitcoin.localtrader.R;
import com.thanksmister.bitcoin.localtrader.data.api.model.DashboardType;
import com.thanksmister.bitcoin.localtrader.data.database.DbManager;
import com.thanksmister.bitcoin.localtrader.data.services.DataService;
import com.thanksmister.bitcoin.localtrader.data.services.SyncUtils;
import com.thanksmister.bitcoin.localtrader.events.NavigateEvent;
import com.thanksmister.bitcoin.localtrader.events.NetworkEvent;
import com.thanksmister.bitcoin.localtrader.events.RefreshEvent;
import com.thanksmister.bitcoin.localtrader.ui.contacts.ContactActivity;
import com.thanksmister.bitcoin.localtrader.ui.search.SearchFragment;
import com.thanksmister.bitcoin.localtrader.utils.NotificationUtils;
import com.thanksmister.bitcoin.localtrader.utils.WalletUtils;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static rx.android.app.AppObservable.bindActivity;

@BaseActivity.RequiresAuthentication
public class MainActivity extends BaseActivity implements View.OnClickListener
{
    private static final String BITCOIN_URI = "com.thanksmister.extra.BITCOIN_URI";
    private static final String DASHBOARD_FRAGMENT = "com.thanksmister.fragment.DASHBOARD_FRAGMENT";
    private static final String ABOUT_FRAGMENT = "com.thanksmister.fragment.ACCOUNT_FRAGMENT";
    private static final String RECEIVE_FRAGMENT = "com.thanksmister.fragment.RECEIVE_FRAGMENT";
    private static final String SEND_FRAGMENT = "com.thanksmister.fragment.SEND_FRAGMENT";
    private static final String SEARCH_FRAGMENT = "com.thanksmister.fragment.SEARCH_FRAGMENT";
    private static final String WALLET_FRAGMENT = "com.thanksmister.fragment.WALLET_FRAGMENT";
    
    public static String EXTRA_CONTACT = "extra_contact";
    public static String EXTRA_TYPE = "extra_type";
    public static String EXTRA_FRAGMENT = "extra_fragment";
    
    private static final int DRAWER_DASHBOARD = 0;
    private static final int DRAWER_SEARCH = 1;
    private static final int DRAWER_SEND = 2;
    private static final int DRAWER_RECEIVE= 3;
    private static final int DRAWER_WALLET = 4;
    private static final int DRAWER_ABOUT = 5;
    
    private static final int REQUEST_SCAN = 49374;

    @Inject
    DataService dataService;
    
    @Inject
    DbManager dbManager;

    @Inject
    Bus bus;

    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @InjectView(R.id.navigation)
    NavigationView navigationView;
    
    Fragment fragment;
    int position = DRAWER_DASHBOARD;
    Observable<Boolean> loginObserver;
    Subscription loginSubscription = Subscriptions.unsubscribed();
    boolean activityCreated;
    
    public static Intent createStartIntent(Context context, String bitcoinUri)
    {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(BITCOIN_URI, bitcoinUri);
        return intent;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        if(savedInstanceState != null) {
            position = savedInstanceState.getInt(EXTRA_FRAGMENT);
        }
        
        final String bitcoinUri = getIntent().getStringExtra(BITCOIN_URI);

        setupNavigationView();
        
        loginObserver = bindActivity(this, dbManager.isLoggedIn());

        loginSubscription = loginObserver
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>()
                {
                    @Override
                    public void call(Boolean isLoggedIn)
                    {
                        if (isLoggedIn) {
                            if (bitcoinUri != null && validAddressOrAmount(bitcoinUri)) { // we have a uri request so override setting content
                                handleBitcoinUri(bitcoinUri);
                            } else {
                                setContentFragment(position);
                            }

                            SyncUtils.TriggerRefresh(getApplicationContext());
                            SyncUtils.CreateSyncAccount(getApplicationContext());
                        } else {
                            launchPromoScreen();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable)
                    {
                        Timber.e("Error thrown during login verification");
                    }
                });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        //No call for super(). Bug on API Level > 11.
        outState.putInt(EXTRA_FRAGMENT, position);
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        if (((Object) this).getClass().isAnnotationPresent(RequiresAuthentication.class)) {

            if(loginSubscription.isUnsubscribed()) {
                
                loginSubscription = dbManager.isLoggedIn()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Boolean>()
                        {
                            @Override
                            public void call(Boolean isLoggedIn)
                            {
                                if (!isLoggedIn) {
                                    launchPromoScreen();
                                }
                            }
                        });
            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        loginSubscription.unsubscribe();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(drawerLayout != null)
                    drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void launchPromoScreen()
    {
        Intent intent = new Intent(MainActivity.this, PromoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
  
    private void setupNavigationView()
    {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem)
            {
                selectDrawerItem(menuItem);
                return true;
            }
        });
    }

    public void selectDrawerItem(MenuItem menuItem) 
    {
        switch(menuItem.getItemId()) {
            case R.id.navigationItemDashboard:
                setContentFragment(DRAWER_DASHBOARD);
                break;
            case R.id.navigationItemSearch:
                setContentFragment(DRAWER_SEARCH);
                break;
            case R.id.navigationItemSend:
                setContentFragment(DRAWER_SEND);
                break;
            case R.id.navigationItemReceive:
                setContentFragment(DRAWER_RECEIVE);
                break;
            case R.id.navigationItemWallet:
                setContentFragment(DRAWER_WALLET);
                break;
            case R.id.navigationItemAbout:
                setContentFragment(DRAWER_ABOUT);
                break;
            default:
                setContentFragment(DRAWER_DASHBOARD);
        }

        // Highlight the selected item, update the title, and close the drawer
        menuItem.setChecked(true);
        drawerLayout.closeDrawers();
    }
    
    /*private void setInitialFragment(int position)
    {
        FragmentManager fragmentManager = getFragmentManager();
        if(fragmentManager.findFragmentByTag(DASHBOARD_FRAGMENT) == null) {
            setContentFragment(DRAWER_DASHBOARD);
        } else {
            setContentFragment(position);
        }
    }*/
    
    private void setContentFragment(int position)
    {
        this.position = position;
        
        if (position == DRAWER_WALLET) {
            fragment = WalletFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
            .replace(R.id.content_frame, fragment, WALLET_FRAGMENT)
                    .commitAllowingStateLoss();
        } else if (position == DRAWER_SEARCH) {
            fragment = SearchFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment, SEARCH_FRAGMENT)
                    .commitAllowingStateLoss();
        } else if (position == DRAWER_SEND) {
            fragment = SendFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment, SEND_FRAGMENT)
                    .commitAllowingStateLoss();
        } else if (position == DRAWER_RECEIVE) {
            fragment = RequestFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment, RECEIVE_FRAGMENT)
                    .commitAllowingStateLoss();
        } else if (position == DRAWER_DASHBOARD) {
            fragment = DashboardFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment, DASHBOARD_FRAGMENT)
                    .commitAllowingStateLoss();
        } else if (position == DRAWER_ABOUT) {
            fragment = AboutFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment, ABOUT_FRAGMENT)
                    .commitAllowingStateLoss();
        }
    }
    
    private void startSendFragment(String bitcoinAddress, String bitcoinAmount)
    {
        fragment = SendFragment.newInstance(bitcoinAddress, bitcoinAmount);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment, SEARCH_FRAGMENT)
                .commit();
        
        navigationView.getMenu().findItem(R.id.navigationItemSend).setChecked(true);
        position = DRAWER_SEND;
    }

    @Subscribe
    public void onRefreshEvent(RefreshEvent event)
    {
        if (fragment.getTag().equals(DASHBOARD_FRAGMENT)) {
            ((DashboardFragment) fragment).onRefresh();
        } else if (fragment.getTag().equals(WALLET_FRAGMENT)){
            ((WalletFragment) fragment).onRefresh();
        } else if (fragment.getTag().equals(SEARCH_FRAGMENT)){
            ((SearchFragment) fragment).onRefresh();
        } else if (fragment.getTag().equals(SEND_FRAGMENT)){
            ((RequestFragment) fragment).onRefresh();
        }
    }
    
    @Subscribe
    public void onNetworkEvent(NetworkEvent event)
    {
        if (event == NetworkEvent.DISCONNECTED) {
            boolean retry = (position == DRAWER_DASHBOARD || position == DRAWER_WALLET);
            snack(getString(R.string.error_no_internet), retry);
        }
    }

    @Subscribe
    public void onNavigateEvent (NavigateEvent event)
    {
        if(event == NavigateEvent.SEND) {
            setContentFragment(DRAWER_SEND);
            navigationView.getMenu().findItem(R.id.navigationItemSend).setChecked(true);
        } else if (event == NavigateEvent.SEARCH) {
            setContentFragment(DRAWER_SEARCH);
            navigationView.getMenu().findItem(R.id.navigationItemSearch).setChecked(true);
        } else if (event == NavigateEvent.WALLET) {
            setContentFragment(DRAWER_WALLET);
            navigationView.getMenu().findItem(R.id.navigationItemWallet).setChecked(true);
        } else if (event == NavigateEvent.LOGOUT_CONFIRM) {
            logOutConfirmation();
        } else if (event == NavigateEvent.LOGOUT) {
            logOut();
        } else if (event == NavigateEvent.QRCODE) {
            launchScanner();
        }
    }
    
    public void restoreActionBar()
    {
        /*ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setTitle(mTitle);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if (drawerLayout != null && !drawerLayout.isDrawerOpen(GravityCompat.START)) {
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        
        if(requestCode == REQUEST_SCAN) {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanningResult != null) {
                handleBitcoinUri(scanningResult.getContents());
            } else {
                toast(getString(R.string.toast_scan_canceled));
            }
        }
    }
    
    protected boolean validAddressOrAmount(String bitcoinUri)
    {
        String bitcoinAddress = WalletUtils.parseBitcoinAddress(bitcoinUri);
        String bitcoinAmount = WalletUtils.parseBitcoinAmount(bitcoinUri);
    
        Timber.d("Bitcoin Address: " + bitcoinAddress);
        Timber.d("Bitcoin Amount: " + bitcoinAmount);

        if(bitcoinAddress == null) {
            return false;
        } else if(!WalletUtils.validBitcoinAddress(bitcoinAddress)) {
            toast(getString(R.string.toast_invalid_address));
            return false;
        }

        if(bitcoinAmount != null && !WalletUtils.validAmount(bitcoinAmount)) {
            toast(getString(R.string.toast_invalid_btc_amount));
            return false;
        }
        
        return true;
    }

    protected void handleBitcoinUri(String bitcoinUri)
    {
        String bitcoinAddress = WalletUtils.parseBitcoinAddress(bitcoinUri);
        String bitcoinAmount = WalletUtils.parseBitcoinAmount(bitcoinUri);
        startSendFragment(bitcoinAddress, bitcoinAmount);
    }
    
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        Bundle extras = intent.getExtras();

        int type = extras.getInt(EXTRA_TYPE, 0);
        
        if(type == NotificationUtils.NOTIFICATION_TYPE_CONTACT || type == NotificationUtils.NOTIFICATION_TYPE_MESSAGE ) {
            
           String contactId = extras.getString(EXTRA_CONTACT);
            
            if(contactId != null) {
                Intent contactIntent = ContactActivity.createStartIntent(this, contactId, DashboardType.ACTIVE);
                startActivity(contactIntent);
            }
            
        } else if (type == NotificationUtils.NOTIFICATION_TYPE_BALANCE) {
            bus.post(NavigateEvent.WALLET);
        }
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.fab) {
            launchScanner();
        }
    }
}
