package iss.workshop.memorygame;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> rlGameActivity;
    private GridAdapter ga;

    private String Url;
    private Thread downloadThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rlGameActivity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result ->{
            if (result.getResultCode() == AppCompatActivity.RESULT_OK){
                Intent data = result.getData();
                Long timefinished = data.getLongExtra("timer",0);
                Toast toast = Toast.makeText(getBaseContext(),"You finish the game in "+timefinished+" seconds",Toast.LENGTH_LONG);
                toast.show();
            }

        });

        EditText inputUrl = findViewById(R.id.InputUrl);
        Button btnStartGame = findViewById(R.id.btnStartGame);
        List<String> paths = Arrays.asList("ph", "ph", "ph", "ph", "ph", "ph", "ph", "ph", "ph", "ph", "ph", "ph", "ph", "ph", "ph", "ph", "ph", "ph", "ph", "ph");
        Button btnfetch = findViewById(R.id.btnfetch);
        ga = new GridAdapter(this,paths);
        GridView gv = findViewById(R.id.gridviewA);
        ConstraintLayout animated = findViewById(R.id.mainbg);
        ProgressBar p = findViewById(R.id.prgdownload);
        if (gv != null){
            gv.setAdapter(ga);
            gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ga.updateSelectedPosition(position);
                }
            });
        }
        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> selectedimgs = (ArrayList<String>) ga.getselecedAbsolutepath();
                if (ga.getselecedAbsolutepath().size() == 6){
                    Intent intent = new Intent(getBaseContext(),GameActivity.class);
                    intent.putStringArrayListExtra("selectedimgs",selectedimgs);
                    rlGameActivity.launch(intent);
                    //Toast toast = Toast.makeText(getBaseContext(),"you have chosen 6 images",Toast.LENGTH_LONG);
                    //toast.show();s
                }else {
                    Toast toast = Toast.makeText(getBaseContext(),"You need to choose 6 images",Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
        btnfetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downloadThread != null){
                    downloadThread.interrupt();
                }
                try {
                    if (downloadThread != null) {
                        downloadThread.join();
                    }
                } catch (InterruptedException e) {
                    // Handle InterruptedException if needed
                    e.printStackTrace();
                }
                ga.setPaths(paths);
                ga.notifyDataSetChanged();
                Url = inputUrl.getText().toString();
                File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                downloadThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Document document = Jsoup.connect(Url).get();
                            int i = 0;
                            for (Element element: document.select("img")){
                                if (downloadThread.isInterrupted()){
                                    return;
                                }
                                //i++;
                                String imageUrl = element.attr("data-src");
                                if (i < 20){
                                    File destfile = new File(dir,i + ".jpg");
                                    if(downloadimage(imageUrl, destfile)){
                                        i++;
                                        int finalI = i;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ProgressBar pgb = findViewById(R.id.prgdownload);
                                                TextView txtprogress = findViewById(R.id.txtprogress);
                                                ga.updatepath(finalI-1,destfile.getAbsolutePath());
                                                ga.notifyDataSetChanged();
                                                pgb.setProgress(finalI);
                                                txtprogress.setText(finalI + " image of 20 images downloaded");
                                                if (finalI == 20){
                                                    btnStartGame.setVisibility(View.VISIBLE);
                                                    pgb.setVisibility(View.INVISIBLE);
                                                    txtprogress.setVisibility(View.INVISIBLE);
                                                }
                                            }
                                        });
                                    }
                                }else{
                                    downloadThread.interrupt();
                                }
                            }
                        } catch (Exception e) {
                            inputUrl.setText("invalid link to download");
                        }

                    }
                });
                downloadThread.start();
                //gonna create thread
                    //use the url to get all img src
                        //for each src we will downloadimage until 20 images and update the grid at the same time
                        //also show the progress bar
            }
        });
    }

    protected boolean downloadimage(String imgUrl, File destFile){
        try {
            URL url = new URL(imgUrl);
            URLConnection conn = url.openConnection();
            InputStream in = conn.getInputStream();
            FileOutputStream out =new FileOutputStream(destFile);

            byte[] buf = new byte[4096];
            int byteread = -1;
            while((byteread = in.read(buf)) != -1){
                out.write(buf,0,byteread);
            }
            out.close();
            in.close();
            return true;
        }catch (Exception e){
            return false;
        }
    }
}