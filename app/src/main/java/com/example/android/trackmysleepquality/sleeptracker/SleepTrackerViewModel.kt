/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.launch

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        /**   The ViewModel needs
        access to the data in the database, so pass in an instance of the SleepDatabaseDao. */
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

        //variable to hold the current night
                private var tonight = MutableLiveData<SleepNight?>()
        //get all nights in db
       val allNights = database.getAllNights()



        val nightString =Transformations.map(allNights){allNights->
                formatNights(allNights,application.resources)

        }




//tonight is null at the beginning

        /**
         * If tonight has not been set, then the START button should be visible.
         */
        val startButtonVisible = Transformations.map(tonight) {
                null == it
        }

        //if tonight has a value- stop should be visible
        /**
         * If tonight has been set, then the STOP button should be visible.
         */

        val stopButtonVisible = Transformations.map(tonight) {
                null != it
        }

        /**
         * If there are any nights in the database, show the CLEAR button.
         */
        val clearButtonVisible = Transformations.map(allNights) {
                it?.isNotEmpty()
        }

        /**
         * Request a toast by setting this value to true.
         *
         * This is private because we don't want to expose setting this value to the Fragment.
         */
        private var _showSnackbarEvent =MutableLiveData<Boolean>()

        /**
         * If this is true, immediately `show()` a toast and call `doneShowingSnackbar()`.
         */
        val showSnackbarEvent:LiveData<Boolean>
        get() = _showSnackbarEvent




        private var _navigateToSleepQuality =MutableLiveData<SleepNight>()

        /**
         * Call this immediately after calling `show()` on a toast.
         *
         * It will clear the toast request, so if the user rotates their phone it won't show a duplicate
         * toast.
         */

        fun doneShowingSnackbar() {
                _showSnackbarEvent.value = false
        }


        val navigateToSleepQuality :LiveData<SleepNight>
                get() = _navigateToSleepQuality



        fun doneNavigating(){
                _navigateToSleepQuality.value = null
        }

        init {
            initializeToNight()
        }

        private fun initializeToNight() {
                viewModelScope.launch {
                        tonight.value = getToNightFromDatabase()
                }
        }

        private suspend fun getToNightFromDatabase(): SleepNight? {
                var night = database.getTonight()

                if (night?.endTimeMili != night?.startTimeMili)
                {
                        night= null
                }


                return night
        }

        //the click handler for the Start button
        fun onStartTracking(){
                viewModelScope.launch {
                        val newNight =SleepNight()

                        insert(newNight)
                        tonight.value=getToNightFromDatabase()
                }

        }

        private suspend fun insert(night: SleepNight) {
                database.insert(night)
        }

        //adding click handlers for the buttons

        fun onStopTracking(){
                viewModelScope.launch {
                        val oldNight = tonight.value ?: return@launch
                        oldNight.endTimeMili = System.currentTimeMillis()

                        update(oldNight)
                        _navigateToSleepQuality.value =oldNight
                }

        }

        private  suspend fun update(night: SleepNight) {
                database.update(night)
        }


        fun onClear(){
                viewModelScope.launch {
                        clear()
                        tonight.value =null
                        _showSnackbarEvent.value =true
                }
        }

        private suspend fun clear(){
                database.clear()
        }

    //add a handler for the click event.
    //You also need to add a MutableLiveData object to control the navigation.
    private val _navigateToSleepDataQuality = MutableLiveData<Long>()
    val navigateToSleepDataQuality
        get() = _navigateToSleepDataQuality

    //Define method to initiate and complete navigation.
    //Initiate navigation by setting _navigateToSleepDataQuality.value to id:
    fun onSleepNightClicked(id: Long){
        _navigateToSleepDataQuality.value = id
    }
   // and then set it to null once navigation is completed:

    fun onSleepDataQualityNavigated() {
        _navigateToSleepDataQuality.value = null
    }


}

