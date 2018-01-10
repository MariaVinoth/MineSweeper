package cs.dal.minesweeper;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by apple on 07/10/17.
 */

public class CustomGridAdapter extends BaseAdapter {
    private Context contxt;
    private String[][] items;
    LayoutInflater inflater;
    public CustomGridAdapter(Context contxt, String[][] items){
        this.contxt = contxt;
        this.items = items;
        inflater = (LayoutInflater)this.contxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {

        return 81;
    }

    @Override
    public Object getItem(int i) {

        return null;
    }

    @Override
    public long getItemId(int i) {

        return 0;
    }

    @SuppressWarnings("ResourceAsColor")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null){
            view = inflater.inflate(R.layout.cell,null);
        }
        if(MainActivity.revealed[i/9][i%9]==1 ) {


            TextView tv = view.findViewById(R.id.textview);
            tv.setText(items[i / 9][i % 9]);

        }else if(MainActivity.revealed[i/9][i%9] == 2)
        {

            TextView tv = view.findViewById(R.id.textview);
            tv.setBackgroundResource(R.drawable.flag);
        }else if(MainActivity.revealed[i/9][i%9]==0)
        {
            TextView tv = view.findViewById(R.id.textview);
            tv.setText("");
            tv.setBackgroundResource(0);
        }
        else if(MainActivity.revealed[i/9][i%9] == -2)
        {
            TextView tv = view.findViewById(R.id.textview);
            tv.setText("");
            tv.setBackgroundResource(R.drawable.bomb2);
        }
        else if(MainActivity.revealed[i/9][i%9] == -3)
        {
            TextView tv = view.findViewById(R.id.textview);
            tv.setText("");
            tv.setBackgroundResource(R.drawable.bomb1);
       }

        return view;
    }
}
