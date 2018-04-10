/*
 * Copyright (c) 2017 ThanksMister LLC
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

package com.thanksmister.bitcoin.localtrader.ui.fragments;

import android.content.Context;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.thanksmister.bitcoin.localtrader.R;
import com.thanksmister.bitcoin.localtrader.network.api.model.Advertisement;
import com.thanksmister.bitcoin.localtrader.network.api.model.ExchangeCurrency;
import com.thanksmister.bitcoin.localtrader.network.api.model.TradeType;
import com.thanksmister.bitcoin.localtrader.data.database.MethodItem;
import com.thanksmister.bitcoin.localtrader.utils.TradeUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

public class EditOnlineFragment extends BaseEditFragment {
    
    @BindView(R.id.editPaymentDetails)
    EditText editPaymentDetails;
    
    @BindView(R.id.paymentDetailsLayout)
    View paymentDetailsLayout;

    @BindView(R.id.detailsPhoneNumberLayout)
    View detailsPhoneNumberLayout;

    @BindView(R.id.detailsPhoneNumber)
    EditText detailsPhoneNumber;
    
    @BindView(R.id.minimumVolumeText)
    EditText minimumVolumeText;
    
    @BindView(R.id.minimumFeedbackText)
    EditText minimumFeedbackText;
    
    @BindView(R.id.newBuyerLimitText)
    EditText newBuyerLimitText;
    
    public EditOnlineFragment() {
        // Required empty public constructor
    }

    public static EditOnlineFragment newInstance() {
        EditOnlineFragment fragment = new EditOnlineFragment();
        return fragment;
    }
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCurrencies();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ad_online_options, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View fragmentView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragmentView, savedInstanceState);
        editAdvertisement = getEditAdvertisement();
        setAdvertisementOnView(editAdvertisement);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(isAdded() &&  actionBar != null) {
            actionBar.setTitle(R.string.text_title_online_options);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean validateChangesAndSave() {
        
        TradeType tradeType = editAdvertisement.trade_type;
        String phoneNumber = detailsPhoneNumber.getText().toString();
        String accountInfo = editPaymentDetails.getText().toString();
       
        if (tradeType == TradeType.ONLINE_SELL) {
            switch (editAdvertisement.online_provider) {
                case TradeUtils.QIWI:
                case TradeUtils.SWISH:
                case TradeUtils.MOBILEPAY_DANSKE_BANK:
                case TradeUtils.MOBILEPAY_DANSKE_BANK_DK:
                case TradeUtils.MOBILEPAY_DANSKE_BANK_NO:
                    if (TextUtils.isEmpty(phoneNumber)) {
                        toast(getString(R.string.toast_missing_phone_number));
                        return false;
                    }
                    editAdvertisement.phone_number = phoneNumber;
                    break;
            }
        }

        if (TextUtils.isEmpty(accountInfo) && TradeType.ONLINE_SELL.equals(editAdvertisement.trade_type)) {
            toast(getString(R.string.toast_provide_payment_details));
            return false;
        } 

        if (!TextUtils.isEmpty(phoneNumber)) {
            editAdvertisement.phone_number = detailsPhoneNumber.getText().toString();
        }

        if (!TextUtils.isEmpty(minimumFeedbackText.getText().toString())) {
            editAdvertisement.require_feedback_score = minimumFeedbackText.getText().toString();
        }

        if (!TextUtils.isEmpty(minimumVolumeText.getText().toString())) {
            editAdvertisement.require_trade_volume = minimumVolumeText.getText().toString();
        }

        if (!TextUtils.isEmpty(newBuyerLimitText.getText().toString())) {
            editAdvertisement.first_time_limit_btc = newBuyerLimitText.getText().toString();
        }

        if (!TextUtils.isEmpty(editPaymentDetails.getText().toString())) {
            editAdvertisement.account_info = editPaymentDetails.getText().toString();
        }

        setEditAdvertisement(editAdvertisement);
        
        return true;
    }
    
    @Override
    protected void setAdvertisementOnView(@NonNull Advertisement editAdvertisement) {
        
        detailsPhoneNumberLayout.setVisibility(GONE);
        paymentDetailsLayout.setVisibility(View.VISIBLE);
        
        TradeType tradeType = editAdvertisement.trade_type;
        if (tradeType == TradeType.ONLINE_SELL) {
            switch (editAdvertisement.online_provider) {
                case TradeUtils.QIWI:
                case TradeUtils.MOBILEPAY_DANSKE_BANK_NO:
                case TradeUtils.MOBILEPAY_DANSKE_BANK:
                case TradeUtils.MOBILEPAY_DANSKE_BANK_DK:
                case TradeUtils.PAYTM:
                case TradeUtils.SWISH:
                    detailsPhoneNumberLayout.setVisibility(View.VISIBLE);
                    paymentDetailsLayout.setVisibility(GONE);
                    break;
                default:
                    detailsPhoneNumberLayout.setVisibility(GONE);
                    paymentDetailsLayout.setVisibility(View.VISIBLE);
                    break;
            }
        }

        if (!TextUtils.isEmpty(editAdvertisement.phone_number)) {
            detailsPhoneNumber.setText(editAdvertisement.phone_number);
        }

        if (!TextUtils.isEmpty(editAdvertisement.require_feedback_score)) {
            minimumFeedbackText.setText(editAdvertisement.require_feedback_score);
        }

        if (!TextUtils.isEmpty(editAdvertisement.require_trade_volume)) {
            minimumVolumeText.setText(editAdvertisement.require_trade_volume);
        }

        if (!TextUtils.isEmpty(editAdvertisement.first_time_limit_btc)) {
            newBuyerLimitText.setText(editAdvertisement.first_time_limit_btc);
        }

        if (!TextUtils.isEmpty(editAdvertisement.account_info)) {
            editPaymentDetails.setText(editAdvertisement.account_info);
        }
    }

    // not implemented for this fragment
    @Override
    protected void setMethods(List<MethodItem> methods) {}
    @Override
    protected  void showAddressError() {}
    @Override
    protected  void displayAddress(Address address) {}
    @Override
    protected void onAddresses(List<Address> addresses) {}
    @Override
    protected void onCurrencies(List<ExchangeCurrency> currencies) {}
}