package iss.workshop.memorygame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GridAdapter extends ArrayAdapter<Object> {
    private final Context context;
    private List<String> paths;
    private List<Boolean> gameconditions = Arrays.asList(false,false,false,false,false,false,false,false,false,false,false,false);
    private List<Integer> positions = new ArrayList<>();
    public GridAdapter(Context context, List<String> paths) {
        super(context, R.layout.item);
        this.context = context;
        this.setPaths(paths);
        addAll(new Object[paths.size()]);
    }

    public void updatepath(int pos, String path){
        paths.set(pos,path);
    }

    @NonNull
    public View getView(int pos, View view, @NonNull ViewGroup Parent){
        if (view == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.item,Parent,false);
        }
        ImageView imageView = view.findViewById(R.id.imageitem);
        //int id = 0;
        if (paths.get(pos).equals("ph")){ //placeholder image when there is no download image yet
            int id = context.getResources().getIdentifier(paths.get(pos),"drawable", context.getPackageName());
            imageView.setImageResource(id);
        }else {// actual image after download
            if (context instanceof GameActivity){
                if (!gameconditions.get(pos)){
                    imageView.setImageResource(R.drawable.ph);
                }
                else {
                    Bitmap bitmap = BitmapFactory.decodeFile(paths.get(pos));
                    imageView.setImageBitmap(bitmap);
                }
            }else
            {
                Bitmap bitmap = BitmapFactory.decodeFile(paths.get(pos));
                imageView.setImageBitmap(bitmap);
            }
        }
        if (context instanceof MainActivity){
            if (positions.contains(pos)){
                view.setBackgroundColor(ContextCompat.getColor(context,R.color.green));//changing the UI of the selected positions
            }
            else {
                view.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        return view;
    }

    public List<String> getselecedAbsolutepath(){
        List<String> selectedpaths = new ArrayList<>();
        int i = 0;
        for (String path: paths){
            if (positions.contains(i)){
                selectedpaths.add(path);
            }
            i++;
        }
        return selectedpaths;
    }

    public void updateSelectedPosition(int pos){ // recording selected position
        if(positions.contains(pos)){
            positions.remove((Object)pos);
        }else {
            positions.add(pos);
        }
        notifyDataSetChanged();
    }

    public void updategamecon(int pos, boolean con){
        gameconditions.set(pos,con);
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }
}
