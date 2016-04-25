package com.selfcompany.yandexmusiclist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.parceler.Parcels;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final String url = "http://download.cdn.yandex.net/mobilization-2016/artists.json";
    ListView mainListView;
    JSONAdapter mJSONAdapter;
    ProgressDialog mDialog;
    ArrayList<Artist> mArtistList = new ArrayList<>();
    private static final String PREFS = "prefs";
    private static final String PREF_ORDER = "orderType";
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //доступ к тривиальной постоянной памяти, чтобы знать о типе сортировке с прошлого использования
        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        mainListView = (ListView) findViewById(R.id.main_listview);

        //слушатель кликов по элементам списка
        mainListView.setOnItemClickListener(this);

        mJSONAdapter = new JSONAdapter(this, getLayoutInflater());

        //привязка вью списка к адаптеру
        mainListView.setAdapter(mJSONAdapter);

        //обращение к серверу, получние json-response с парсом, заполнение списка
        receiveData();
        //сортировка списка по раннее указываемому типу
        sortListView();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //доступ к searchView
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Исполнитель...");

        //слушатель изменений в строке редактирования Поиска
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            //не нужно - отслеживаем каждый введенный символ
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            //фильтруем список, отправляя каждое изменение строки редактирования адаптеру списка
            @Override
            public boolean onQueryTextChange(String newText) {
                mJSONAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //нажата кнопка сортировки
            case R.id.sort:
                //настройка диалогового окна с последующим вызовом
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.order_type).setItems(R.array.order_types, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //изменяем тип выбранной сортировки в тривиальной постоянной памяти
                        SharedPreferences.Editor e = mSharedPreferences.edit();
                        e.putInt(PREF_ORDER, which + 1);
                        e.commit();

                        //сортируем в соответсвии с нынешним выбором
                        sortListView();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void receiveData () {
        new AsyncTask<Void,Void,Void>(){

            //перед обращение к серверу - блокируем UI
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mDialog = new ProgressDialog(MainActivity.this);
                mDialog.setCancelable(false);
                mDialog.setMessage("Загрузка...");
                mDialog.show();
            }

            //обращение к серверу, получение json-response, парсим
            @Override
            protected Void doInBackground(Void... voids) {
                Reader reader=API.getData(url);

                Type listType = new TypeToken<ArrayList<Artist>>(){}.getType();
                mArtistList = new GsonBuilder().create().fromJson(reader, listType);
                return null;
            }

            //обновляем инфу для адаптера, разблокируем UI
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mJSONAdapter.updateData(mArtistList);
                mDialog.dismiss();
            }
        }.execute();
    }

    //сортировка
    public void sortListView(){
        //вытаскиваем из тривиальной постоянной памяти инфу о типе сортировки
        final int orderType = mSharedPreferences.getInt(PREF_ORDER, 0);

        Collections.sort(mArtistList, new Comparator<Artist>() {

            //кастомный компаратор для всех типов сортировки
            @Override
            public int compare(Artist lhs, Artist rhs) {
                //res = -1 - lhs идет выше rhs
                //res = 0 - lhs равен rhs
                //res = 1 - lhs идет ниже rhs
                int res = 0;
                switch (orderType) {
                    //по имени восходящий
                    case 1:
                        res = lhs.getName().compareToIgnoreCase(rhs.getName());
                        return res;
                    //по имени нисходящий
                    case 2:
                        res = -lhs.getName().compareToIgnoreCase(rhs.getName());
                        return res;
                    //по кол-ву альбомов восходящий
                    case 3:
                        if (lhs.getAlbums() < rhs.getAlbums()) res = -1;
                        else res = 1;
                        return res;
                    //по кол-ву альбомов нисходящий
                    case 4:
                        if (lhs.getAlbums() < rhs.getAlbums()) res = 1;
                        else res = -1;
                        return res;
                    //по кол-ву треков восходящий
                    case 5:
                        if (lhs.getTracks() < rhs.getTracks()) res = -1;
                        else res = 1;
                        return res;
                    //по кол-ву треков нисходящий
                    case 6:
                        if (lhs.getTracks() < rhs.getTracks()) res = 1;
                        else res = -1;
                        return res;
                    default:
                        return res;
                }
            }
        });

        mJSONAdapter.updateData(mArtistList);
    }

    //клик по эл-ту списка - настройка интента второй активити "Подробно" и ее запуск
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        //эл-т списка по которому кликнули
        Artist artist = mJSONAdapter.getItem(position);

        Intent detailIntent = new Intent(this, DetailActivity.class);

        // запаковываем в интент эл-т списка ко которому кликнули
        Bundle bundle = new Bundle();
        bundle.putParcelable("artist", Parcels.wrap(artist));
        detailIntent.putExtras(bundle);

        // запуск следующей активити
        startActivity(detailIntent);
    }

}
