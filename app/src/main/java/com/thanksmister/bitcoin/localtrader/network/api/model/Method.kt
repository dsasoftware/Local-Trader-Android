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
 *
 */

package com.thanksmister.bitcoin.localtrader.network.api.model

import android.arch.persistence.room.*

@Entity(tableName = "Methods", indices = [(Index(value = arrayOf("key"), unique = true))])
class Method {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "key")
    var key: String? = null

    @ColumnInfo(name = "code")
    var code: String? = null

    @ColumnInfo(name = "name")
    var name: String? = null

    @ColumnInfo(name = "currencies")
    @TypeConverters(StringConverter::class)
    var currencies: ArrayList<String> = ArrayList<String>()
}



