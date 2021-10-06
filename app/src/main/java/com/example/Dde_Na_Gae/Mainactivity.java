package com.example.Dde_Na_Gae;

import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.Dde_Na_Gae.model.UserModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Mainactivity extends AppCompatActivity {

    TextView category1;
    TextView category2;
    TextView category3;
    TextView category4;
    TextView category5;
    TextView category6;

    DatabaseReference mDatabase;


    ImageView today_place1;
    ImageView today_place2;

    ImageView best_tour1;
    ImageView best_tour2;

    ImageView best_walk1;
    ImageView best_walk2;

    ImageView add_menu1;
    ImageView add_menu2;

    //네비게이션바
    DrawerLayout drawerLayout;
    View drawerView;
    ListView listview = null;
    LinearLayout my_page;
    //네비게이션바

    BottomNavigationView bottomNavigationView;

    ImageView profile_photo;
    TextView my_nickname;
    TextView unlogin;
    LinearLayout layout_account;

    ArrayList<String> Main_urls = new ArrayList<>();
    ArrayList<String> Main_titles = new ArrayList<>();
    ArrayList<String> Main_addrs = new ArrayList<>();
    ArrayList<String> Main_conIds = new ArrayList<>();

    int region_code=1;
    private RecyclerView main_recyclerview;
    private ArrayList<MainRecyclerViewItem> mainlist;
    private MainRecyclerViewAdapter main_recyclerviewadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profile_photo = (ImageView) findViewById(R.id.profile_photo);
        listview = findViewById(R.id.navi_list);
        layout_account = (LinearLayout) findViewById(R.id.my_account);
        unlogin = (TextView) findViewById(R.id.my_page_unlogin);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // 로그인한 유저의 정보 가져오기

        Spinner region_spinner = findViewById(R.id.region_spinner);
        ArrayAdapter region_adapter = ArrayAdapter.createFromResource(this, R.array.region, android.R.layout.simple_spinner_dropdown_item);
        region_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        region_spinner.setAdapter(region_adapter);

        region_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                TextView region_travel = (TextView)findViewById(R.id.region_travel);
                region_travel.setText(region_spinner.getSelectedItem() + " 관광지");
                if (position < 8){
                    region_code = position + 1;
                }
                else{
                    region_code = position + 23;
                }

                Main_api main_api = new Main_api(String.valueOf(region_code));
                Thread main_thread = new Thread(main_api);

                try {
                    main_thread.start();
                    main_thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Main_urls = main_api.getMain_urls();
                Main_titles = main_api.getMain_titles();
                Main_addrs = main_api.getMain_addrs();
                Main_conIds = main_api.getMain_contentids();

                Init();
                for (int i=0; i < Main_conIds.size(); i++){
                    addItem(Main_urls.get(i), Main_titles.get(i), Main_addrs.get(i), Main_conIds.get(i));
                }

                main_recyclerviewadapter = new MainRecyclerViewAdapter(mainlist);
                main_recyclerview.setAdapter(main_recyclerviewadapter);
                main_recyclerview.setLayoutManager(new LinearLayoutManager(Mainactivity.this));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavi);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.matching:
                        Intent intent = new Intent(getApplicationContext(), Matching.class);
                        startActivity(intent);
                        break;

                    case R.id.home:
                        Intent intent2 = new Intent(getApplicationContext(), Mainactivity.class);
                        startActivity(intent2);
                        break;

                    case R.id.freeboard:
                        Intent intent3 = new Intent(getApplicationContext(), Freeboard_Activity.class);
                        startActivity(intent3);
                        break;

                }
                return false;
            }
        });

        //네비게이션바(햄버거) 클릭 이벤트 삽입 구간
        drawerLayout = findViewById(R.id.drawlayout);
        drawerView = findViewById(R.id.drawer);

        ImageButton nvg_open = findViewById(R.id.nvg_open);
        nvg_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawerView);
            }
        });

        ImageButton nvg_close = findViewById(R.id.nvg_close);
        nvg_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
            }
        });

        drawerLayout.setDrawerListener(listener);
        drawerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        List<String> list = new ArrayList<>();
        list.add("공지사항");
        list.add("이벤트");
        list.add("고객센터");
        list.add("설정");
        list.add("로그인");

        FirebaseAuth aAuth = FirebaseAuth.getInstance();

        String uid = user != null ? user.getUid() : null; // 로그인한 유저의 고유 uid 가져오기

        listview = findViewById(R.id.navi_list);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            list.set(4, "로그아웃");

            layout_account.setVisibility(View.VISIBLE);
            my_nickname = (TextView) findViewById(R.id.my_page_login);
            String myuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("users").child(myuid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    Glide.with(Mainactivity.this)
                            .load(userModel.imageUri)
                            .apply(new RequestOptions().circleCrop())
                            .into(profile_photo);
                    my_nickname.setText(userModel.nickname);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
            my_nickname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), My_Page.class);
                    startActivity(intent);
                }
            });

        } else {
            unlogin.setVisibility(View.VISIBLE);
            unlogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Login_New_Page.class);
                    startActivity(intent);
                }
            });
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.
                simple_list_item_1, list);

        listview.setAdapter(adapter);






        mDatabase = FirebaseDatabase.getInstance().getReference(); // 파이어베이스 realtime database 에서 정보 가져오기

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent intent_notice =
                                new Intent(getApplicationContext(), Notice.class);
                        startActivity(intent_notice);
                        break;

                    case 1:
                        // 공지사항이랑 이벤트는 같은 페이지임 나중에 이벤트 클릭된 상태로 넘어가게 하면됨
                        Intent intent_event =
                                new Intent(getApplicationContext(), Notice.class);
                        startActivity(intent_event);
                        break;

                    case 2:
                        break;

                    case 3:
                        Intent intent_setting = new Intent(getApplicationContext(), Setting.class);
                        startActivity(intent_setting);
                        break;

                    case 4:
                        if (list.get(7).equals("로그아웃")) {
                            //로그아웃 이벤트
                        } else {
                            Intent intent = new Intent(getApplicationContext(), Login_New_Page.class);
                            startActivity(intent);
                        }
                        break;

                }
            }
        });

        my_page = findViewById(R.id.my_account);
        my_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), My_Page.class);
                startActivity(intent);
            }
        });

        //네비게이션바

        Intent intent = getIntent();

        String new_id;
        String new_pw;
        String new_animal;
        String animal_info;

        new_id = getIntent().getStringExtra("NEW_ID");
        new_pw = getIntent().getStringExtra("NEW_PW");
        new_animal = getIntent().getStringExtra("ANIMAL_INFO");
        animal_info = getIntent().getStringExtra("ANIMAL_MORE_INFO");

        main_search();

    }


    //네비게이션바
    DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };
    //네비게이션바

    // serach box

    public String str;
    private void main_search() {
        final EditText main_search = findViewById(R.id.main_search);
        str = main_search.getText().toString();
        main_search.getText().clear();

        main_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                str = main_search.getText().toString();
                switch (actionId) {
                    case IME_ACTION_SEARCH:
                        Intent intent = new Intent(getApplicationContext(), Category.class);
                        intent.putExtra("SEARCH", str);

                        startActivity(intent);
                }
                return true;
            }
        });
    }

    public void onTextViewClick() {
        category1 = (TextView) findViewById(R.id.category_1);
        category1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getcategory1;
                getcategory1 = category1.getText().toString();
                Intent intent = new Intent(getApplicationContext(), Category.class);
                intent.putExtra("SEARCH", getcategory1);

                startActivity(intent);
            }
        });
        category2 = (TextView) findViewById(R.id.category_2);
        category2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getcategory1;
                getcategory1 = category2.getText().toString();
                Intent intent = new Intent(getApplicationContext(), Category.class);
                intent.putExtra("SEARCH", getcategory1);

                startActivity(intent);
            }
        });
        category3 = (TextView) findViewById(R.id.category_3);
        category3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getcategory1;
                getcategory1 = category3.getText().toString();
                Intent intent = new Intent(getApplicationContext(), Category.class);
                intent.putExtra("SEARCH", getcategory1);

                startActivity(intent);
            }
        });
        category4 = (TextView) findViewById(R.id.category_4);
        category4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getcategory1;
                getcategory1 = category4.getText().toString();
                Intent intent = new Intent(getApplicationContext(), Category.class);
                intent.putExtra("SEARCH", getcategory1);
                startActivity(intent);
            }
        });
    }

    public void Init(){
        main_recyclerview = (RecyclerView)findViewById(R.id.main_recyclerview);
        mainlist = new ArrayList<>();
    }

    public void addItem(String url, String title, String addr, String conId){
        MainRecyclerViewItem main_item = new MainRecyclerViewItem();

        main_item.setUrl(url);
        main_item.setTitle(title);
        main_item.setAddr(addr);
        main_item.setConId(conId);
        mainlist.add(main_item);
    }
}