package cs.dal.minesweeper;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import java.util.Arrays;
import java.util.Random;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public GridView gv;
    public int itemscount=0;

    public String[][] items = new String[9][9];
    public static int[][] revealed = new int[9][9];
    private CustomGridAdapter gridAdapter;
    public int startcount = 0;
    public boolean started;
    public int minecount;
    public int counter = 0 ;
    public int flagcount = 0;
    public int mines;
    public int tempmines;
    public int clocker = 0;
    int gam_row = 9;
    int gam_col = 9;
    public boolean winwithflag;
    public boolean winwithoutflag;
    public boolean gameover;
    public boolean lvl_not_selctd = false;
    CountDownTimer minetimer;
    Button restart;
    TextView timer;
    TextView tot_mines;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int i = 0;i<gam_row;i++)
        {
            for(int j = 0;j<gam_col;j++)
            {
                items[i][j] = "";
                revealed[i][j] = 0;
            }
        }
        gv = (GridView) this.findViewById(R.id.mygrid);
        final TextView tv1 = (TextView)findViewById(R.id.tot_mines);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        timer = (TextView)findViewById(R.id.Timer);
        tot_mines = (TextView)findViewById(R.id.tot_mines);
        restart = (Button) findViewById(R.id.restart);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.level, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(this);

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                final TextView timer = view.findViewById(R.id.Timer);
                int row = i/gam_row;
                int column = i%gam_col;

                // Initial loading

                if(lvl_not_selctd)
                {
                    Toast.makeText(MainActivity.this,"Please select the level of complexity", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!started)
                {
                    start(row,column,view);
                    if ("0".equalsIgnoreCase(items[row][column])){
                        revealed[row][column] = 0;
                        openNearby(row,column);
                    }

                }
                else if(gameover)    // when the game is over no item click should be functional
                {
                    return;
                }
                else   //explore each cell on click
                {
                    if(revealed[row][column] == 2)   //if the cell clicked is already flagged,no action
                        return;

                    if(revealed[row][column] == 1) //if the cell clicked is already explored
                        return;

                    //when the cell clicked is neither bomb nor empty, reveal the cell

                   if( !"0".equalsIgnoreCase(items[row][column]) &&  !"*".equalsIgnoreCase(items[row][column]) )
                   {
                        revealed[row][column] = 1;
                    }

                    //when the cell clicked is a mine, open all the other mines and tell the user that the game is over

                    else if ("*".equalsIgnoreCase(items[row][column]))
                    {
                        for(int x =0; x<gam_row;x++) {
                            for (int y = 0; y < gam_col; y++) {
                                if(items[x][y] == "*")
                                    revealed[x][y] = -2;
                            }
                       }

                        revealed[row][column] = -3;
                        gameover = true;
                        Toast.makeText(MainActivity.this,"Game Over", Toast.LENGTH_LONG).show();
                        restart.setBackgroundResource(R.drawable.sadsmiley1);
                        minetimer.cancel();

                    }

                    // when the cell clicked is empty, explore the nearby cells not the mines

                    else
                    {
                        openNearby(row,column);
                    }

                }
                haswon(); //check whether the player has won
                if (winwithoutflag) {
                    Toast.makeText(MainActivity.this, "Excellent! You made it", Toast.LENGTH_LONG).show();
                    minetimer.cancel();
                }
                gridAdapter.notifyDataSetChanged();
                }

        });

        gv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                // when the cell clicked is already revealed or the game is over, no action to be performed

                int row = i/gam_row;
                int col = i%gam_col;

                if((revealed[row][col] == 1) || gameover )
                    return true;

                if(revealed[row][col] == 0) //when the cell is not revealed, reveal
                {
                    revealed[row][col] = 2;
                    flagcount ++;
                }
                else // when the cell selected is already been flagged, unflag it
                {
                    revealed[row][col] = 0;
                    flagcount--;

                }
                
                gridAdapter.notifyDataSetChanged();

                //***flag based descion logic***

                if (flagcount == tempmines)
                {
                    //Toast.makeText(MainActivity.this,"Control inside", Toast.LENGTH_LONG).show();
                    for(int x =0; x<gam_row;x++) {
                        for (int y = 0; y < gam_col; y++) {
                            if (revealed[x][y] == 1 || revealed[x][y] == 2)
                                winwithflag = true;
                            else
                            {
                                winwithflag = false;
                                break;
                            }

                        }
                        if (!winwithflag)
                            break;
                    }

                }
                if (winwithflag)
                    Toast.makeText(MainActivity.this,"Excellent! You made it", Toast.LENGTH_LONG).show();

                return true;
            }
        });

        gridAdapter = new CustomGridAdapter(MainActivity.this, items);
        gv.setAdapter(gridAdapter);

    }

    //Initial load, where the mines and neighbours based on the mine are set

    public void start(int x1, int y1,View view)
    {
        started  = true;
        Random r = new Random();

        //Randomly generate location to plant the mines

        while( mines > 0 ){
            int x = r.nextInt(gam_row);
            int y = r.nextInt(gam_col);
            if( items[x][y] != "*" && (x != x1 && y != y1)){
                items[x][y] = "*";
                mines--;
            }
        }
        mines = tempmines; // this is used in calculating the win, based on flag logic
        revealed[x1][y1] = 1;
        mineNeigbours(gam_row,gam_col);                //explore neighbours
        gridAdapter.notifyDataSetChanged();
        clocker = 0;
        //timer
        minetimer =  new CountDownTimer(20000000, 1000)
        {
            @Override
            public void onTick(long l) {
                clocker++;
                timer.setText(String.format("%03d",clocker));
            }

            @Override
            public void onFinish() {
                clocker = 000;
                timer.setText(Integer.toString(clocker));

            }
        };
        minetimer.start();

    }

    // restarting the game

    public void startGame(View view)
    {
        restart.setBackgroundResource(R.drawable.happysmiley);
        gameover =false;
        if(minetimer!=null){
            minetimer.cancel();
            minetimer =null;
        }

        // to display the  timer and flag count in a format

        timer.setText(String.format("%03d",0));
        tot_mines.setText(String.format("%02d",mines));
        flagcount = 0;
        started = false;
        for (int i = 0;i<gam_row;i++)
        {
            for(int j = 0;j<gam_col;j++)
            {
                items[i][j] = "";
                revealed[i][j] = 0;
            }
        }
        gridAdapter.notifyDataSetChanged();

    }

    public void mineNeigbours( int row , int column){
        for( int x = 0 ; x < row ; x++){
            for( int y = 0 ; y < column ; y++){
                if(  !"*".equalsIgnoreCase(items[x][y]) ){

                    int no_of_mines = 0;

                    // top-left
                    if( (x - 1)  >= 0 && (y -1) >= 0 && (x - 1 ) < row && (y - 1) < column )
                    {
                        if ("*".equalsIgnoreCase(items[x - 1][y - 1]))
                        {
                            no_of_mines++;
                        }
                    }

                    // top
                    if( (x - 1)  >= 0 && y >= 0 && (x - 1) < row && y < column )
                    {
                        if ("*".equalsIgnoreCase(items[x - 1][y]))
                        {
                            no_of_mines++;
                        }
                    }

                    // top-right
                    if( (x - 1)  >= 0 && (y + 1) >= 0 && (x - 1) < row && (y + 1) < column )
                    {
                        if ("*".equalsIgnoreCase(items[x - 1][y + 1]))
                        {
                            no_of_mines++;
                        }
                    }

                    // left
                    if( x  >= 0 && (y -1) >= 0 && x < row && (y - 1) < column )
                    {
                        if ("*".equalsIgnoreCase(items[x][y - 1]))
                        {
                            no_of_mines++;
                        }
                    }


                    // right
                    if( x >= 0 && (y + 1) >= 0 && x < row && (y + 1) < column )
                    {
                        if ("*".equalsIgnoreCase(items[x][y + 1]))
                        {
                            no_of_mines++;
                        }
                    }

                    // bottom-left
                    if( (x + 1) >= 0 && (y - 1) >= 0 && (x + 1) < row && (y - 1) < column )
                    {
                        if ("*".equalsIgnoreCase(items[x + 1][y - 1]))
                        {
                            no_of_mines++;
                        }
                    }

                    // bottom
                    if( (x + 1)  >= 0 && y >= 0 && (x + 1) < row && y < column )
                    {
                        if ("*".equalsIgnoreCase(items[x + 1][y]))
                        {
                            no_of_mines++;
                        }
                    }

                    // bottom-right
                    if( (x + 1)  >= 0 && (y + 1) >= 0 && (x + 1) < row && (y + 1) < column )
                    {
                        if ("*".equalsIgnoreCase(items[x + 1][y + 1]))
                        {
                            no_of_mines++;
                        }
                    }
                    items[x][y] = String.valueOf(no_of_mines);

                }


            }
        }

    }

    public void openNearby( int row , int column) {

        if( row >= 0 && column >= 0 && row < gam_row && column < gam_col ) {

            if("*".equalsIgnoreCase(items[row][column])|| revealed[row][column] == 2 || revealed[row][column] == 1)
            {
                return;
            }

            if("0".equalsIgnoreCase(items[row][column])) {
                revealed[row][column] = 1;


                openNearby(row + 1, column); //bottom
                openNearby(row - 1, column); //top
                openNearby(row, column - 1); //left
                openNearby(row, column + 1); //right
                openNearby(row - 1, column - 1);//top-left
                openNearby(row - 1, column + 1);// top-right
                openNearby(row + 1, column - 1);//bottom-left
                openNearby(row + 1, column + 1); //bottom-right
            }
            else
            {
                revealed[row][column] = 1;
            }

            }

        }

        // Based on the difficulty level the mine count is set

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String level = (String) adapterView.getItemAtPosition(i);

        if (i == 0)
        {
            lvl_not_selctd = true;
            return;
        }

         else if(i == 1)
            mines = 10;
        else if(i == 2)
            mines = 24;
        else
            mines = 40;

        tempmines = mines;
        lvl_not_selctd = false;
        tot_mines.setText(String.format("%02d",mines));

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        {
            Toast.makeText(MainActivity.this,"Please select the level", Toast.LENGTH_SHORT).show();
        }

    }
    public void haswon() //check whether won
    {
        for(int x =0; x<gam_row;x++) {
            for (int y = 0; y < gam_col; y++) {
                if (items[x][y] != "*" && revealed[x][y] == 1)
                    winwithoutflag = true;
                else if (items[x][y] != "*" && (revealed[x][y] == 0 || revealed[x][y] == 2))
                {
                    winwithoutflag = false;
                    break;
                }
                else
                    winwithoutflag = true;
            }
            if(!winwithoutflag)
                break;
        }

    }
}
