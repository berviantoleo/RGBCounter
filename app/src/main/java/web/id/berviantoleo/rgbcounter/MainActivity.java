/**
 * Copyright 2018 Bervianto Leo Pratama
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package web.id.berviantoleo.rgbcounter;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;
import com.facebook.drawee.view.SimpleDraweeView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.image_holder)
    protected SimpleDraweeView imageHolder;

    @BindView(R.id.red_chart)
    protected LineChart redChart;

    @BindView(R.id.green_chart)
    protected LineChart greenChart;

    @BindView(R.id.blue_chart)
    protected LineChart blueChart;

    private LineData lineDataRed;
    private LineData lineDataGreen;
    private LineData lineDataBlue;
    private AlertDialog dialog;
    private long start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        dialog = new SpotsDialog.Builder().setContext(this).build();
        lineDataRed = new LineData();
        lineDataGreen = new LineData();
        lineDataBlue = new LineData();
    }

    @OnClick(R.id.image_holder)
    protected void clickImage() {
        ImagePicker.create(this)
                .returnMode(ReturnMode.ALL)
                .folderMode(true) // folder mode (false by default)
                .toolbarFolderTitle("Select Folder") // folder selection title
                .toolbarImageTitle("Select Image") // image selection title
                .single() // single mode
                .showCamera(true) // show camera or not (true by default)
                .imageDirectory("RGB_Counter") // directory name for captured image  ("Camera" folder by default)
                .enableLog(false) // disabling log
                .start(); // start image picker activity with request code
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Image image = ImagePicker.getFirstImageOrNull(data);
            if (image != null) {
                String path = image.getPath();
                Uri uri = Uri.parse("file://" + path);
                imageHolder.setImageURI(uri);
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                new CountColour().execute(bitmap);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class CountColour extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lineDataBlue.clearValues();
            lineDataBlue.notifyDataChanged();
            lineDataGreen.clearValues();
            lineDataGreen.clearValues();
            lineDataRed.clearValues();
            lineDataRed.notifyDataChanged();
            dialog.show();
            start = System.nanoTime();
        }

        @Override
        protected Void doInBackground(Bitmap... params) {
            Bitmap bitmap = params[0];
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] red = new int[256];
            int[] green = new int[256];
            int[] blue = new int[256];
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int color = bitmap.getPixel(i, j);
                    int redColor = (color >> 16) & 0xff;
                    int greenColor = (color >> 8) & 0xff;
                    int blueColor = color & 0xff;
                    red[redColor] += 1;
                    green[greenColor] += 1;
                    blue[blueColor] += 1;
                }
            }
            List<Entry> entriesRed = new ArrayList<>();
            List<Entry> entriesGreen = new ArrayList<>();
            List<Entry> entriesBlue = new ArrayList<>();
            for (int i = 0; i < 256; i++) {
                entriesRed.add(new Entry(i, red[i]));
                entriesGreen.add(new Entry(i, green[i]));
                entriesBlue.add(new Entry(i, blue[i]));
            }
            LineDataSet dataSetRed = new LineDataSet(entriesRed, "red");
            LineDataSet dataSetGreen = new LineDataSet(entriesGreen, "green");
            LineDataSet dataSetBlue = new LineDataSet(entriesBlue, "blue");
            dataSetRed.setColor(Color.RED);
            dataSetRed.setDrawCircles(false);
            dataSetGreen.setColor(Color.GREEN);
            dataSetGreen.setDrawCircles(false);
            dataSetBlue.setColor(Color.BLUE);
            dataSetBlue.setDrawCircles(false);
            lineDataRed.addDataSet(dataSetRed);
            lineDataGreen.addDataSet(dataSetGreen);
            lineDataBlue.addDataSet(dataSetBlue);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            redChart.setData(lineDataRed);
            greenChart.setData(lineDataGreen);
            blueChart.setData(lineDataBlue);
            redChart.invalidate();
            greenChart.invalidate();
            blueChart.invalidate();
            dialog.dismiss();
            long end = System.nanoTime();
            long duration = end - start;
            Log.i("Process Photo", String.format("Waktu dibutuhkan : %d", duration));
        }
    }
}