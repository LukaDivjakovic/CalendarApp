package com.example.rmaapp.widget

import android.content.Intent
import android.widget.RemoteViewsService

class EventWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return EventRemoteViewsFactory(this.applicationContext)
    }
}
