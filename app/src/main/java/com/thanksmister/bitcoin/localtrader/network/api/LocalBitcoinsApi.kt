/*
 * Copyright (c) 2018 ThanksMister LLC
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

package com.thanksmister.bitcoin.localtrader.network.api

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.thanksmister.bitcoin.localtrader.network.api.adapters.DataTypeAdapterFactory
import com.thanksmister.bitcoin.localtrader.network.api.model.*
import com.thanksmister.bitcoin.localtrader.network.services.LocalBitcoinsService
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.*

import java.util.concurrent.TimeUnit

class LocalBitcoinsApi (private  val context: Context, private var  baseUrl: String) {

    private val service: LocalBitcoinsService

    init {

        Timber.d("baseUrl $baseUrl")

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .addNetworkInterceptor(StethoInterceptor())
                .build()

        val gson = GsonBuilder()
                .registerTypeAdapterFactory(DataTypeAdapterFactory())
                .create()

        val retrofit = Retrofit.Builder()
                .client(httpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(baseUrl)
                .build()

        service = retrofit.create(LocalBitcoinsService::class.java)
    }

    fun getAuthorization(grantType: String, code: String, clientId: String, clientSecret:String): Observable<Authorization> {
        return service.getAuthorization(grantType, code, clientId, clientSecret)
    }

    fun refreshToken(grantType: String, refreshToken: String, clientId: String, clientSecret:String): Observable<Authorization> {
        return service.refreshToken(grantType, refreshToken, clientId, clientSecret)
    }

    fun getAdvertisements(token: String): Observable<Advertisements> {
        return service.getAdvertisements(token)
    }

    fun getCurrencies(): Observable<TreeMap<String?, Any>> {
        return service.currencies
    }

    fun checkPinCode(accessToken: String, pinCode: String): Observable<JsonElement> {
        return service.checkPinCode(accessToken, pinCode)
    }

    fun releaseContactPinCode(accessToken: String, contactId: String, pinCode: String): Observable<JsonElement> {
        return service.releaseContactPinCode(accessToken, contactId, pinCode)
    }

    fun contactCancel(accessToken: String, contactId: String): Observable<JsonElement> {
        return service.contactCancel(accessToken, contactId)
    }

    fun contactDispute(accessToken: String, contactId: String): Observable<JsonElement> {
        return service.contactDispute(accessToken, contactId)
    }

    fun markAsPaid(accessToken: String, contactId: String): Observable<JsonElement> {
        return service.markAsPaid(accessToken, contactId)
    }

    fun contactFund(accessToken: String, contactId: String): Observable<JsonElement> {
        return service.contactFund(accessToken, contactId)
    }

    fun updateAdvertisement(accessToken: String, s: String, account_info: String, bank_name: String, city: String, country_code: String,
                            currency: String, lat: String, location: String, lon: String, max_amount: String, min_amount: String,
                            message: String, price_equation: String, trust : String, sms: String, trackMax: String, visible:
                            String, identification: String, require_feedback_score: String, require_trade_volume:
                            String, first_time_limit_btc: String, phone_number: String, opening_hours: String): Observable<JsonElement> {
        return service.updateAdvertisement(
                accessToken, s, account_info, bank_name, city, country_code, currency,
                lat, location, lon, max_amount, min_amount,
                message, price_equation, trust, sms,
                trackMax, visible, identification,
                require_feedback_score, require_trade_volume, first_time_limit_btc,
                phone_number, opening_hours);
    }

    fun contactMessagePost(accessToken: String, contact_id: String, message: String): Observable<JsonElement> {
        return service.contactMessagePost(accessToken, contact_id, message)
    }

  /*  fun contactMessagePostWithAttachment(accessToken: String, contact_id: String, params: LinkedHashMap<String, String>, typedFile: TypedFile): Observable<Response> {
        return service.contactMessagePostWithAttachment(accessToken, contact_id, params, typedFile)
    }
*/
    fun getMyself(accessToken: String): Observable<User> {
        return service.getMyself(accessToken)
    }

    fun getContactInfo(accessToken: String, contactId: Int): Observable<Contact> {
        return service.getContactInfo(accessToken, contactId)
    }

    fun contactMessages(accessToken: String, contact_id: String): Observable<Messages> {
        return service.contactMessages(accessToken, contact_id)
    }

    fun getNotifications(accessToken: String): Observable<Notifications> {
        return service.getNotifications(accessToken)
    }

    fun markNotificationRead(accessToken: String, notificationId: String): Observable<JsonElement> {
        return service.markNotificationRead(accessToken, notificationId)
    }

    fun getDashboard(accessToken: String): Observable<Dashboard> {
        return service.getDashboard(accessToken)
    }

    fun getDashboard(accessToken: String, type: String): Observable<Dashboard> {
        return service.getDashboard(accessToken, type)
    }

    fun getAdvertisement(accessToken: String, adId: Int): Observable<Advertisement> {
        return service.getAdvertisement(accessToken, adId)
    }

    fun deleteAdvertisement(accessToken: String, adId: String): Observable<JsonElement> {
        return service.deleteAdvertisement(accessToken, adId)
    }

    fun createAdvertisement(accessToken: String, min_amount: String, max_amount: String, price_equation: String, name: String,
                            online_provider: String, s: String, s1: String, city: String, location: String, country_code: String,
                            account_info: String, bank_name: String, s2: String, s3: String, s4: String, s5: String,
                            require_feedback_score: String, require_trade_volume: String, first_time_limit_btc: String,
                            message: String, currency: String, phone_number: String, opening_hours: String): Observable<JsonElement> {

        return service.createAdvertisement(accessToken, min_amount, max_amount, price_equation, name, online_provider, s, s1, city, location,
                country_code, account_info, bank_name, s2, s3, s4, s5, require_feedback_score, require_trade_volume, first_time_limit_btc,
                message, currency, phone_number, opening_hours)
    }

    fun getWallet(accessToken: String): Observable<Wallet> {
        return service.getWallet(accessToken)
    }

    fun getOnlineProviders(): Observable<TreeMap<String?, Any>> {
        return service.onlineProviders
    }

    fun createContactEmail(accessToken: String, adId: String, amount: String, email: String, message: String): Observable<JsonElement> {
        return service.createContactEmail(accessToken, adId, amount, email, message)
    }

    fun createContact(accessToken: String, adId: String, amount: String, message: String): Observable<JsonElement> {
        return service.createContact(accessToken, adId, amount, message)
    }

    fun createContactPhone(accessToken: String, adId: String, amount: String, phone: String, message: String): Observable<JsonElement> {
        return service.createContactPhone(accessToken, adId, amount, phone, message)
    }

    fun createContactBPay(accessToken: String, adId: String, amount: String, billerCode: String, reference: String, message: String): Observable<JsonElement> {
        return service.createContactBPay(accessToken, adId, amount, billerCode, reference, message)
    }

    fun createContactSepa(accessToken: String, adId: String, amount: String, name: String, iban: String, bic: String, reference: String, message: String): Observable<JsonElement> {
        return service.createContactSepa(accessToken, adId, amount, name, iban, bic, reference, message)
    }

    fun createContactEthereumAddress(accessToken: String, adId: String, amount: String, ethereumAddress: String, message: String): Observable<JsonElement> {
        return service.createContactEthereumAddress(accessToken, adId, amount, ethereumAddress, message)
    }

    fun createContactNational(accessToken: String, adId: String, amount: String, message: String): Observable<JsonElement> {
        return service.createContactNational(accessToken, adId, amount, message)
    }

    fun createContactNationalFI(accessToken: String, adId: String, amount: String, name: String, iban: String, bic: String, reference: String, message: String): Observable<JsonElement> {
        return service.createContactNational_FI(accessToken, adId, amount, name, iban, bic, reference, message)
    }

    fun createContactNationalAU(accessToken: String, adId: String, amount: String, name: String, bsb: String, reference: String, accountNumber: String, message: String): Observable<JsonElement> {
        return service.createContactNational_AU(accessToken, adId, amount, name, bsb, reference, accountNumber, message)
    }

    fun createContactNationalUK(accessToken: String, adId: String, amount: String, name: String, sortCode: String, reference: String, accountNumber: String, message: String): Observable<JsonElement> {
        return service.createContactNational_UK(accessToken, adId, amount, name, sortCode, reference, accountNumber, message)
    }

    fun walletSendPin(accessToken: String, pinCode: String, address: String, amount: String): Observable<JsonElement> {
        return service.walletSendPin(accessToken, pinCode, address, amount)
    }

    fun getWalletBalance(accessToken: String): Observable<Wallet> {
        return service.getWalletBalance(accessToken);
    }


}