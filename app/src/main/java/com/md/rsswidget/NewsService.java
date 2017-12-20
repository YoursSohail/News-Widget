package com.md.rsswidget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class NewsService extends Service {
    ArrayList<String> descriptions;
    ArrayList<String> links;
    ArrayList<String> titles;
    Intent intent;
    public NewsService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        descriptions = new ArrayList<>();
        links = new ArrayList<>();
        titles = new ArrayList<>();

        this.intent = intent;

        if(ApplicationClass.connectionAvailable(getApplicationContext()))
        {
            RemoteViews views = new RemoteViews("com.md.rsswidget",R.layout.new_app_widget);
            views.setTextViewText(R.id.tvTitle,"Busy retrieving data...");
            views.setTextViewText(R.id.tvDescription,"Busy retrieving data...");

            AppWidgetManager.getInstance(getApplicationContext()).updateAppWidget(intent.getIntExtra("appWidgetId",0),views);

            new GetStoriesInBackground().execute();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Please make sure your phone has an active internet connection",
                    Toast.LENGTH_SHORT).show();
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            return null;
        }
    }



    public class GetStoriesInBackground extends AsyncTask<Integer, Integer, String>
    {

        @Override
        protected String doInBackground(Integer... params) {

            try {

                URL url = new URL("http://timesofindia.indiatimes.com/rssfeedstopstories.cms");

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();


                xpp.setInput(getInputStream(url), "UTF_8");


                boolean insideItem = false;

                // Returns the type of current event: START_TAG, END_TAG, etc..
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {

                        if (xpp.getName().equalsIgnoreCase("item")) {
                            insideItem = true;
                        } else if (xpp.getName().equalsIgnoreCase("title")) {
                            if (insideItem)
                                titles.add(xpp.nextText()); //extract the headline
                        } else if (xpp.getName().equalsIgnoreCase("link")) {
                            if (insideItem)
                                links.add(xpp.nextText()); //extract the link of article
                        }
                        else if (xpp.getName().equalsIgnoreCase("description")) {
                            if (insideItem)
                                descriptions.add(xpp.nextText()); //extract the link of article
                        }
                    }else if(eventType==XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")){
                        insideItem=false;
                    }

                    eventType = xpp.next();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        }


        @Override
        protected void onPostExecute(String result) {

            Random random = new Random();
            int randomValue = random.nextInt(titles.size());

            RemoteViews views = new RemoteViews("com.md.rsswidget",R.layout.new_app_widget);
            views.setTextViewText(R.id.tvTitle,titles.get(randomValue));
            views.setTextViewText(R.id.tvDescription,descriptions.get(randomValue));

            Uri uri = Uri.parse(links.get(randomValue));
            Intent linkIntent = new Intent(Intent.ACTION_VIEW,uri);

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,linkIntent,PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.tvDescription,pendingIntent);

            PendingIntent pendingIntent1Sync = PendingIntent.getService(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.ivSync,pendingIntent1Sync);

            AppWidgetManager.getInstance(getApplicationContext()).updateAppWidget(intent.getIntExtra("appWidgetId",0),views);

        }

    }
}
