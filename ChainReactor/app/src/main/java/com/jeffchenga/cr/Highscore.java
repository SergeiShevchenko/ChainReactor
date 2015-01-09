package com.jeffchenga.cr;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * Created by ACER on 01.04.14.
 */
public class Highscore
{

    public class Score
    {
        public int score;
        public int level;
        public String name;

        public Score(int s, int l, String n)
        {
            score = s;
            level = l;
            name = n;
        }

        public Score(String p)
        {
            String[] parsed = p.split(" ");
            score = Integer.parseInt(parsed[0]);
            //level = Integer.parseInt(parsed[1]);
            name = parsed[1];
        }

        public int compareTo(Score o)
        {
            return(score - o.score);
        }

        public String outScore()
        {
            String s = "";
            s = s+String.valueOf(score)+" "+name;
            //s = s+String.valueOf(score)+" "+String.valueOf(level)+" "+name;
            return s;
        }

        public String out()
        {
            String s = "";
            s = s+String.valueOf(score)+" "+String.valueOf(level)+" "+name;
            return s;
        }
    }

    List<Score> scores = new ArrayList<Score>();

    String _url = "http://jeffchenga.ucoz.ru/mobile/colors.txt";

    public Highscore(final Context context, int score, int level, String name)
    {
        String mes = "";
        final String filename = "scoreboard.txt";

        /*
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);


        try
        {
            int count;
            try {
                URL url = new URL(_url);
                URLConnection conection = url.openConnection();
                conection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream

                File file = new File(context.getFilesDir(), filename);

                OutputStream output = new FileOutputStream(file);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

        }
        catch (Exception e) {
            mes = "";
        }
*/
        scores.add(new Score(score, level, name));


        FileInputStream fis;

        final StringBuffer storedString = new StringBuffer();

        try {
            fis = context.openFileInput(filename);
            DataInputStream dataIO = new DataInputStream(fis);
            String strLine = null;

            while ((strLine = dataIO.readLine()) != null) {
                scores.add(new Score(strLine));
            }
            dataIO.close();
            fis.close();
        }
        catch  (Exception e) {
            mes = "";
        }

            Collections.sort(scores, new Comparator<Score>(){
                public int compare(Score o1, Score o2){
                    if(o1.score == o2.score)
                        return 0;
                    return o1.score > o2.score ? -1 : 1;
                }
            });

            if (scores.size()>10)
                scores.remove(10);

            for (int i=0; i<scores.size(); i++)
            {
                mes+=scores.get(i).outScore()+"\n";
            }



            FileOutputStream outputStream;
            File file = new File(context.getFilesDir(), filename);
            try {
                outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(mes.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }




        AlertDialog.Builder builder = new AlertDialog.Builder(
                context);
        builder.setCancelable(true);
        builder.setMessage(mes);
        builder.setTitle("High-scores");
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton("Clear",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        FileOutputStream outputStream;
                        File file = new File(context.getFilesDir(), filename);
                        try {
                            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                            outputStream.write("".getBytes());
                            outputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}


