package com.selfcompany.yandexmusiclist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

public class DetailActivity extends Activity {

    ShareActionProvider mShareActionProvider;
    ImageView imageView;
    Artist artist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        imageView = (ImageView) findViewById(R.id.img_big);
        TextView nameView = (TextView) findViewById(R.id.artist_name);
        TextView genresView = (TextView) findViewById(R.id.artist_genres);
        TextView albumsView = (TextView) findViewById(R.id.artist_albums);
        TextView tracksView = (TextView) findViewById(R.id.artist_tracks);
        //разворачиваемый textView
        ExpandableTextView descriptionView = (ExpandableTextView) findViewById(R.id.expand_text_view);
        TextView linkView = (TextView) findViewById(R.id.artist_link);

        //десериализация обьекта-исполнитель по которому нажали в предыдущей активити
        artist = Parcels.unwrap(getIntent().getParcelableExtra("artist"));

        //заполненение всех вью
        //загрузка большого аватара
        //сначала пробуем из кеша
        Picasso.with(this).load(artist.getCover().getBig()).networkPolicy(NetworkPolicy.OFFLINE).
                placeholder(R.drawable.music_android_r_l).into(imageView, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                //не получилось - пробуем из интернета
                Picasso.with(getApplicationContext()).load(artist.getCover().getBig()).
                        placeholder(R.drawable.music_android_r_l).into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Log.v("Picasso", "Could not fetch image");
                        Toast.makeText(getApplicationContext(), "Не удалось загрузить изображение", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        if (artist.getName() == null || artist.getName().isEmpty())
            nameView.setText(getString(R.string.artist_name, "Не указано"));
        else
            nameView.setText(getString(R.string.artist_name, artist.getName()));

        if (artist.getGenres() == null || artist.getGenres().size() == 0)
            genresView.setText(getString(R.string.artist_genres, "Не указано"));
        else {
            StringBuilder sb = new StringBuilder();
            for (String genr : artist.getGenres()) {
                sb.append(genr + " ");
            }
            genresView.setText(getString(R.string.artist_genres, sb.toString().trim().replaceAll(" ", ", ")));
        }

        if (artist.getAlbums() == 0)
            albumsView.setText(getString(R.string.artist_albums, "Не указано"));
        else
            albumsView.setText(getString(R.string.artist_albums, artist.getAlbums()));

        if (artist.getTracks() == 0)
            tracksView.setText(getString(R.string.artist_tracks, "Не указано"));
        else
            tracksView.setText(getString(R.string.artist_tracks, artist.getTracks()));

        if (artist.getDescription() == null || artist.getDescription().isEmpty())
            descriptionView.setText(getString(R.string.artist_description, "Не указано"));
        else
            descriptionView.setText(getString(R.string.artist_description, artist.getDescription()));

        if (artist.getLink() == null || artist.getLink().isEmpty())
            linkView.setText(getString(R.string.artist_link, "Не указано"));
        else
            linkView.setText(getString(R.string.artist_link, artist.getLink()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //заполняем меню
        getMenuInflater().inflate(R.menu.menu_detail, menu);

        //доступ к баркнопке "поделиться"
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);

        // Доступ к объекту, ответственного за вызов "поделиться" подменю
        if (shareItem != null) {
            mShareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
        }

        //создание интента для "поделиться" контентом
        setShareIntent();

        return true;
    }

    private void setShareIntent() {

        // интент "поделиться" с предустановленными темой и ссылкой но аватар
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Зацените!");
        shareIntent.putExtra(Intent.EXTRA_TEXT, artist.getCover().getBig());

        // убеждаемся, что провайдер знает, что он должен работать с этим интент
        mShareActionProvider.setShareIntent(shareIntent);
    }
}
