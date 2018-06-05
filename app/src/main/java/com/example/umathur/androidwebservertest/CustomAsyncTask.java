package com.example.umathur.androidwebservertest;

import android.os.AsyncTask;

public class CustomAsyncTask extends AsyncTask<Void, Void, String> {

    private AsyncHandler asyncHandler;

    public CustomAsyncTask(AsyncHandler asyncHandler) {
        this.asyncHandler = asyncHandler;
    }

    @Override
    protected String doInBackground(Void... voids) {
        return asyncHandler.doInBackgroundHandler();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        asyncHandler.onPostExecHandler();

    }

    interface AsyncHandler {
        String doInBackgroundHandler();
        void onPostExecHandler();
    }

}